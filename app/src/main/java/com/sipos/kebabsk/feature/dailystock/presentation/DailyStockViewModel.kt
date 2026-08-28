package com.sipos.kebabsk.feature.dailystock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.AppSessionStore
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.feature.auth.data.remote.AuthApiService
import com.sipos.kebabsk.feature.dailystock.domain.repository.DailyStockRepository
import com.sipos.kebabsk.feature.dailystock.domain.model.DailyStockResult
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipeAnchorInput
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipeGroup
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipePreset
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipePreview
import com.sipos.kebabsk.feature.dailystock.domain.model.CashReconciliation
import com.sipos.kebabsk.feature.dailystock.domain.validation.DailyStockValidator
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.common.validation.ValidationResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.util.UUID

data class DailyStockUiState(
    val isLoading: Boolean = false,
    val items: List<DailyStockItem> = emptyList(),
    val sessionId: Long? = null,
    val isSessionOpen: Boolean? = null,
    val sessionStatusLabel: String? = null,
    val businessDate: String? = null,
    val cutoffTime: String? = null,
    val canClose: Boolean = false,
    val isOverdue: Boolean = false,
    val errorMessage: String? = null,
    val isCashReconciliationPending: Boolean = false,
    val closingPresets: List<ClosingRecipePreset> = emptyList(),
    val closingGroups: List<ClosingRecipeGroup> = emptyList(),
    val isPreviewingClosing: Boolean = false,
    val closingPreview: ClosingRecipePreview? = null,
    val closingPreviewError: String? = null,
    val cashReconciliation: CashReconciliation? = null,
    val isLoadingCashReconciliation: Boolean = false,
    val cashReconciliationError: String? = null,

    // Close session state
    val isClosing: Boolean = false,
    val closeSuccess: Boolean = false,
    val closeSuccessMessage: String? = null,
    val closeErrorMessage: String? = null
)

class DailyStockViewModel(
    private val repository: DailyStockRepository,
    private val authApiService: AuthApiService,
    private val validator: DailyStockValidator = DailyStockValidator()
) : ViewModel() {
    private var closingIdempotencyKey: String = UUID.randomUUID().toString()
    private var closingPreviewJob: Job? = null
    private var closingPreviewVersion: Long = 0
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
            var businessDate: String? = null
            var cutoffTime: String? = null
            var canClose = false
            var isOverdue = false
            var backendStatusMessage: String? = null
            try {
                val token = AppSessionStore.loadSession()?.token ?: ""
                val statusResponse = authApiService.sessionCurrentStatus("Bearer $token")
                if (statusResponse.isSuccessful) {
                    val body = statusResponse.body()
                    val rootActive = body.getBooleanOrNull("active")

                    if (body != null && body.has("data") && body.get("data").isJsonObject) {
                        val data = body.getAsJsonObject("data")
                        val stockStatus = data.get("stock_session_status")?.asString ?: data.get("stock_status")?.asString
                        val sessionStatus = data.get("status")?.asString
                        businessDate = data.get("business_date")?.asString
                            ?: data.get("session_date")?.asString
                        cutoffTime = data.get("cutoff_time")?.asString
                            ?: data.get("closing_grace_until")?.asString
                        canClose = data.getBooleanOrNull("can_close") ?: false
                        isOverdue = data.getBooleanOrNull("overdue") ?: false
                        backendStatusMessage = if (isOverdue) {
                            body.get("message")?.asString
                        } else {
                            null
                        }

                        val isStockClosed = stockStatus?.uppercase() == "CLOSED" || stockStatus?.uppercase() == "RECONCILED"
                        val isFinanciallyOpen = sessionStatus?.uppercase() == "OPEN"

                        isPending = isStockClosed && isFinanciallyOpen
                        isSessionOpen = resolveSessionOpenState(
                            active = data.getBooleanOrNull("active") ?: rootActive,
                            sessionStatus = sessionStatus,
                            stockStatus = stockStatus
                        )
                        sessionStatusLabel = if (isOverdue) {
                            "Melewati Cut-off"
                        } else {
                            resolveSessionStatusLabel(isSessionOpen, sessionStatus, stockStatus)
                        }
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
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (e: Exception) {
                // Ignore failure for current-status, continue to daily stock load
            }

            try {
                val token = AppSessionStore.loadSession()?.token ?: ""
                repository.getDailyStock(token)
                    .onSuccess { result ->
                        _uiState.update {
                            it.copy(
                                items = result.items,
                                sessionId = result.sessionId,
                                closingPresets = result.closingPresets,
                                closingGroups = result.closingGroups,
                                isSessionOpen = isSessionOpen,
                                sessionStatusLabel = sessionStatusLabel,
                                businessDate = result.businessDate ?: businessDate,
                                cutoffTime = result.cutoffTime ?: cutoffTime,
                                canClose = result.canClose,
                                isOverdue = result.overdue || isOverdue,
                                isCashReconciliationPending = isPending,
                                errorMessage = result.statusMessage ?: backendStatusMessage ?: if (result.items.isEmpty() && result.sessionId == null) {
                                    "Sesi stok harian belum dibuka oleh admin hari ini."
                                } else null
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                items = emptyList(),
                                isSessionOpen = isSessionOpen ?: it.isSessionOpen,
                                sessionStatusLabel = sessionStatusLabel ?: it.sessionStatusLabel,
                                businessDate = businessDate ?: it.businessDate,
                                cutoffTime = cutoffTime ?: it.cutoffTime,
                                canClose = canClose,
                                isOverdue = isOverdue,
                                isCashReconciliationPending = isPending,
                                errorMessage = sanitizeUserMessage(
                                    error.message,
                                    "Stok bahan harian belum bisa dimuat. Silakan coba lagi."
                                )
                            )
                        }
                    }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadCashReconciliation() {
        if (_uiState.value.isLoadingCashReconciliation) return
        _uiState.update {
            it.copy(
                isLoadingCashReconciliation = true,
                cashReconciliationError = null
            )
        }
        val token = AppSessionStore.loadSession()?.token ?: ""
        viewModelScope.launch {
            try {
                repository.getCashReconciliation(token)
                    .onSuccess { reconciliation ->
                        _uiState.update {
                            it.copy(
                                cashReconciliation = reconciliation,
                                cashReconciliationError = null
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                cashReconciliation = null,
                                cashReconciliationError = sanitizeUserMessage(
                                    error.message,
                                    "Rekonsiliasi kas belum bisa dimuat."
                                )
                            )
                        }
                    }
            } finally {
                _uiState.update { it.copy(isLoadingCashReconciliation = false) }
            }
        }
    }

    fun closeSession(
        remaining: Map<Long, Double>,
        notes: String?,
        actualCash: Long = 0L
    ) {
        if (_uiState.value.isClosing) return
        val validation = validator.validateCloseSession(
            sessionId = _uiState.value.sessionId,
            items = _uiState.value.items,
            remaining = remaining
        )
        if (validation is ValidationResult.Invalid) {
            _uiState.update {
                it.copy(
                    closeSuccess = false,
                    closeErrorMessage = validation.message
                )
            }
            return
        }
        _uiState.update { it.copy(isClosing = true, closeErrorMessage = null, closeSuccess = false) }
        val token = AppSessionStore.loadSession()?.token ?: ""
        viewModelScope.launch {
            try {
                repository.closeSession(
                    token,
                    remaining,
                    notes?.trim()?.takeIf { it.isNotBlank() },
                    actualCash
                )
                    .onSuccess { message ->
                        _uiState.update {
                            it.copy(
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
                                closeSuccess = false,
                                closeErrorMessage = sanitizeUserMessage(
                                    error.message,
                                    "Gagal menutup sesi stok harian. Silakan coba lagi."
                                )
                            )
                        }
                    }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } finally {
                _uiState.update { it.copy(isClosing = false) }
            }
        }
    }

    fun previewClosing(anchors: List<ClosingRecipeAnchorInput>) {
        if (anchors.isEmpty()) return
        val requestVersion = ++closingPreviewVersion
        closingPreviewJob?.cancel()
        _uiState.update {
            it.copy(isPreviewingClosing = true, closingPreview = null, closingPreviewError = null)
        }
        val token = AppSessionStore.loadSession()?.token ?: ""
        closingPreviewJob = viewModelScope.launch {
            try {
                repository.previewClosing(token, anchors)
                    .onSuccess { preview ->
                        if (requestVersion == closingPreviewVersion) {
                            _uiState.update { it.copy(closingPreview = preview, closingPreviewError = null) }
                        }
                    }
                    .onFailure { error ->
                        if (requestVersion == closingPreviewVersion) {
                            _uiState.update {
                                it.copy(closingPreviewError = sanitizeUserMessage(error.message, "Perhitungan resep gagal."))
                            }
                        }
                    }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } finally {
                if (requestVersion == closingPreviewVersion) {
                    _uiState.update { it.copy(isPreviewingClosing = false) }
                }
            }
        }
    }

    fun clearClosingPreview() {
        closingPreviewVersion++
        closingPreviewJob?.cancel()
        closingPreviewJob = null
        _uiState.update {
            it.copy(isPreviewingClosing = false, closingPreview = null, closingPreviewError = null)
        }
    }

    fun closeSessionWithRecipe(
        remainingOverrides: Map<Long, Double>,
        anchors: List<ClosingRecipeAnchorInput>,
        notes: String?,
        actualCash: Long = 0L
    ) {
        if (_uiState.value.isClosing) return
        if (_uiState.value.closingPreview == null) {
            _uiState.update { it.copy(closeErrorMessage = "Hitung sisa otomatis terlebih dahulu.") }
            return
        }
        val validation = validator.validateCloseSession(
            sessionId = _uiState.value.sessionId,
            items = _uiState.value.items,
            remaining = remainingOverrides
        )
        if (validation is ValidationResult.Invalid) {
            _uiState.update { it.copy(closeErrorMessage = validation.message) }
            return
        }
        _uiState.update { it.copy(isClosing = true, closeErrorMessage = null, closeSuccess = false) }
        val token = AppSessionStore.loadSession()?.token ?: ""
        viewModelScope.launch {
            try {
                repository.closeSessionWithRecipe(
                    token,
                    remainingOverrides,
                    anchors,
                    notes?.trim()?.takeIf { it.isNotBlank() },
                    closingIdempotencyKey,
                    actualCash
                ).onSuccess { message ->
                    closingIdempotencyKey = UUID.randomUUID().toString()
                    _uiState.update {
                        it.copy(
                            closeSuccess = true,
                            closeSuccessMessage = message,
                            closeErrorMessage = null,
                            sessionId = null,
                            isSessionOpen = false,
                            sessionStatusLabel = "Sesi Harian Ditutup"
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(closeErrorMessage = sanitizeUserMessage(error.message, "Gagal menutup sesi stok harian."))
                    }
                }
            } finally {
                _uiState.update { it.copy(isClosing = false) }
            }
        }
    }

    fun clearCloseState() {
        _uiState.update {
            it.copy(
                closeSuccess = false,
                closeSuccessMessage = null,
                closeErrorMessage = null,
                closingPreview = null,
                closingPreviewError = null,
                cashReconciliation = null,
                cashReconciliationError = null,
                isLoadingCashReconciliation = false
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
