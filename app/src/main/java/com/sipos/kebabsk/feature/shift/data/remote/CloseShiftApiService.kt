package com.sipos.kebabsk.feature.shift.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface CloseShiftApiService {
    @POST("sessions/{id}/close")
    suspend fun closeSession(
        @Header("Authorization") authorization: String,
        @Path("id") sessionId: Long,
        @Body request: CloseSessionRequest
    ): Response<CloseSessionResponse>
}
