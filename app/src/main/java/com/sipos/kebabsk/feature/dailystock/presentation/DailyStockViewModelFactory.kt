package com.sipos.kebabsk.feature.dailystock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sipos.kebabsk.feature.dailystock.data.repository.DailyStockRepositoryImpl

class DailyStockViewModelFactory(
    private val token: String,
    private val repository: DailyStockRepositoryImpl
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DailyStockViewModel::class.java)) {
            return DailyStockViewModel(token, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

