package com.sipos.kebabsk.feature.shift.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sipos.kebabsk.feature.dailystock.data.repository.DailyStockRepositoryImpl
import com.sipos.kebabsk.feature.shift.data.remote.CloseShiftApiService

class CloseShiftViewModelFactory(
    private val token: String,
    private val closeShiftApiService: CloseShiftApiService,
    private val dailyStockRepository: DailyStockRepositoryImpl,
    private val targetSessionId: Long? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CloseShiftViewModel::class.java)) {
            return CloseShiftViewModel(token, closeShiftApiService, dailyStockRepository, targetSessionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
