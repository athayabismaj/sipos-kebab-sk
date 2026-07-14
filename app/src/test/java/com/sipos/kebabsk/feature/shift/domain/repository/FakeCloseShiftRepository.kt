package com.sipos.kebabsk.feature.shift.domain.repository

import com.sipos.kebabsk.feature.shift.data.remote.CloseSessionData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

class FakeCloseShiftRepository : CloseShiftRepository {
    var shouldFail = false
    var closeSessionResult = Result.success(CloseSessionData(0L, 0L, 0L))
    var closeSessionCalls = 0
    var delayMs = 0L
    var cancellation: CancellationException? = null

    override suspend fun closeSession(
        token: String,
        sessionId: Long,
        actualPhysicalCash: Long,
        closingNotes: String?
    ): Result<CloseSessionData> {
        closeSessionCalls += 1
        if (delayMs > 0) delay(delayMs)
        cancellation?.let { throw it }
        return if (shouldFail) Result.failure(Exception("Test failure")) else closeSessionResult
    }
}
