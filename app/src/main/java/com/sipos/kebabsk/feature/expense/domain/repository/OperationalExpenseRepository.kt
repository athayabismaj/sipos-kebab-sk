package com.sipos.kebabsk.feature.expense.domain.repository

interface OperationalExpenseRepository {
    suspend fun submitExpense(
        token: String,
        amount: Long,
        source: String,
        note: String?
    ): Result<String>

    suspend fun submitExpense(
        token: String,
        amount: Long,
        source: String,
        paymentSource: String,
        note: String?
    ): Result<String> = submitExpense(token, amount, source, note)
}
