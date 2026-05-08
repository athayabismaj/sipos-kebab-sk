package com.sipos.kebabsk.feature.transactions.presentation

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

data class TransactionsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentDate: LocalDate = AppTime.todayJakarta(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val allTransactions: List<TransactionHistoryItem> = emptyList(),
    val paginatedTransactions: List<TransactionHistoryItem> = emptyList()
)

class TransactionsViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
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
            // Always fetch from page 1 to get all data, then paginate client-side
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
                        allTransactions = allItems,
                        totalPages = totalPages,
                        currentPage = safePage,
                        paginatedTransactions = paginated
                    )
                }
            }.onFailure { error ->
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
}
