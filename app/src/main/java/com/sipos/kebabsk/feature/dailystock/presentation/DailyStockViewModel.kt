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
            try {
                val statusResponse = com.sipos.kebabsk.data.network.NetworkModule.authApiService.sessionCurrentStatus("Bearer $token")
                if (statusResponse.isSuccessful) {
                    val body = statusResponse.body()
                    if (body != null && body.has("data") && body.get("data").isJsonObject) {
                        val data = body.getAsJsonObject("data")
                        val stockStatus = data.get("stock_session_status")?.asString ?: data.get("stock_status")?.asString
                        val sessionStatus = data.get("status")?.asString
                        
                        val isStockClosed = stockStatus?.uppercase() == "CLOSED" || stockStatus?.uppercase() == "RECONCILED"
                        val isFinanciallyOpen = sessionStatus?.uppercase() == "OPEN"
                        
                        isPending = isStockClosed && isFinanciallyOpen
                    }
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
                            closeErrorMessage = null
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
}
