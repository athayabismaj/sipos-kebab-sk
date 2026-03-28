package com.sipos.kebabsk.feature.transactions.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sipos.kebabsk.feature.transactions.data.remote.TransactionItemResponse
import com.sipos.kebabsk.feature.transactions.data.remote.TransactionsApiService
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionHistoryItem
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionPageData
import com.sipos.kebabsk.feature.transactions.domain.repository.TransactionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionsRepositoryImpl(
    private val apiService: TransactionsApiService
) : TransactionsRepository {

    override suspend fun getTransactions(token: String, date: LocalDate, page: Int): Result<TransactionPageData> {
        return try {
            // Because backend uses custom format: \Carbon\Carbon::parse($transaction->created_at)->isoFormat('D MMMM Y HH:mm')
            // Example output: "9 Maret 2026 14:30"
            // Screenshot shows format like "09 Mar 2026 01:04"
            val formattedQueryDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val response = apiService.getTransactions("Bearer $token", formattedQueryDate, page)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    
                    return withContext(Dispatchers.Default) {
                        try {
                            // Flexibly handle "data" being either a JSON Array or a JSON Object (Laravel pagination)
                            val rawDataElement = body.data
                            val listType = object : TypeToken<List<TransactionItemResponse>>() {}.type
                            val gson = Gson()
                            
                            var lastPage = 1
                            val parsedList: List<TransactionItemResponse> = try {
                                if (rawDataElement.isJsonArray) {
                                    gson.fromJson(rawDataElement, listType)
                                } else if (rawDataElement.isJsonObject) {
                                    // Backend might be returning paginated object: { "current_page": 1, "last_page": 2, "data": [...] }
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
                            } catch (e: Exception) {
                                emptyList()
                            }

                            val items = parsedList.mapIndexed { index, itemResponse ->
                                // Generate TRX-0001 format based on daily index.
                                // Assuming API returns newest first (descending), the oldest is at the end.
                                // So index 0 (newest) gets the highest number.
                                val chronologicalNumber = parsedList.size - index
                                val generatedCode = "TRX-" + String.format("%04d", chronologicalNumber)

                                // Parse time regardless of format
                                val timeString = try {
                                    val createdAtStr = itemResponse.createdAt ?: ""
                                    if (createdAtStr.contains("T")) {
                                        // Probably ISO-8601 like 2026-03-09T14:30:00
                                        val timePart = createdAtStr.substringAfter("T").substringBefore(".")
                                        val timeParts = timePart.split(":")
                                        if (timeParts.size >= 2) "${timeParts[0]}:${timeParts[1]}" else "00:00"
                                    } else {
                                        // Probably D MMMM Y HH:mm format
                                        val parts = createdAtStr.split(" ")
                                        if (parts.size >= 2) {
                                            val timeCandidate = parts.last()
                                            if (timeCandidate.contains(":")) timeCandidate else "00:00"
                                        } else {
                                            "00:00"
                                        }
                                    }
                                } catch (e: Exception) {
                                    "00:00" // Fallback if parsing fails
                                }

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

                            Result.success(TransactionPageData(items, lastPage))
                        } catch (e: Exception) {
                            Result.failure(e)
                        }
                    }
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal memuat data transaksi"))
                }
            } else {
                Result.failure(Exception("HTTP Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRevenueSummary(token: String, date: LocalDate): Result<Pair<Double, Int>> {
        return try {
            val formattedQueryDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val response = apiService.getRevenueSummary("Bearer $token", formattedQueryDate)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    val totalRevenue = body.data.totalRevenue ?: 0.0
                    val totalCount = body.data.totalCount ?: 0
                    Result.success(Pair(totalRevenue, totalCount))
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal mengambil data ringkasan"))
                }
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRevenueTrend(token: String, date: LocalDate): Result<List<Pair<String, Double>>> {
        return try {
            val formattedQueryDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val response = apiService.getRevenueTrend("Bearer $token", formattedQueryDate)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    val trendList = body.data.map { 
                        Pair(it.date ?: "", it.totalRevenue ?: 0.0)
                    }
                    Result.success(trendList)
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal mengambil data tren pendapatan"))
                }
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
