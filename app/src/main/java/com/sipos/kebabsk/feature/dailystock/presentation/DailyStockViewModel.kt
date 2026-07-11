package com.sipos.kebabsk.feature.dailystock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.feature.dailystock.data.repository.DailyStockRepositoryImpl
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DailyStockUiState(
    val isLoading: Boolean = false,
    val items: List<DailyStockItem> = emptyList(),
    val sessionId: Long? = null,
    val isSessionOpen: Boolean? = null,
    val sessionStatusLabel: String? = null,
    val errorMessage: String? = null,
    val isCashReconciliationPending: Boolean = false,

    // Close session state
    val isClosing: Boolean = false,
    val closeSuccess: Boolean = false,
    val closeSuccessMessage: String? = null,
    val closeErrorMessage: String? = null
)

class DailyStockViewModel(
    private val token: String,
    private val repository: DailyStockRepositoryImpl
) : ViewModel() {
    private val _uiState = MutableStateFlow(DailyStockUiState())
    val uiState: StateFlow<DailyStockUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            var isPending = false
            var isSessionOpen: Boolean? = null
            var sessionStatusLabel: String? = null
            try {
                val statusResponse = com.sipos.kebabsk.data.network.NetworkModule.authApiService.sessionCurrentStatus("Bearer $token")
                if (statusResponse.isSuccessful) {
                    val body = statusResponse.body()
                    val rootActive = body.getBooleanOrNull("active")

                    if (body != null && body.has("data") && body.get("data").isJsonObject) {
                        val data = body.getAsJsonObject("data")
                        val stockStatus = data.get("stock_session_status")?.asString ?: data.get("stock_status")?.asString
                        val sessionStatus = data.get("status")?.asString

                        val isStockClosed = stockStatus?.uppercase() == "CLOSED" || stockStatus?.uppercase() == "RECONCILED"
                        val isFinanciallyOpen = sessionStatus?.uppercase() == "OPEN"

                        isPending = isStockClosed && isFinanciallyOpen
                        isSessionOpen = resolveSessionOpenState(
                            active = data.getBooleanOrNull("active") ?: rootActive,
                            sessionStatus = sessionStatus,
                            stockStatus = stockStatus
                        )
                        sessionStatusLabel = resolveSessionStatusLabel(isSessionOpen, sessionStatus, stockStatus)
                    } else if (rootActive != null) {
                        isSessionOpen = rootActive
                        sessionStatusLabel = if (rootActive) {
                            "Sesi Harian Aktif"
                        } else {
                            "Sesi Harian Belum Dibuka"
                        }
                    }
                } else if (statusResponse.code() == 404 || statusResponse.code() == 409) {
                    isSessionOpen = false
                    sessionStatusLabel = "Sesi Harian Belum Dibuka"
                }
            } catch (e: Exception) {
                // Ignore failure for current-status, continue to daily stock load
            }

            repository.getDailyStock(token)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = result.items,
                            sessionId = result.sessionId,
                            isSessionOpen = isSessionOpen,
                            sessionStatusLabel = sessionStatusLabel,
                            isCashReconciliationPending = isPending,
                            errorMessage = if (result.items.isEmpty() && result.sessionId == null) {
                                "Sesi stok harian belum dibuka oleh admin hari ini."
                            } else null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = emptyList(),
                            isSessionOpen = isSessionOpen ?: it.isSessionOpen,
                            sessionStatusLabel = sessionStatusLabel ?: it.sessionStatusLabel,
                            isCashReconciliationPending = isPending,
                            errorMessage = sanitizeUserMessage(
                                error.message,
                                "Stok bahan harian belum bisa dimuat. Silakan coba lagi."
                            )
                        )
                    }
                }
        }
    }

    fun closeSession(remaining: Map<Long, Double>, notes: String?) {
        _uiState.update { it.copy(isClosing = true, closeErrorMessage = null, closeSuccess = false) }
        viewModelScope.launch {
            repository.closeSession(token, remaining, notes)
                .onSuccess { message ->
                    _uiState.update {
                        it.copy(
                            isClosing = false,
                            closeSuccess = true,
                            closeSuccessMessage = message,
                            closeErrorMessage = null,
                            sessionId = null,
                            isSessionOpen = false,
                            sessionStatusLabel = "Sesi Harian Ditutup"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isClosing = false,
                            closeSuccess = false,
                            closeErrorMessage = sanitizeUserMessage(
                                error.message,
                                "Gagal menutup sesi stok harian. Silakan coba lagi."
                            )
                        )
                    }
                }
        }
    }

    fun clearCloseState() {
        _uiState.update {
            it.copy(
                closeSuccess = false,
                closeSuccessMessage = null,
                closeErrorMessage = null
            )
        }
    }

    private fun resolveSessionOpenState(
        active: Boolean?,
        sessionStatus: String?,
        stockStatus: String?
    ): Boolean? {
        val normalizedSessionStatus = sessionStatus?.uppercase()
        val normalizedStockStatus = stockStatus?.uppercase()

        return when {
            active == false -> false
            normalizedSessionStatus in INACTIVE_SESSION_STATUSES -> false
            normalizedStockStatus in INACTIVE_STOCK_STATUSES -> false
            active == true -> true
            normalizedSessionStatus == "OPEN" -> true
            normalizedStockStatus == "OPEN" -> true
            else -> null
        }
    }

    private fun com.google.gson.JsonObject?.getBooleanOrNull(key: String): Boolean? {
        val value = this?.get(key) ?: return null
        if (value.isJsonNull) return null
        return runCatching { value.asBoolean }.getOrNull()
    }

    private fun resolveSessionStatusLabel(
        isSessionOpen: Boolean?,
        sessionStatus: String?,
        stockStatus: String?
    ): String? {
        val normalizedSessionStatus = sessionStatus?.uppercase()
        val normalizedStockStatus = stockStatus?.uppercase()

        return when {
            isSessionOpen == true -> "Sesi Harian Aktif"
            normalizedSessionStatus in CLOSED_SESSION_STATUSES ||
                normalizedStockStatus in CLOSED_STOCK_STATUSES -> "Sesi Harian Ditutup"
            isSessionOpen == false -> "Sesi Harian Belum Dibuka"
            !stockStatus.isNullOrBlank() -> "Status stok: ${stockStatus.lowercase()}"
            !sessionStatus.isNullOrBlank() -> "Status sesi: ${sessionStatus.lowercase()}"
            else -> null
        }
    }

    companion object {
        private val CLOSED_SESSION_STATUSES = setOf("CLOSED", "CLOSE", "ENDED", "FINISHED")
        private val CLOSED_STOCK_STATUSES = setOf("CLOSED", "CLOSE", "RECONCILED", "ENDED", "FINISHED")
        private val NOT_OPENED_STATUSES = setOf("NOT_OPENED", "NOT_STARTED", "NONE", "NO_SESSION")
        private val INACTIVE_SESSION_STATUSES = CLOSED_SESSION_STATUSES + NOT_OPENED_STATUSES
        private val INACTIVE_STOCK_STATUSES = CLOSED_STOCK_STATUSES + NOT_OPENED_STATUSES
    }
}
