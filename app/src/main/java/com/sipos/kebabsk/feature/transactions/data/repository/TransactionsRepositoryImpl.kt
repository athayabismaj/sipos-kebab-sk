package com.sipos.kebabsk.feature.transactions.data.repository

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.common.retryNetworkRequest
import com.sipos.kebabsk.common.suspendRunCatching
import com.sipos.kebabsk.common.validation.safeMultiply
import com.sipos.kebabsk.common.validation.safeSubtract
import com.sipos.kebabsk.feature.transactions.data.remote.TransactionItemResponse
import com.sipos.kebabsk.feature.transactions.data.remote.TransactionsApiService
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionHistoryItem
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionPageData
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionReceipt
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionReceiptItem
import com.sipos.kebabsk.feature.transactions.domain.model.RevenueSummaryResult
import com.sipos.kebabsk.feature.transactions.domain.repository.TransactionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class TransactionsRepositoryImpl(
    private val apiService: TransactionsApiService
) : TransactionsRepository {

    override suspend fun getTransactions(token: String, date: LocalDate, page: Int): Result<TransactionPageData> {
        return suspendRunCatching {
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
                        total = itemResponse.totalAmount ?: 0L,
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

    override suspend fun getTransactionReceipt(
        token: String,
        transactionId: Long,
        transactionCode: String?
    ): Result<TransactionReceipt> {
        return suspendRunCatching {
            val references = listOfNotNull(
                transactionId.toString(),
                transactionCode?.trim()?.takeIf { it.isNotBlank() }
            ).distinct()

            var lastCode = 0
            var lastMessage: String? = null
            var lastFailure: Throwable? = null

            for (reference in references) {
                val candidates = listOf(
                    suspendRunCatching {
                        retryNetworkRequest(maxAttempts = 1) {
                            apiService.getTransactionDetail("Bearer $token", reference)
                        }
                    },
                    suspendRunCatching {
                        retryNetworkRequest(maxAttempts = 1) {
                            apiService.getTransactionReceiptDetail("Bearer $token", reference)
                        }
                    }
                )

                candidates.forEach { result ->
                    result
                        .onSuccess { response ->
                            lastCode = response.code()
                            val body = response.body()
                            lastMessage = body.extractApiMessage()

                            if (response.isSuccessful) {
                                val parsed = runCatching {
                        parseTransactionReceiptBody(body, transactionId)
                    }
                    if (parsed.isSuccess) {
                        return@suspendRunCatching parsed.getOrThrow()
                    }
                                lastFailure = parsed.exceptionOrNull()
                            }
                        }
                        .onFailure { throwable ->
                            lastFailure = throwable
                        }
                }
            }

            throw IllegalStateException(
                lastFailure?.message?.takeIf { it.isNotBlank() }
                    ?: mapHttpFailure(
                        code = lastCode,
                        rawMessage = lastMessage,
                        fallback = "Detail struk belum bisa dimuat. Silakan coba lagi."
                    )
            )
        }.recoverCatching { throwable ->
            throw IllegalStateException(
                mapThrowable(
                    throwable = throwable,
                    fallback = "Detail struk belum bisa dimuat. Silakan coba lagi."
                )
            )
        }
    }

    override suspend fun getRevenueSummary(token: String, date: LocalDate): Result<RevenueSummaryResult> {
        return suspendRunCatching {
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
                totalRevenue = body.data.totalRevenue ?: 0L,
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

    override suspend fun getRevenueTrend(token: String, date: LocalDate): Result<List<Pair<String, Long>>> {
        return suspendRunCatching {
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

            body.data.map { Pair(it.date ?: "", it.totalRevenue ?: 0L) }
        }.recoverCatching { throwable ->
            throw IllegalStateException(
                mapThrowable(
                    throwable = throwable,
                    fallback = "Grafik omzet belum bisa dimuat. Silakan coba lagi."
                )
            )
        }
    }

    override suspend fun voidTransaction(token: String, transactionId: Long, reason: String, sessionId: Long): Result<String> {
        return suspendRunCatching {
            val idempotencyKey = java.util.UUID.randomUUID().toString()
            val request = com.sipos.kebabsk.feature.transactions.data.remote.VoidTransactionRequest(
                reason = reason,
                currentSessionId = sessionId,
                idempotencyKey = idempotencyKey
            )
            val response = retryNetworkRequest {
                apiService.voidTransaction("Bearer $token", transactionId, request)
            }

            val body = response.body()
            if (!response.isSuccessful || body?.success != true) {
                throw IllegalStateException(
                    mapHttpFailure(
                        code = response.code(),
                        rawMessage = body?.message,
                        fallback = "Gagal membatalkan transaksi. Silakan coba lagi."
                    )
                )
            }

            body.message ?: "Transaksi berhasil dibatalkan."
        }.recoverCatching { throwable ->
            throw IllegalStateException(
                mapThrowable(
                    throwable = throwable,
                    fallback = "Gagal membatalkan transaksi. Silakan coba lagi."
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

    private suspend fun parseTransactionReceiptBody(body: JsonElement?, transactionId: Long): TransactionReceipt {
        return withContext(Dispatchers.Default) {
            if (body == null || body.isJsonNull || !body.isJsonObject) {
                throw IllegalStateException("Respons detail struk tidak valid.")
            }

            val envelope = body.asJsonObject
            val dataElement = envelope.get("data")?.takeIf { !it.isJsonNull } ?: body
            val root = when {
                dataElement.isJsonObject -> dataElement.asJsonObject
                else -> throw IllegalStateException("Data detail struk tidak valid.")
            }

            val transaction = root.objectValue("transaction", "trx", "detail") ?: root
            val itemsArray = transaction.arrayValue("items", "transaction_items", "details", "transaction_details")
                ?: root.arrayValue("items", "transaction_items", "details", "transaction_details")

            val totalAmount = transaction.longValue(
                "total_amount",
                "grand_total",
                "total",
                "subtotal"
            ) ?: root.longValue("total_amount", "grand_total", "total", "subtotal") ?: 0L

            val paidAmount = transaction.longValue(
                "paid_amount",
                "amount_paid",
                "cash_received",
                "received_amount",
                "dibayar"
            ) ?: root.longValue(
                "paid_amount",
                "amount_paid",
                "cash_received",
                "received_amount",
                "dibayar"
            ) ?: totalAmount

            val changeAmount = transaction.longValue(
                "change_amount",
                "change",
                "cash_change",
                "kembalian"
            ) ?: root.longValue(
                "change_amount",
                "change",
                "cash_change",
                "kembalian"
            ) ?: (safeSubtract(paidAmount, totalAmount) ?: 0L).coerceAtLeast(0L)

            val parsedItems = itemsArray
                ?.mapNotNull { element ->
                    if (!element.isJsonObject) return@mapNotNull null
                    val item = element.asJsonObject
                    val qty = item.intValue("qty", "quantity", "jumlah") ?: 1
                    val subtotal = item.longValue("subtotal", "subtotal_amount", "total", "total_price")
                    val price = item.longValue("price", "unit_price", "menu_price", "selling_price")
                        ?: subtotal?.let { if (qty > 0) it / qty else it }
                        ?: 0L
                    val safeSubtotal = subtotal ?: safeMultiply(price, qty) ?: 0L
                    val menu = item.objectValue("menu", "product")
                    val variant = item.objectValue("variant", "menu_variant")
                    val name = item.stringValue("menu_name", "item_name", "product_name", "name")
                        ?: menu?.stringValue("name")
                        ?: variant?.objectValue("menu")?.stringValue("name")
                        ?: return@mapNotNull null
                    val variantName = item.stringValue("variant_name", "variant_label")
                        ?: variant?.stringValue("name")

                    TransactionReceiptItem(
                        name = name,
                        variantName = variantName,
                        qty = qty,
                        price = price,
                        subtotal = safeSubtotal
                    )
                }
                .orEmpty()

            val paymentMethodObject = transaction.objectValue("payment_method", "paymentMethod")
                ?: root.objectValue("payment_method", "paymentMethod")
            val cashierObject = transaction.objectValue("cashier", "kasir", "user", "created_by", "createdBy")
                ?: root.objectValue("cashier", "kasir", "user", "created_by", "createdBy")

            TransactionReceipt(
                id = transaction.longValue("id") ?: root.longValue("id") ?: transactionId,
                code = transaction.stringValue("transaction_code", "code", "invoice_number")
                    ?: root.stringValue("transaction_code", "code", "invoice_number")
                    ?: "TRX-$transactionId",
                createdAtLabel = formatReceiptDate(
                    transaction.stringValue("created_at", "createdAt", "date", "tanggal")
                        ?: root.stringValue("created_at", "createdAt", "date", "tanggal")
                ),
                paymentMethod = transaction.stringValue("payment_method_name", "payment_method", "payment")
                    ?: root.stringValue("payment_method_name", "payment_method", "payment")
                    ?: paymentMethodObject?.stringValue("name", "label", "type")
                    ?: "Tunai",
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                changeAmount = changeAmount,
                status = transaction.stringValue("status")
                    ?: root.stringValue("status")
                    ?: "Unknown",
                items = parsedItems,
                cashierName = transaction.stringValue("cashier_name", "kasir_name", "cashier", "kasir", "user_name", "created_by_name")
                    ?: root.stringValue("cashier_name", "kasir_name", "cashier", "kasir", "user_name", "created_by_name")
                    ?: cashierObject?.stringValue("name", "username", "full_name", "display_name")
                    ?: "Kebab SK POS",
                isDetailed = parsedItems.isNotEmpty()
            )
        }
    }

    private fun formatReceiptDate(createdAt: String?): String {
        if (createdAt.isNullOrBlank()) return "-"
        return runCatching {
            val normalized = createdAt
                .replace("T", " ")
                .substringBefore(".")
                .substringBefore("+")
                .removeSuffix("Z")
                .trim()
            val dateTime = LocalDateTime.parse(
                normalized.take(19),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            )
            dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")))
        }.getOrElse { createdAt }
    }

    private fun JsonElement?.extractApiMessage(): String? {
        if (this == null || isJsonNull || !isJsonObject) return null
        return asJsonObject.stringValue("message", "error")
    }

    private fun JsonObject.stringValue(vararg keys: String): String? {
        return keys.firstNotNullOfOrNull { key ->
            val value = get(key) ?: return@firstNotNullOfOrNull null
            if (value.isJsonNull || !value.isJsonPrimitive) return@firstNotNullOfOrNull null
            runCatching { value.asString.trim().takeIf { it.isNotBlank() } }.getOrNull()
        }
    }

    private fun JsonObject.intValue(vararg keys: String): Int? {
        return keys.firstNotNullOfOrNull { key ->
            val value = get(key) ?: return@firstNotNullOfOrNull null
            if (value.isJsonNull || !value.isJsonPrimitive) return@firstNotNullOfOrNull null
            runCatching { value.asInt }.getOrNull()
        }
    }

    private fun JsonObject.longValue(vararg keys: String): Long? {
        return keys.firstNotNullOfOrNull { key ->
            val value = get(key) ?: return@firstNotNullOfOrNull null
            if (value.isJsonNull || !value.isJsonPrimitive) return@firstNotNullOfOrNull null
            runCatching { value.asLong }.getOrNull()
        }
    }

    private fun JsonObject.objectValue(vararg keys: String): JsonObject? {
        return keys.firstNotNullOfOrNull { key ->
            val value = get(key) ?: return@firstNotNullOfOrNull null
            if (value.isJsonObject) value.asJsonObject else null
        }
    }

    private fun JsonObject.arrayValue(vararg keys: String): JsonArray? {
        return keys.firstNotNullOfOrNull { key ->
            val value = get(key) ?: return@firstNotNullOfOrNull null
            if (value.isJsonArray) value.asJsonArray else null
        }
    }

    private fun mapHttpFailure(code: Int, rawMessage: String?, fallback: String): String {
        // Jika server mengirimkan pesan spesifik (seperti alasan dari exception Laravel), utamakan itu!
        if (!rawMessage.isNullOrBlank() && rawMessage != "The given data was invalid.") {
            return rawMessage
        }
        val httpMapped = com.sipos.kebabsk.common.NetworkErrorMapper.mapHttpCodeToUserMessage(code, fallback)
        return if (httpMapped == fallback) {
            sanitizeUserMessage(rawMessage, fallback)
        } else {
            httpMapped
        }
    }

    private fun mapThrowable(throwable: Throwable, fallback: String): String {
        return com.sipos.kebabsk.common.NetworkErrorMapper.mapThrowableToUserMessage(throwable, fallback)
    }
}
