package com.sipos.kebabsk.feature.shift.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.feature.dailystock.data.repository.DailyStockRepositoryImpl
import com.sipos.kebabsk.feature.shift.data.remote.CloseSessionData
import com.sipos.kebabsk.feature.shift.data.remote.CloseSessionRequest
import com.sipos.kebabsk.feature.shift.data.remote.CloseShiftApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CloseShiftUiState(
    // Pre-flight readiness
    val isCheckingReadiness: Boolean = false,
    val isReadyToClose: Boolean = false,
    val readinessMessage: String? = null,
    val sessionId: Long? = null,

    // Submit state
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,

    // Reconciliation result — SSOT dari server, bukan kalkulasi client
    val reconciliationResult: CloseSessionData? = null
)

class CloseShiftViewModel(
    private val token: String,
    private val closeShiftApiService: CloseShiftApiService,
    private val dailyStockRepository: DailyStockRepositoryImpl,
    private val targetSessionId: Long? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloseShiftUiState())
    val uiState: StateFlow<CloseShiftUiState> = _uiState.asStateFlow()

    init {
        checkReadiness()
    }

    /**
     * Pre-flight Check: Verifikasi apakah semua sisa stok sudah terisi.
     * Jika ada item dengan remainingQty == null, kasir WAJIB mengisi stok
     * terlebih dahulu sebelum bisa menutup sesi.
     */
    fun checkReadiness() {
        if (targetSessionId != null) {
            _uiState.update {
                it.copy(
                    isCheckingReadiness = false,
                    sessionId = targetSessionId,
                    isReadyToClose = true,
                    readinessMessage = null,
                    errorMessage = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isCheckingReadiness = true,
                errorMessage = null,
                readinessMessage = null
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            dailyStockRepository.getDailyStock(token)
                .onSuccess { result ->
                    val resolvedSessionId = result.sessionId
                    val hasNullRemaining = result.items.any { item -> item.remainingQty == null }
                    val hasNoItems = result.items.isEmpty()

                    _uiState.update { state ->
                        state.copy(
                            isCheckingReadiness = false,
                            sessionId = resolvedSessionId,
                            isReadyToClose = !hasNullRemaining && !hasNoItems && resolvedSessionId != null,
                            readinessMessage = when {
                                resolvedSessionId == null -> "Tidak ada sesi stok harian yang aktif."
                                hasNoItems -> "Tidak ada data stok harian. Hubungi admin."
                                hasNullRemaining -> "Harap isi stok sisa bahan baku terlebih dahulu!"
                                else -> null
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isCheckingReadiness = false,
                            isReadyToClose = false,
                            readinessMessage = sanitizeUserMessage(
                                error.message,
                                "Gagal memeriksa kesiapan stok. Silakan coba lagi."
                            )
                        )
                    }
                }
        }
    }

    /**
     * Eksekusi penutupan shift. Mengirim uang kas fisik ke server.
     * Variance diterima mentah dari API — DILARANG dihitung di client.
     */
    fun submitCloseShift(actualPhysicalCash: Double, closingNotes: String?) {
        val sessionId = _uiState.value.sessionId ?: return

        // Guard: cegah double-submit
        if (_uiState.value.isSubmitting) return

        _uiState.update { state -> state.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = CloseSessionRequest(
                    actualPhysicalCash = actualPhysicalCash,
                    closingNotes = closingNotes?.trim()?.takeIf { text -> text.isNotBlank() }
                )

                val response = closeShiftApiService.closeSession(
                    authorization = "Bearer $token",
                    sessionId = sessionId,
                    request = request
                )

                if (response.isSuccessful && response.body() != null) {
                    // SSOT: Terima data rekonsiliasi mentah dari server
                    val reconciliation = response.body()!!.data
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            reconciliationResult = reconciliation
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val serverMessage = runCatching {
                        com.google.gson.JsonParser.parseString(errorBody)
                            .asJsonObject.get("message")?.asString
                    }.getOrNull()

                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            errorMessage = mapCloseError(response.code(), serverMessage)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        errorMessage = sanitizeUserMessage(
                            e.message,
                            "Terjadi kesalahan jaringan. Silakan coba lagi."
                        )
                    )
                }
            }
        }
    }

    fun clearState() {
        _uiState.update { CloseShiftUiState() }
    }

    private fun mapCloseError(code: Int, rawMessage: String?): String {
        return when (code) {
            401 -> "Sesi login sudah berakhir. Silakan login ulang."
            403 -> "Anda tidak memiliki izin untuk menutup sesi ini."
            404 -> "Sesi stok harian tidak ditemukan."
            409 -> "Sesi ini sudah ditutup sebelumnya."
            422 -> rawMessage ?: "Data belum valid. Pastikan semua stok sisa sudah terisi."
            429 -> "Permintaan terlalu sering. Coba lagi beberapa saat."
            in 500..599 -> "Layanan sedang bermasalah. Silakan coba lagi nanti."
            else -> sanitizeUserMessage(rawMessage, "Gagal menutup sesi. Silakan coba lagi.")
        }
    }
}
