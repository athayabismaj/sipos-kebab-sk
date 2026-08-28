package com.sipos.kebabsk.feature.expense.data.repository

import com.sipos.kebabsk.feature.expense.domain.repository.OperationalExpenseRepository
import com.sipos.kebabsk.common.NetworkErrorMapper
import com.sipos.kebabsk.common.suspendRunCatching

import com.google.gson.JsonParser
import com.sipos.kebabsk.common.retryNetworkRequest
import com.sipos.kebabsk.feature.expense.data.remote.OperationalExpenseApiService
import com.sipos.kebabsk.feature.expense.data.remote.OperationalExpenseRequest

class OperationalExpenseRepositoryImpl(
    private val apiService: OperationalExpenseApiService
) : OperationalExpenseRepository {
    override suspend fun submitExpense(
        token: String,
        amount: Long,
        source: String,
        note: String?
    ): Result<String> = submitExpense(token, amount, source, "CASH_DRAWER", note)

    override suspend fun submitExpense(
        token: String,
        amount: Long,
        source: String,
        paymentSource: String,
        note: String?
    ): Result<String> {
        return suspendRunCatching {
            val requestBody = OperationalExpenseRequest(
                amount = amount,
                source = source,
                paymentSource = paymentSource,
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
                return@suspendRunCatching body.message ?: "Pengeluaran operasional berhasil disimpan."
            }

            val rawError = response.errorBody()?.string()
            val mappedMessage = body?.message ?: mapHttpError(response.code(), rawError)
            
            throw IllegalStateException(mappedMessage)
        }
    }

    private fun mapHttpError(code: Int, rawError: String?): String {
        val parsedMessage = parseErrorMessage(rawError)
        if (!parsedMessage.isNullOrBlank()) return parsedMessage

        return NetworkErrorMapper.mapHttpCodeToUserMessage(
            code,
            "Pengeluaran belum berhasil disimpan.",
            "Fitur pengeluaran belum aktif. Hubungi admin untuk update sistem.",
            "Data pengeluaran belum valid. Periksa nominal dan kategori."
        )
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
