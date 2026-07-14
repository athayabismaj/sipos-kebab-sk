package com.sipos.kebabsk.feature.dailystock.data.repository

import com.sipos.kebabsk.feature.dailystock.domain.model.DailyStockResult
import com.sipos.kebabsk.feature.dailystock.domain.repository.DailyStockRepository

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.sipos.kebabsk.common.retryNetworkRequest
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.common.firstString
import com.sipos.kebabsk.common.firstLong
import com.sipos.kebabsk.common.suspendRunCatching
import com.sipos.kebabsk.feature.dailystock.data.remote.DailyStockApiService
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem

class DailyStockRepositoryImpl(
    private val apiService: DailyStockApiService
) : DailyStockRepository {
    override suspend fun getDailyStock(token: String): Result<DailyStockResult> {
        return suspendRunCatching {
            retryNetworkRequest {
                apiService.getDailyStock("Bearer $token")
            }
        }.fold(
            onSuccess = { response ->
                if (!response.isSuccessful) {
                    return Result.failure(retrofit2.HttpException(response))
                }

                val body = response.body() ?: return Result.failure(IllegalStateException("Empty response body"))
                val sessionId = extractSessionId(body)
                val extracted = extractItems(body)

                if (sessionId == null && extracted.isEmpty()) {
                    return Result.failure(IllegalStateException("Sesi stok harian belum dibuka oleh admin."))
                }

                Result.success(DailyStockResult(sessionId, extracted))
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    override suspend fun closeSession(
        token: String,
        remaining: Map<Long, Double>,
        notes: String?
    ): Result<String> {
        return suspendRunCatching {
            val body = JsonObject().apply {
                val remainingObj = JsonObject()
                remaining.forEach { (ingredientId, qty) ->
                    remainingObj.addProperty(ingredientId.toString(), qty)
                }
                add("remaining", remainingObj)
                if (!notes.isNullOrBlank()) {
                    addProperty("notes", notes)
                }
            }

            val response = retryNetworkRequest {
                apiService.closeSession("Bearer $token", body)
            }

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                val errorMsg = runCatching {
                    com.google.gson.JsonParser.parseString(errorBody)
                        .asJsonObject.get("message")?.asString
                }.getOrNull()
                throw IllegalStateException(
                    mapCloseSessionError(response.code(), errorMsg)
                )
            }

            val responseBody = response.body()
            responseBody?.get("message")?.asString ?: "Sesi stok harian berhasil ditutup."
        }
    }

    private fun extractSessionId(body: JsonObject): Long? {
        body.get("data")?.let { data ->
            if (data.isJsonObject) {
                val dataObj = data.asJsonObject
                dataObj.get("session_id")?.let { sid ->
                    if (!sid.isJsonNull) {
                        runCatching { sid.asLong }.getOrNull()?.let { return it }
                    }
                }
            }
        }
        return null
    }

    private fun extractItems(body: JsonObject): List<DailyStockItem> {
        val collected = mutableListOf<List<DailyStockItem>>()

        val roots = listOf(
            body.get("data"),
            body.get("items"),
            body.get("stocks"),
            body.get("materials"),
            body.get("daily_stock_items"),
            body.get("carried_stock_items")
        )
        roots.forEach { root ->
            val list = parseRoot(root)
            if (list.isNotEmpty()) collected += list
        }

        val nestedData = body.getAsJsonObject("data")
        val nestedCandidates = listOf(
            nestedData?.get("items"),
            nestedData?.get("stocks"),
            nestedData?.get("materials"),
            nestedData?.get("daily_stock_items"),
            nestedData?.get("carried_stock_items"),
            nestedData?.get("session_items"),
            nestedData?.get("stock_items")
        )
        nestedCandidates.forEach { nested ->
            val list = parseRoot(nested)
            if (list.isNotEmpty()) collected += list
        }

        if (collected.isEmpty()) return emptyList()

        // If backend provides multiple roots, prefer the largest parsed list.
        val best = collected.maxByOrNull { it.size }.orEmpty()
        return best
            .distinctBy { item ->
                if (item.ingredientId > 0L) {
                    "id:${item.ingredientId}"
                } else {
                    "name:${item.name.lowercase()}"
                }
            }
    }

    private fun parseRoot(element: JsonElement?): List<DailyStockItem> {
        if (element == null || element.isJsonNull) return emptyList()

        return when {
            element.isJsonArray -> parseArray(element.asJsonArray)
            element.isJsonObject -> {
                val obj = element.asJsonObject
                when {
                    obj.has("data") && obj.get("data").isJsonArray -> parseArray(obj.getAsJsonArray("data"))
                    obj.has("items") && obj.get("items").isJsonArray -> parseArray(obj.getAsJsonArray("items"))
                    obj.has("stocks") && obj.get("stocks").isJsonArray -> parseArray(obj.getAsJsonArray("stocks"))
                    obj.has("materials") && obj.get("materials").isJsonArray -> parseArray(obj.getAsJsonArray("materials"))
                    obj.has("daily_stock_items") && obj.get("daily_stock_items").isJsonArray -> parseArray(obj.getAsJsonArray("daily_stock_items"))
                    obj.has("carried_stock_items") && obj.get("carried_stock_items").isJsonArray -> parseArray(obj.getAsJsonArray("carried_stock_items"))
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }

    private fun parseArray(array: JsonArray): List<DailyStockItem> {
        return array.mapNotNull { row ->
            if (!row.isJsonObject) return@mapNotNull null
            val obj = row.asJsonObject
            val ingredientObj = obj.getAsJsonObject("ingredient")

            val ingredientId = obj.firstLong("ingredient_id", "id")
                ?: ingredientObj?.firstLong("id", "ingredient_id")
                ?: 0L

            val name = obj.firstString("name", "item_name", "material_name", "bahan_name")
                ?: ingredientObj?.firstString("name", "item_name", "material_name", "bahan_name")
                ?: return@mapNotNull null

            val qty = firstDouble(
                obj,
                "qty",
                "quantity",
                "amount",
                "carried_qty",
                "transferred_qty",
                "stock",
                "converted_stock",
                "opening_qty"
            ) ?: ingredientObj?.let {
                firstDouble(
                    it,
                    "qty",
                    "quantity",
                    "amount",
                    "stock",
                    "converted_stock"
                )
            } ?: 0.0

            val remainingQty = firstDouble(
                obj,
                "remaining_qty",
                "sisa_qty",
                "left_qty",
                "current_qty",
                "sisa"
            )

            val unit = obj.firstString(
                "unit",
                "uom",
                "satuan",
                "display_unit",
                "base_unit",
                "display_stock_unit"
            ) ?: ingredientObj?.firstString(
                "unit",
                "uom",
                "satuan",
                "display_unit",
                "base_unit",
                "display_stock_unit"
            )

            DailyStockItem(
                ingredientId = ingredientId,
                name = name.trim(),
                qty = qty,
                remainingQty = remainingQty,
                unit = unit?.trim()?.takeIf { it.isNotBlank() }
            )
        }
    }

    private fun firstDouble(obj: JsonObject, vararg keys: String): Double? {
        keys.forEach { key ->
            val value = obj.get(key)
            if (value != null && !value.isJsonNull) {
                runCatching { value.asDouble }.getOrNull()?.let { return it }
            }
        }
        return null
    }

    private fun mapCloseSessionError(code: Int, rawMessage: String?): String {
        return com.sipos.kebabsk.common.NetworkErrorMapper.mapHttpCodeToUserMessage(
            code,
            sanitizeUserMessage(rawMessage, "Gagal menutup sesi stok harian. Silakan coba lagi."),
            "Sesi stok harian tidak ditemukan.",
            "Data sisa bahan belum valid. Silakan periksa kembali."
        )
    }
}
