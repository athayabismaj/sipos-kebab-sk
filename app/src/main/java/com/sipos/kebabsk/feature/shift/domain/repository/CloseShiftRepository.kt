package com.sipos.kebabsk.feature.shift.domain.repository

import com.sipos.kebabsk.feature.shift.data.remote.CloseSessionData

interface CloseShiftRepository {
    suspend fun closeSession(
        token: String,
        sessionId: Long,
        actualPhysicalCash: Long,
        closingNotes: String?
    ): Result<CloseSessionData>
}
