package com.sipos.kebabsk.feature.transactions.domain.usecase

import com.sipos.kebabsk.feature.transactions.domain.model.TransactionHistoryItem
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionPageData
import com.sipos.kebabsk.feature.transactions.domain.repository.TransactionsRepository
import java.time.LocalDate

class GetTransactionsUseCase(
    private val repository: TransactionsRepository
) {
    suspend operator fun invoke(token: String, date: LocalDate, page: Int = 1): Result<TransactionPageData> {
        return repository.getTransactions(token, date, page)
    }
}
