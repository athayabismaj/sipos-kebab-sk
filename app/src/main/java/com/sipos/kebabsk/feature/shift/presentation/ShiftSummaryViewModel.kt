package com.sipos.kebabsk.feature.shift.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.AppSessionStore
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.feature.transactions.domain.repository.TransactionsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShiftSummaryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalTransactions: Int = 0,
    val totalItemsSold: Int = 0,
    val totalRevenue: Long = 0L,
    val transactionGrowthPercentage: Double? = null,
    val dominantItemName: String? = null,
    val revenueTargetPercentage: Double? = null,
    val dailyTargetRevenue: Long? = null
)

class ShiftSummaryViewModel(
    private val repository: TransactionsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShiftSummaryUiState())
    val uiState: StateFlow<ShiftSummaryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val token = AppSessionStore.loadSession()?.token ?: ""
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val today = AppTime.todayJakarta()

        viewModelScope.launch {
            val summaryDeferred = async { repository.getRevenueSummary(token, today) }
            val pageOneDeferred = async { repository.getTransactions(token, today, 1) }

            val summaryResult = summaryDeferred.await()
            val pageOneResult = pageOneDeferred.await()

            val summary = summaryResult.getOrNull()
            val totalRevenue = summary?.totalRevenue ?: 0L
            val totalTransactions = summary?.totalCount ?: 0

            var totalItemsSold = 0
            var firstFailure: Throwable? = summaryResult.exceptionOrNull()

            pageOneResult
                .onSuccess { pageData ->
                    totalItemsSold += pageData.items.sumOf { it.itemCount }
                    if (pageData.totalPages > 1) {
                        for (page in 2..pageData.totalPages) {
                            val pageResult = repository.getTransactions(token, today, page)
                            pageResult
                                .onSuccess { nextPage ->
                                    totalItemsSold += nextPage.items.sumOf { it.itemCount }
                                }
                                .onFailure { error ->
                                    if (firstFailure == null) firstFailure = error
                                }
                        }
                    }
                }
                .onFailure { error ->
                    if (firstFailure == null) firstFailure = error
                }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    totalRevenue = totalRevenue,
                    totalTransactions = totalTransactions,
                    totalItemsSold = totalItemsSold,
                    transactionGrowthPercentage = summary?.transactionGrowthPercentage,
                    dominantItemName = summary?.dominantItemName,
                    revenueTargetPercentage = summary?.revenueTargetPercentage,
                    dailyTargetRevenue = summary?.dailyTargetRevenue,
                    errorMessage = firstFailure?.let { throwable ->
                        sanitizeUserMessage(
                            throwable.message,
                            "Ringkasan shift belum bisa dimuat. Silakan coba lagi."
                        )
                    }
                )
            }
        }
    }
}

