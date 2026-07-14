package com.sipos.kebabsk.feature.shift.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.AppSessionStore
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.feature.dailystock.domain.repository.DailyStockRepository
import com.sipos.kebabsk.feature.shift.data.remote.CloseSessionData
import com.sipos.kebabsk.feature.shift.data.remote.CloseSessionRequest
import com.sipos.kebabsk.feature.shift.domain.repository.CloseShiftRepository
import kotlinx.coroutines.CancellationException
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

    // Reconciliation result â€” SSOT dari server, bukan kalkulasi client
    val reconciliationResult: CloseSessionData? = null
)

class CloseShiftViewModel(
    private val closeShiftRepository: CloseShiftRepository,
    private val dailyStockRepository: DailyStockRepository
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
    fun checkReadiness(targetSessionId: Long? = null) {
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

        val token = AppSessionStore.loadSession()?.token ?: ""
        viewModelScope.launch {
            try {
                dailyStockRepository.getDailyStock(token)
                    .onSuccess { result ->
                        val resolvedSessionId = result.sessionId
                        val hasNullRemaining = result.items.any { item -> item.remainingQty == null }
                        val hasNoItems = result.items.isEmpty()

                        _uiState.update { state ->
                            state.copy(
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
                                isReadyToClose = false,
                                readinessMessage = sanitizeUserMessage(
                                    error.message,
                                    "Gagal memeriksa kesiapan stok. Silakan coba lagi."
                                )
                            )
                        }
                    }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } finally {
                _uiState.update { it.copy(isCheckingReadiness = false) }
            }
        }
    }

    /**
     * Eksekusi penutupan shift. Mengirim uang kas fisik ke server.
     * Variance diterima mentah dari API â€” DILARANG dihitung di client.
     */
    fun submitCloseShift(actualPhysicalCash: Long, closingNotes: String?) {
        val sessionId = _uiState.value.sessionId ?: return

        // Guard: cegah double-submit
        if (_uiState.value.isSubmitting) return

        _uiState.update { state -> state.copy(isSubmitting = true, errorMessage = null) }

        val token = AppSessionStore.loadSession()?.token ?: ""
        viewModelScope.launch {
            try {
                val request = CloseSessionRequest(
                    actualPhysicalCash = actualPhysicalCash,
                    closingNotes = closingNotes?.trim()?.takeIf { text -> text.isNotBlank() }
                )

                val result = closeShiftRepository.closeSession(
                    token = token,
                    sessionId = sessionId,
                    actualPhysicalCash = request.actualPhysicalCash,
                    closingNotes = request.closingNotes
                )

                result.fold(
                    onSuccess = { reconciliation ->
                        _uiState.update { state ->
                            state.copy(
                                reconciliationResult = reconciliation
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { state ->
                            state.copy(
                                errorMessage = error.message ?: "Gagal menutup shift. Silakan coba lagi."
                            )
                        }
                    }
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun clearState() {
        _uiState.update { CloseShiftUiState() }
    }
}
