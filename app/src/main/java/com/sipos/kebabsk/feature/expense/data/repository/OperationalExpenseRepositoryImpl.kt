package com.sipos.kebabsk.feature.expense.data.repository

import com.google.gson.JsonParser
import com.sipos.kebabsk.BuildConfig
import com.sipos.kebabsk.common.retryNetworkRequest
import com.sipos.kebabsk.data.network.ApiPathResolver
import com.sipos.kebabsk.feature.expense.data.remote.OperationalExpenseApiService
import com.sipos.kebabsk.feature.expense.data.remote.OperationalExpenseRequest

class OperationalExpenseRepositoryImpl(
    private val apiService: OperationalExpenseApiService
) {
    private val candidateEndpoints = listOf(
        "operational-expenses",
        "api/operational-expenses",
        "cashflow/operational-expenses",
        "api/cashflow/operational-expenses",
        "cashflow/expenses",
        "api/cashflow/expenses",
        "expenses",
        "api/expenses"
    )

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

            val attempts = mutableListOf<Pair<String, String>>()
            var mostRelevantFailureMessage: String? = null

            candidateEndpoints.forEach { endpoint ->
                val resolvedEndpoint = ApiPathResolver.resolve(BuildConfig.API_BASE_URL, endpoint)
                val response = retryNetworkRequest {
                    apiService.createExpense(
                        authorization = "Bearer $token",
                        url = resolvedEndpoint,
                        body = requestBody
                    )
                }

                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    return@runCatching body.message ?: "Pengeluaran operasional berhasil disimpan."
                }

                val rawError = response.errorBody()?.string()
                val mappedMessage = body?.message ?: mapHttpError(response.code(), rawError)
                attempts += resolvedEndpoint to mappedMessage

                // Prefer non-404 errors when available because they are more actionable.
                if (response.code() != 404) {
                    mostRelevantFailureMessage = mappedMessage
                } else if (mostRelevantFailureMessage == null) {
                    mostRelevantFailureMessage = mappedMessage
                }
            }

            val allNotFound = attempts.isNotEmpty() && attempts.all { (_, message) ->
                message.contains("tidak ditemukan", ignoreCase = true) ||
                    message.contains("belum aktif", ignoreCase = true)
            }

            if (allNotFound) {
                throw IllegalStateException(
                    "Endpoint pengeluaran belum tersedia di server aktif. Hubungi admin untuk deploy update backend."
                )
            }

            throw IllegalStateException(
                mostRelevantFailureMessage ?: "Pengeluaran belum berhasil disimpan."
            )
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
