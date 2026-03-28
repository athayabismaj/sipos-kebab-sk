package com.sipos.kebabsk.feature.transactions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sipos.kebabsk.data.network.NetworkModule
import com.sipos.kebabsk.feature.transactions.data.repository.TransactionsRepositoryImpl
import com.sipos.kebabsk.feature.transactions.domain.usecase.GetTransactionsUseCase

class TransactionsViewModelFactory(private val token: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            val repository = TransactionsRepositoryImpl(NetworkModule.transactionsApiService)
            val useCase = GetTransactionsUseCase(repository)
            @Suppress("UNCHECKED_CAST")
            return TransactionsViewModel(useCase, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
