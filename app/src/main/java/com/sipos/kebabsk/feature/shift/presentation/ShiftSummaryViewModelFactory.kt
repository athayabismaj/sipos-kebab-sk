package com.sipos.kebabsk.feature.shift.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sipos.kebabsk.feature.transactions.domain.repository.TransactionsRepository

class ShiftSummaryViewModelFactory(
    private val token: String,
    private val repository: TransactionsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShiftSummaryViewModel::class.java)) {
            return ShiftSummaryViewModel(token, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

