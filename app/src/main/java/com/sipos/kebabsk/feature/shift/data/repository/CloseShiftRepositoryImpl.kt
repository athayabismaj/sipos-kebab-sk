package com.sipos.kebabsk.feature.shift.data.repository

import com.sipos.kebabsk.feature.shift.domain.repository.CloseShiftRepository
import com.sipos.kebabsk.feature.shift.data.remote.CloseSessionData
import com.sipos.kebabsk.feature.shift.data.remote.CloseShiftApiService
import com.sipos.kebabsk.feature.shift.data.remote.CloseSessionRequest
import com.sipos.kebabsk.common.retryNetworkRequest
import com.sipos.kebabsk.common.NetworkErrorMapper
import com.sipos.kebabsk.common.suspendRunCatching
import com.google.gson.JsonParser

class CloseShiftRepositoryImpl(
    private val apiService: CloseShiftApiService
) : CloseShiftRepository {
    override suspend fun closeSession(
        token: String,
        sessionId: Long,
        actualPhysicalCash: Long,
        closingNotes: String?
    ): Result<CloseSessionData> {
        return suspendRunCatching {
            val request = CloseSessionRequest(
                actualPhysicalCash = actualPhysicalCash,
                closingNotes = closingNotes
            )

            val response = retryNetworkRequest {
                apiService.closeSession(
                    authorization = "Bearer $token",
                    sessionId = sessionId,
                    request = request
                )
            }

            val body = response.body()
            if (response.isSuccessful && body != null) {
                return@suspendRunCatching body.data
            }

            val rawError = response.errorBody()?.string()
            val parsedMessage = parseErrorMessage(rawError)
            
            val mappedMessage = parsedMessage ?: NetworkErrorMapper.mapHttpCodeToUserMessage(
                response.code(),
                "Gagal menutup shift. Silakan coba lagi.",
                "Sesi shift tidak ditemukan.",
                "Data rekonsiliasi kas tidak valid."
            )
            
            throw IllegalStateException(mappedMessage)
        }
    }

    private fun parseErrorMessage(rawError: String?): String? {
        if (rawError.isNullOrBlank()) return null
        return runCatching {
            val json = JsonParser.parseString(rawError).asJsonObject
            val message = json.get("message")?.asString
            if (!message.isNullOrBlank()) {
                message
            } else {
                null
            }
        }.getOrNull()
    }
}
