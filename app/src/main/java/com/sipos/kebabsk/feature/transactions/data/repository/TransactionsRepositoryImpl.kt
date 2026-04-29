package com.sipos.kebabsk.feature.transactions.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sipos.kebabsk.common.mapHttpCodeToUserMessage
import com.sipos.kebabsk.common.mapThrowableToUserMessage
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.common.retryNetworkRequest
import com.sipos.kebabsk.feature.transactions.data.remote.TransactionItemResponse
import com.sipos.kebabsk.feature.transactions.data.remote.TransactionsApiService
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionHistoryItem
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionPageData
import com.sipos.kebabsk.feature.transactions.domain.model.RevenueSummaryResult
import com.sipos.kebabsk.feature.transactions.domain.repository.TransactionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionsRepositoryImpl(
    private val apiService: TransactionsApiService
) : TransactionsRepository {

    override suspend fun getTransactions(token: String, date: LocalDate, page: Int): Result<TransactionPageData> {
        return runCatching {
            val formattedQueryDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val response = retryNetworkRequest(maxAttempts = 1) {
                apiService.getTransactions("Bearer $token", formattedQueryDate, page)
            }

            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.data == null) {
                throw IllegalStateException(
                    mapHttpFailure(
                        code = response.code(),
                        rawMessage = body?.message,
                        fallback = "Riwayat transaksi belum bisa dimuat. Silakan coba lagi."
                    )
                )
            }

            withContext(Dispatchers.Default) {
                val rawDataElement = body.data
                val listType = object : TypeToken<List<TransactionItemResponse>>() {}.type
                val gson = Gson()

                var lastPage = 1
                val parsedList: List<TransactionItemResponse> = try {
                    if (rawDataElement.isJsonArray) {
                        gson.fromJson(rawDataElement, listType)
                    } else if (rawDataElement.isJsonObject) {
                        val dataObj = rawDataElement.asJsonObject
                        if (dataObj.has("last_page")) {
                            lastPage = dataObj.get("last_page").asInt
                        }
                        if (dataObj.has("data") && dataObj.get("data").isJsonArray) {
                            gson.fromJson(dataObj.get("data"), listType)
                        } else {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                } catch (_: Exception) {
                    emptyList()
                }

                val items = parsedList.map { itemResponse ->
                    val timeString = parseTransactionTime(itemResponse.createdAt)
                    TransactionHistoryItem(
                        id = itemResponse.id ?: 0L,
                        code = itemResponse.transactionCode ?: "TRX-UNKNOWN",
                        time = timeString,
                        itemCount = itemResponse.itemsCount ?: 0,
                        total = itemResponse.totalAmount ?: 0.0,
                        status = itemResponse.status ?: "Unknown",
                        originalDate = itemResponse.createdAt ?: ""
                    )
                }

                TransactionPageData(items, lastPage)
            }
        }.recoverCatching { throwable ->
            throw IllegalStateException(
                mapThrowable(
                    throwable = throwable,
                    fallback = "Riwayat transaksi belum bisa dimuat. Silakan coba lagi."
                )
            )
        }
    }

    override suspend fun getRevenueSummary(token: String, date: LocalDate): Result<RevenueSummaryResult> {
        return runCatching {
            val formattedQueryDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val response = retryNetworkRequest {
                apiService.getRevenueSummary("Bearer $token", formattedQueryDate)
            }

            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.data == null) {
                throw IllegalStateException(
                    mapHttpFailure(
                        code = response.code(),
                        rawMessage = body?.message,
                        fallback = "Ringkasan omzet belum bisa dimuat. Silakan coba lagi."
                    )
                )
            }

            RevenueSummaryResult(
                totalRevenue = body.data.totalRevenue ?: 0.0,
                totalCount = body.data.totalCount ?: 0,
                transactionGrowthPercentage = body.data.transactionGrowthPercentage,
                dominantItemName = body.data.dominantItemName,
                revenueTargetPercentage = body.data.revenueTargetPercentage,
                dailyTargetRevenue = body.data.dailyTargetRevenue
            )
        }.recoverCatching { throwable ->
            throw IllegalStateException(
                mapThrowable(
                    throwable = throwable,
                    fallback = "Ringkasan omzet belum bisa dimuat. Silakan coba lagi."
                )
            )
        }
    }

    override suspend fun getRevenueTrend(token: String, date: LocalDate): Result<List<Pair<String, Double>>> {
        return runCatching {
            val formattedQueryDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val response = retryNetworkRequest {
                apiService.getRevenueTrend("Bearer $token", formattedQueryDate)
            }

            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.data == null) {
                throw IllegalStateException(
                    mapHttpFailure(
                        code = response.code(),
                        rawMessage = body?.message,
                        fallback = "Grafik omzet belum bisa dimuat. Silakan coba lagi."
                    )
                )
            }

            body.data.map { Pair(it.date ?: "", it.totalRevenue ?: 0.0) }
        }.recoverCatching { throwable ->
            throw IllegalStateException(
                mapThrowable(
                    throwable = throwable,
                    fallback = "Grafik omzet belum bisa dimuat. Silakan coba lagi."
                )
            )
        }
    }

    private fun parseTransactionTime(createdAt: String?): String {
        if (createdAt.isNullOrBlank()) return "00:00"
        return try {
            if (createdAt.contains("T")) {
                val timePart = createdAt.substringAfter("T").substringBefore(".")
                val timeParts = timePart.split(":")
                if (timeParts.size >= 2) "${timeParts[0]}:${timeParts[1]}" else "00:00"
            } else {
                val parts = createdAt.split(" ")
                if (parts.size >= 2) {
                    val timeCandidate = parts.last()
                    if (timeCandidate.contains(":")) timeCandidate else "00:00"
                } else {
                    "00:00"
                }
            }
        } catch (_: Exception) {
            "00:00"
        }
    }

    private fun mapHttpFailure(code: Int, rawMessage: String?, fallback: String): String {
        val httpMapped = mapHttpCodeToUserMessage(code, fallback)
        return if (httpMapped == fallback) {
            sanitizeUserMessage(rawMessage, fallback)
        } else {
            httpMapped
        }
    }

    private fun mapThrowable(throwable: Throwable, fallback: String): String {
        return mapThrowableToUserMessage(throwable, fallback)
    }
}
