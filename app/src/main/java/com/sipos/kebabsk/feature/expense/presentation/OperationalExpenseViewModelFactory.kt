package com.sipos.kebabsk.feature.expense.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sipos.kebabsk.feature.expense.data.repository.OperationalExpenseRepositoryImpl

class OperationalExpenseViewModelFactory(
    private val token: String,
    private val repository: OperationalExpenseRepositoryImpl
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OperationalExpenseViewModel::class.java)) {
            return OperationalExpenseViewModel(token, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

