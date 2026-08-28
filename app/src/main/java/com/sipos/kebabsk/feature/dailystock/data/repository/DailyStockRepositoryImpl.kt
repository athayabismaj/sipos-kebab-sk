package com.sipos.kebabsk.feature.dailystock.data.repository

import com.sipos.kebabsk.feature.dailystock.domain.model.DailyStockResult
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipeAnchorInput
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipeAffectedIngredient
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipeGroup
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipeGroupVariant
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipePreset
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipePreview
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipePreviewItem
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipeSummary
import com.sipos.kebabsk.feature.dailystock.domain.model.CashReconciliation
import com.sipos.kebabsk.feature.dailystock.domain.repository.DailyStockRepository

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.sipos.kebabsk.common.NetworkErrorMapper
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
    override suspend fun closeSession(
        token: String,
        remaining: Map<Long, Double>,
        notes: String?
    ): Result<String> = closeSession(token, remaining, notes, 0L)

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
                val data = body.getAsJsonObject("data")
                val overdueSession = data?.get("overdue_session")
                    ?.takeIf { it.isJsonObject }
                    ?.asJsonObject
                val businessDate = data?.firstString("business_date", "session_date")
                    ?: overdueSession?.firstString("business_date", "session_date")
                val cutoffTime = data?.firstString("cutoff_time", "closing_grace_until")
                    ?: overdueSession?.firstString("cutoff_time", "closing_grace_until")
                val overdue = runCatching {
                    data?.get("overdue")?.asBoolean ?: overdueSession?.get("overdue")?.asBoolean
                }.getOrNull() ?: false
                val canClose = runCatching {
                    data?.get("can_close")?.asBoolean ?: overdueSession?.get("can_close")?.asBoolean
                }.getOrNull() ?: (sessionId != null && !overdue)
                val statusMessage = if (overdue) {
                    "Sesi operasional ${businessDate.orEmpty()} telah melewati batas penyelesaian pukul ${cutoffTime ?: "06:00"} WIB. Hubungi admin/owner."
                } else {
                    null
                }

                if (sessionId == null && extracted.isEmpty() && overdueSession == null) {
                    return Result.failure(IllegalStateException("Sesi stok harian belum dibuka oleh admin."))
                }

                Result.success(
                    DailyStockResult(
                        sessionId = sessionId,
                        items = extracted,
                        businessDate = businessDate,
                        cutoffTime = cutoffTime,
                        canClose = canClose,
                        overdue = overdue,
                        statusMessage = statusMessage,
                        closingPresets = extractClosingPresets(body),
                        closingGroups = extractClosingGroups(body)
                    )
                )
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    override suspend fun closeSession(
        token: String,
        remaining: Map<Long, Double>,
        notes: String?,
        actualCash: Long
    ): Result<String> {
        return suspendRunCatching {
            val body = JsonObject().apply {
                val remainingObj = JsonObject()
                remaining.forEach { (ingredientId, qty) ->
                    remainingObj.addProperty(ingredientId.toString(), qty)
                }
                add("remaining", remainingObj)
                addProperty("actual_cash", actualCash)
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

    override suspend fun getCashReconciliation(token: String): Result<CashReconciliation> =
        suspendRunCatching {
            val response = retryNetworkRequest {
                apiService.getCashReconciliation("Bearer $token")
            }
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    extractErrorMessage(response, "Rekonsiliasi kas belum bisa dihitung.")
                )
            }

            val data = response.body()?.getAsJsonObject("data")
                ?: throw IllegalStateException("Respons rekonsiliasi kas tidak lengkap.")
            CashReconciliation(
                sessionId = data.firstLong("session_id")
                    ?: throw IllegalStateException("Sesi rekonsiliasi tidak ditemukan."),
                businessDate = data.firstString("business_date"),
                openingCash = data.firstLong("opening_cash") ?: 0L,
                cashSales = data.firstLong("cash_sales") ?: 0L,
                cashExpenses = data.firstLong("cash_expenses") ?: 0L,
                expectedCash = data.firstLong("expected_cash") ?: 0L
            )
        }

    override suspend fun previewClosing(
        token: String,
        anchors: List<ClosingRecipeAnchorInput>
    ): Result<ClosingRecipePreview> = suspendRunCatching {
        val response = retryNetworkRequest {
            apiService.previewClosing("Bearer $token", JsonObject().apply {
                add("closing_anchors", anchorsJson(anchors))
            })
        }
        if (!response.isSuccessful) {
            throw IllegalStateException(extractErrorMessage(response, "Perhitungan resep gagal."))
        }
        parseClosingPreview(response.body() ?: throw IllegalStateException("Respons preview kosong."))
    }

    override suspend fun closeSessionWithRecipe(
        token: String,
        remainingOverrides: Map<Long, Double>,
        anchors: List<ClosingRecipeAnchorInput>,
        notes: String?,
        idempotencyKey: String
    ): Result<String> = closeSessionWithRecipe(
        token,
        remainingOverrides,
        anchors,
        notes,
        idempotencyKey,
        0L
    )

    override suspend fun closeSessionWithRecipe(
        token: String,
        remainingOverrides: Map<Long, Double>,
        anchors: List<ClosingRecipeAnchorInput>,
        notes: String?,
        idempotencyKey: String,
        actualCash: Long
    ): Result<String> = suspendRunCatching {
        val body = JsonObject().apply {
            add("closing_anchors", anchorsJson(anchors))
            add("remaining_overrides", JsonObject().also { overrides ->
                remainingOverrides.forEach { (id, value) -> overrides.addProperty(id.toString(), value) }
            })
            addProperty("idempotency_key", idempotencyKey)
            addProperty("actual_cash", actualCash)
            if (!notes.isNullOrBlank()) addProperty("notes", notes)
        }
        val response = retryNetworkRequest { apiService.closeSession("Bearer $token", body) }
        if (!response.isSuccessful) {
            throw IllegalStateException(extractErrorMessage(response, "Gagal menutup sesi stok harian."))
        }
        response.body()?.get("message")?.asString ?: "Sesi stok harian berhasil ditutup."
    }

    private fun anchorsJson(anchors: List<ClosingRecipeAnchorInput>) = JsonArray().apply {
        anchors.forEach { anchor ->
            add(JsonObject().apply {
                addProperty("menu_variant_id", anchor.menuVariantId)
                addProperty("actual_remaining", anchor.actualRemaining)
                anchor.allocatedQuantity?.let {
                    addProperty("allocated_quantity", it)
                }
            })
        }
    }

    private fun extractErrorMessage(response: retrofit2.Response<JsonObject>, fallback: String): String {
        val raw = runCatching { response.errorBody()?.string() }.getOrNull()
        return runCatching {
            com.google.gson.JsonParser.parseString(raw).asJsonObject.get("message")?.asString
        }.getOrNull()?.let { sanitizeUserMessage(it, fallback) } ?: fallback
    }

    private fun extractClosingPresets(body: JsonObject): List<ClosingRecipePreset> {
        val rows = body.getAsJsonObject("data")?.getAsJsonArray("closing_presets") ?: return emptyList()
        return rows.mapNotNull { element ->
            val obj = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val variantId = obj.firstLong("menu_variant_id") ?: return@mapNotNull null
            val missing = obj.getAsJsonArray("missing_ingredients")?.mapNotNull {
                runCatching { it.asString }.getOrNull()
            }.orEmpty()
            ClosingRecipePreset(
                menuVariantId = variantId,
                label = obj.firstString("label") ?: "Menu",
                anchorIngredientId = obj.firstLong("anchor_ingredient_id") ?: 0L,
                anchorName = obj.firstString("anchor_name") ?: "Bahan acuan",
                anchorUnit = obj.firstString("anchor_unit") ?: "pcs",
                systemRemaining = firstDouble(obj, "system_remaining") ?: 0.0,
                quantityPerServing = firstDouble(obj, "quantity_per_serving") ?: 1.0,
                ready = runCatching { obj.get("ready")?.asBoolean }.getOrNull() ?: false,
                missingIngredients = missing
            )
        }
    }

    private fun extractClosingGroups(body: JsonObject): List<ClosingRecipeGroup> {
        val rows = body.getAsJsonObject("data")?.getAsJsonArray("closing_groups") ?: return emptyList()
        return rows.mapNotNull { element ->
            val obj = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val groupId = obj.firstLong("group_id") ?: return@mapNotNull null
            val anchorIngredientId = obj.firstLong("anchor_ingredient_id") ?: return@mapNotNull null
            val variants = obj.getAsJsonArray("variants")?.mapNotNull variantMap@{ variantElement ->
                val variant = variantElement.takeIf { it.isJsonObject }?.asJsonObject
                    ?: return@variantMap null
                ClosingRecipeGroupVariant(
                    menuVariantId = variant.firstLong("menu_variant_id") ?: return@variantMap null,
                    label = variant.firstString("label") ?: "Varian menu",
                    anchorQuantity = firstDouble(variant, "anchor_quantity") ?: 1.0,
                    isDefault = runCatching { variant.get("is_default")?.asBoolean }.getOrNull() ?: false
                )
            }.orEmpty()
            if (variants.isEmpty()) return@mapNotNull null

            val configuredDefault = obj.firstLong("default_menu_variant_id") ?: 0L
            val defaultVariantId = configuredDefault.takeIf { id -> variants.any { it.menuVariantId == id } }
                ?: variants.firstOrNull { it.isDefault }?.menuVariantId
                ?: variants.first().menuVariantId

            ClosingRecipeGroup(
                groupId = groupId,
                label = obj.firstString("label") ?: obj.firstString("anchor_name") ?: "Bahan pemicu",
                anchorIngredientId = anchorIngredientId,
                anchorName = obj.firstString("anchor_name") ?: "Bahan pemicu",
                anchorUnit = obj.firstString("anchor_unit") ?: "pcs",
                systemRemaining = firstDouble(obj, "system_remaining") ?: 0.0,
                defaultMenuVariantId = defaultVariantId,
                requiresAllocation = runCatching { obj.get("requires_allocation")?.asBoolean }.getOrNull()
                    ?: (variants.size > 1),
                ready = runCatching { obj.get("ready")?.asBoolean }.getOrNull() ?: false,
                variants = variants
            )
        }
    }

    private fun parseClosingPreview(body: JsonObject): ClosingRecipePreview {
        val data = body.getAsJsonObject("data") ?: JsonObject()
        val items = data.getAsJsonArray("remaining_items")?.mapNotNull { element ->
            val obj = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            ClosingRecipePreviewItem(
                ingredientId = obj.firstLong("ingredient_id") ?: return@mapNotNull null,
                name = obj.firstString("name") ?: "Bahan",
                remainingQty = firstDouble(obj, "remaining_qty") ?: 0.0,
                autoUsedQty = firstDouble(obj, "auto_used_qty") ?: 0.0,
                unit = obj.firstString("unit") ?: "unit"
            )
        }.orEmpty()
        val summaries = data.getAsJsonArray("summaries")?.mapNotNull { element ->
            val obj = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val anchorIngredientId = obj.firstLong("anchor_ingredient_id")
            val affectedRows = obj.getAsJsonArray("affected_ingredients")
                ?: obj.getAsJsonArray("usage")
            val affectedIngredients = affectedRows?.mapNotNull affectedMap@{ affectedElement ->
                val affected = affectedElement.takeIf { it.isJsonObject }?.asJsonObject
                    ?: return@affectedMap null
                ClosingRecipeAffectedIngredient(
                    ingredientId = affected.firstLong("ingredient_id") ?: return@affectedMap null,
                    name = affected.firstString("name") ?: "Bahan",
                    usedQty = firstDouble(affected, "used_qty") ?: 0.0,
                    unit = affected.firstString("unit") ?: "unit"
                )
            }.orEmpty()
            ClosingRecipeSummary(
                menuVariantId = obj.firstLong("menu_variant_id") ?: return@mapNotNull null,
                label = obj.firstString("label") ?: "Menu",
                inferredServings = firstDouble(obj, "inferred_servings")?.toInt() ?: 0,
                anchorIngredientId = anchorIngredientId,
                affectedIngredients = affectedIngredients.filter {
                    anchorIngredientId == null || it.ingredientId != anchorIngredientId
                }
            )
        }.orEmpty()
        return ClosingRecipePreview(items, summaries)
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
        return NetworkErrorMapper.mapHttpCodeToUserMessage(
            code,
            sanitizeUserMessage(rawMessage, "Gagal menutup sesi stok harian. Silakan coba lagi."),
            "Sesi stok harian tidak ditemukan.",
            "Data sisa bahan belum valid. Silakan periksa kembali."
        )
    }
}
