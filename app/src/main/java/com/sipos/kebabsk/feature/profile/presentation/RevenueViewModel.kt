package com.sipos.kebabsk.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.feature.transactions.domain.repository.TransactionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import java.time.LocalDate

data class RevenueUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val revenueAmount: Double = 0.0,
    val transactionCount: Int = 0,
    val trendData: List<Pair<String, Double>> = emptyList(),
    val errorMessage: String? = null
)

class RevenueViewModel(
    private val token: String,
    private val repository: TransactionsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevenueUiState())
    val uiState: StateFlow<RevenueUiState> = _uiState.asStateFlow()

    init {
        loadRevenueData()
    }

    fun setDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadRevenueData()
    }

    fun refresh() {
        loadRevenueData()
    }

    private fun loadRevenueData() {
        val currentDate = _uiState.value.selectedDate
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val summaryDeferred = async { repository.getRevenueSummary(token, currentDate) }
                val trendDeferred = async { repository.getRevenueTrend(token, currentDate) }
                
                val summaryResult = summaryDeferred.await()
                val trendResult = trendDeferred.await()
                
                if (summaryResult.isSuccess && trendResult.isSuccess) {
                    val (totalRevenue, totalCount) = summaryResult.getOrNull()!!
                    val trendList = trendResult.getOrNull()!!
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            revenueAmount = totalRevenue,
                            transactionCount = totalCount,
                            trendData = trendList
                        )
                    }
                } else {
                    val error = (summaryResult.exceptionOrNull() ?: trendResult.exceptionOrNull()) as? Throwable
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = error?.message ?: "Gagal mengambil data omset dan tren",
                            revenueAmount = 0.0,
                            transactionCount = 0,
                            trendData = emptyList()
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = e.message ?: "Terjadi kesalahan tak terduga",
                        revenueAmount = 0.0,
                        transactionCount = 0,
                        trendData = emptyList()
                    ) 
                }
            }
        }
    }
}
