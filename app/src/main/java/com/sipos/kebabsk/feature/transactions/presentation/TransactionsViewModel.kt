package com.sipos.kebabsk.feature.transactions.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionHistoryItem
import com.sipos.kebabsk.feature.transactions.domain.usecase.GetTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class VoidReason { RESTOCK, WASTE }

data class TransactionsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentDate: LocalDate = AppTime.todayJakarta(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalRevenue: Double = 0.0,
    val totalTransactionsCount: Int = 0,
    val allTransactions: List<TransactionHistoryItem> = emptyList(),
    val paginatedTransactions: List<TransactionHistoryItem> = emptyList(),
    val isVoiding: Boolean = false,
    val voidSuccess: Boolean = false,
    val voidMessage: String? = null,
    val voidErrorMessage: String? = null
)

class TransactionsViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val repository: com.sipos.kebabsk.feature.transactions.domain.repository.TransactionsRepository,
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()
    private var fetchJob: Job? = null

    init {
        fetchTransactions()
    }

    companion object {
        const val PAGE_SIZE = 10
    }

    fun fetchTransactions() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val summaryResult = repository.getRevenueSummary(token, _uiState.value.currentDate)
            val summary = summaryResult.getOrNull()

            val result = getTransactionsUseCase(token, _uiState.value.currentDate, 1)
            result.onSuccess { pageData ->
                val allItems = pageData.items
                val page = _uiState.value.currentPage
                val totalPages = maxOf(1, (allItems.size + PAGE_SIZE - 1) / PAGE_SIZE)
                val safePage = page.coerceIn(1, totalPages)
                val paginated = allItems.chunked(PAGE_SIZE).getOrNull(safePage - 1) ?: emptyList()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalRevenue = summary?.totalRevenue ?: 0.0,
                        totalTransactionsCount = summary?.totalCount ?: allItems.size,
                        allTransactions = allItems,
                        totalPages = totalPages,
                        currentPage = safePage,
                        paginatedTransactions = paginated
                    )
                }
            }.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException || error.message?.contains("cancelled", ignoreCase = true) == true) {
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = sanitizeUserMessage(error.message, "Riwayat transaksi belum bisa dimuat. Silakan coba lagi.")
                    )
                }
            }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.currentPage >= state.totalPages) return
        val newPage = state.currentPage + 1
        val paginated = state.allTransactions.chunked(PAGE_SIZE).getOrNull(newPage - 1) ?: emptyList()
        _uiState.update { it.copy(currentPage = newPage, paginatedTransactions = paginated) }
    }

    fun loadPreviousPage() {
        val state = _uiState.value
        if (state.isLoading || state.currentPage <= 1) return
        val newPage = state.currentPage - 1
        val paginated = state.allTransactions.chunked(PAGE_SIZE).getOrNull(newPage - 1) ?: emptyList()
        _uiState.update { it.copy(currentPage = newPage, paginatedTransactions = paginated) }
    }

    fun setDate(newDate: LocalDate) {
        if (_uiState.value.isLoading && _uiState.value.currentDate == newDate) return
        _uiState.update { it.copy(currentDate = newDate, currentPage = 1) }
        fetchTransactions()
    }

    fun nextDay() {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(currentDate = it.currentDate.plusDays(1), currentPage = 1)
        }
        fetchTransactions()
    }

    fun previousDay() {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(currentDate = it.currentDate.minusDays(1), currentPage = 1)
        }
        fetchTransactions()
    }

    fun voidTransaction(transactionId: Long, reason: VoidReason, sessionId: Long) {
        if (_uiState.value.isVoiding) return
        _uiState.update { it.copy(isVoiding = true, voidSuccess = false, voidErrorMessage = null, voidMessage = null) }
        val reasonString = reason.name.lowercase()
        Log.d("VoidPayload", "Sending reason: $reasonString for ID: $transactionId, session: $sessionId")
        viewModelScope.launch {
            val result = repository.voidTransaction(token, transactionId, reasonString, sessionId)
            result.onSuccess { message ->
                _uiState.update {
                    it.copy(
                        isVoiding = false,
                        voidSuccess = true,
                        voidMessage = message,
                        voidErrorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isVoiding = false,
                        voidSuccess = false,
                        voidErrorMessage = sanitizeUserMessage(error.message, "Gagal membatalkan transaksi. Silakan coba lagi.")
                    )
                }
            }
        }
    }

    fun clearVoidState() {
        _uiState.update { it.copy(voidSuccess = false, voidMessage = null, voidErrorMessage = null) }
    }
}
