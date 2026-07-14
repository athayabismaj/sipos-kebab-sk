package com.sipos.kebabsk.feature.expense.domain.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

class FakeOperationalExpenseRepository : OperationalExpenseRepository {
    var shouldFail = false
    var submitResult = Result.success("Sukses")
    var submitCalls = 0
    var delayMs = 0L
    var cancellation: CancellationException? = null
    var lastAmount: Long? = null
    var lastSource: String? = null
    var lastNote: String? = null

    override suspend fun submitExpense(
        token: String,
        amount: Long,
        source: String,
        note: String?
    ): Result<String> {
        submitCalls += 1
        lastAmount = amount
        lastSource = source
        lastNote = note
        if (delayMs > 0) delay(delayMs)
        cancellation?.let { throw it }
        return if (shouldFail) Result.failure(Exception("Test failure")) else submitResult
    }
}
