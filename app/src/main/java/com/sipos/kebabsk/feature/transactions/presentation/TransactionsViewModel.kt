package com.sipos.kebabsk.feature.transactions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionHistoryItem
import com.sipos.kebabsk.feature.transactions.domain.usecase.GetTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TransactionsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentDate: LocalDate = LocalDate.now(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val paginatedTransactions: List<TransactionHistoryItem> = emptyList()
)

class TransactionsViewModel(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val token: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        fetchTransactions()
    }

    fun fetchTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = getTransactionsUseCase(token, _uiState.value.currentDate, _uiState.value.currentPage)
            result.onSuccess { pageData ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalPages = pageData.totalPages,
                        paginatedTransactions = pageData.items
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
        val currentState = _uiState.value
        if (currentState.currentPage < currentState.totalPages) {
            _uiState.update { it.copy(currentPage = it.currentPage + 1) }
            fetchTransactions()
        }
    }

    fun loadPreviousPage() {
        val currentState = _uiState.value
        if (currentState.currentPage > 1) {
            _uiState.update { it.copy(currentPage = it.currentPage - 1) }
            fetchTransactions()
        }
    }

    fun setDate(newDate: LocalDate) {
        _uiState.update { it.copy(currentDate = newDate, currentPage = 1) }
        fetchTransactions()
    }

    fun nextDay() {
        _uiState.update {
            it.copy(currentDate = it.currentDate.plusDays(1), currentPage = 1)
        }
        fetchTransactions()
    }

    fun previousDay() {
        _uiState.update {
            it.copy(currentDate = it.currentDate.minusDays(1), currentPage = 1)
        }
        fetchTransactions()
    }
}
