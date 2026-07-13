package com.sipos.kebabsk.feature.expense.data.repository

import com.google.gson.JsonParser
import com.sipos.kebabsk.common.retryNetworkRequest
import com.sipos.kebabsk.feature.expense.data.remote.OperationalExpenseApiService
import com.sipos.kebabsk.feature.expense.data.remote.OperationalExpenseRequest

class OperationalExpenseRepositoryImpl(
    private val apiService: OperationalExpenseApiService
) {
    suspend fun submitExpense(
        token: String,
        amount: Long,
        source: String,
        note: String?
    ): Result<String> {
        return runCatching {
            val requestBody = OperationalExpenseRequest(
                amount = amount,
                source = source,
                note = note
            )

            val response = retryNetworkRequest {
                apiService.createExpense(
                    authorization = "Bearer $token",
                    body = requestBody
                )
            }

            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                return@runCatching body.message ?: "Pengeluaran operasional berhasil disimpan."
            }

            val rawError = response.errorBody()?.string()
            val mappedMessage = body?.message ?: mapHttpError(response.code(), rawError)
            
            throw IllegalStateException(mappedMessage)
        }
    }

    private fun mapHttpError(code: Int, rawError: String?): String {
        val parsedMessage = parseErrorMessage(rawError)
        if (!parsedMessage.isNullOrBlank()) return parsedMessage

        return when (code) {
            401 -> "Sesi login sudah berakhir. Silakan login ulang."
            404 -> "Fitur pengeluaran belum aktif. Hubungi admin untuk update sistem."
            422 -> "Data pengeluaran belum valid. Periksa nominal dan kategori."
            429 -> "Permintaan terlalu sering. Coba lagi beberapa saat."
            in 500..599 -> "Layanan sedang bermasalah. Silakan coba lagi nanti."
            else -> "Pengeluaran belum berhasil disimpan."
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
