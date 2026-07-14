package com.sipos.kebabsk.feature.dailystock.domain.repository

import com.sipos.kebabsk.feature.dailystock.domain.model.DailyStockResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

class FakeDailyStockRepository : DailyStockRepository {
    var shouldFail = false
    var getDailyStockResult = Result.success(DailyStockResult(1L, emptyList()))
    var closeSessionResult = Result.success("Sukses")
    var getDailyStockCalls = 0
    var closeSessionCalls = 0
    var delayMs = 0L
    var cancellation: CancellationException? = null

    override suspend fun getDailyStock(token: String): Result<DailyStockResult> {
        getDailyStockCalls += 1
        if (delayMs > 0) delay(delayMs)
        cancellation?.let { throw it }
        return if (shouldFail) Result.failure(Exception("Test failure")) else getDailyStockResult
    }

    override suspend fun closeSession(
        token: String,
        remaining: Map<Long, Double>,
        notes: String?
    ): Result<String> {
        closeSessionCalls += 1
        if (delayMs > 0) delay(delayMs)
        cancellation?.let { throw it }
        return if (shouldFail) Result.failure(Exception("Test failure")) else closeSessionResult
    }
}
