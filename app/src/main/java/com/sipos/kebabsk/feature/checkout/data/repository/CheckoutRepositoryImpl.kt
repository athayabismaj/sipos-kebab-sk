package com.sipos.kebabsk.feature.checkout.data.repository

import com.google.gson.JsonParser
import com.sipos.kebabsk.common.retryNetworkRequest
import com.sipos.kebabsk.common.suspendRunCatching
import com.sipos.kebabsk.feature.checkout.data.remote.CheckoutApiService
import com.sipos.kebabsk.feature.checkout.data.remote.CreateTransactionItemRequest
import com.sipos.kebabsk.feature.checkout.data.remote.CreateTransactionRequest
import com.sipos.kebabsk.feature.checkout.data.remote.GenerateQrisRequest
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutRequestData
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutResult
import com.sipos.kebabsk.feature.checkout.domain.model.PaymentMethod
import com.sipos.kebabsk.feature.checkout.domain.model.QrisPayment
import com.sipos.kebabsk.feature.checkout.domain.repository.CheckoutRepository
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CheckoutRepositoryImpl(
    private val checkoutApiService: CheckoutApiService
) : CheckoutRepository {
    override suspend fun getPaymentMethods(token: String): Result<List<PaymentMethod>> {
        return suspendRunCatching {
            val response = retryNetworkRequest {
                checkoutApiService.getPaymentMethods("Bearer $token")
            }
            val body = response.body()

            if (!response.isSuccessful || body?.success != true) {
                throw IllegalStateException(body?.message ?: "Gagal memuat metode pembayaran")
            }

            body.data?.paymentMethods.orEmpty().mapNotNull {
                val id = it.id ?: return@mapNotNull null
                val name = it.name?.trim().orEmpty()
                if (id <= 0L || name.isBlank()) return@mapNotNull null
                PaymentMethod(
                    id = id,
                    name = name
                )
            }
        }.recoverCatching { throwable ->
            throw IllegalStateException(mapNetworkError(throwable))
        }
    }

    override suspend fun createTransaction(token: String, request: CheckoutRequestData): Result<CheckoutResult> {
        return suspendRunCatching {
            val apiRequest = CreateTransactionRequest(
                paymentMethodId = request.paymentMethodId,
                paidAmount = request.paidAmount,
                items = request.items.map { CreateTransactionItemRequest(it.variantId, it.qty) },
                note = request.note
            )

            val response = checkoutApiService.createTransaction("Bearer $token", apiRequest)
            val body = response.body()
            val rawError = response.errorBody()?.string()

            if (!response.isSuccessful || body?.success != true || body.data == null) {
                val errorMessage = normalizeBusinessError(extractErrorMessage(rawError))
                    ?: body?.message
                    ?: "Transaksi belum berhasil diproses. Silakan coba lagi."
                throw IllegalStateException(errorMessage)
            }

            val data = body.data
            CheckoutResult(
                transactionId = data.transactionId ?: 0L,
                transactionCode = data.transactionCode ?: "-",
                branchAddress = data.branch?.address?.trim()?.takeIf { it.isNotEmpty() },
                totalAmount = data.totalAmount ?: 0L,
                paidAmount = data.paidAmount ?: 0L,
                changeAmount = data.changeAmount ?: 0L
            )
        }.recoverCatching { throwable ->
            throw IllegalStateException(mapNetworkError(throwable))
        }
    }

    override suspend fun generateQris(token: String, transactionId: Long): Result<QrisPayment> {
        return suspendRunCatching {
            val response = retryNetworkRequest {
                checkoutApiService.generateQris(
                    authorization = "Bearer $token",
                    request = GenerateQrisRequest(transactionId)
                )
            }
            val body = response.body()
            val data = body?.data

            if (!response.isSuccessful || body?.success != true || data == null) {
                val rawError = response.errorBody()?.string()
                throw IllegalStateException(
                    extractErrorMessage(rawError)
                        ?: body?.message
                        ?: "QRIS transaksi belum dapat dibuat. Silakan coba lagi."
                )
            }

            val payload = data.qrisPayload?.trim().orEmpty()
            if (payload.isBlank()) {
                throw IllegalStateException("Payload QRIS dari server kosong.")
            }

            QrisPayment(
                transactionId = data.transactionId ?: transactionId,
                branchName = data.branchName?.trim().orEmpty(),
                merchantName = data.merchantName?.trim().orEmpty(),
                amount = data.amount ?: 0L,
                payload = payload
            )
        }.recoverCatching { throwable ->
            throw IllegalStateException(mapNetworkError(throwable))
        }
    }

    private fun extractErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val json = JsonParser.parseString(raw).asJsonObject
            val message = json.get("message")?.asString
            if (!message.isNullOrBlank()) {
                message
            } else {
                val data = json.getAsJsonObject("data")
                val total = data?.get("total_amount")?.asLong
                val paid = data?.get("paid_amount")?.asLong
                val deficit = data?.get("deficit_amount")?.asLong
                if (deficit != null) {
                    "Pembayaran kurang. Total ${total ?: 0L}, dibayar ${paid ?: 0L}, kurang ${deficit}."
                } else {
                    null
                }
            }
        }.getOrNull()
    }

    private fun normalizeBusinessError(rawMessage: String?): String? {
        if (rawMessage.isNullOrBlank()) return null
        val lower = rawMessage.lowercase()

        return when {
            lower.contains("sesi harian") && lower.contains("belum") ->
                "Sesi harian belum dibuka admin. Checkout belum bisa dilakukan."
            lower.contains("bahan") && lower.contains("stok harian") ->
                "Bahan belum dibawa ke stok harian. Hubungi admin terlebih dahulu."
            lower.contains("stok harian") && (lower.contains("tidak cukup") || lower.contains("kurang")) ->
                "Stok harian bahan tidak cukup untuk transaksi ini."
            lower.contains("pembayaran kurang") || lower.contains("deficit") ->
                "Nominal pembayaran kurang. Silakan periksa kembali."
            else -> rawMessage
        }
    }

    private fun mapNetworkError(throwable: Throwable): String {
        val message = throwable.message ?: ""
        return when (throwable) {
            is SocketTimeoutException -> "Permintaan sedang padat. Silakan coba beberapa saat lagi."
            is ConnectException -> "Aplikasi belum bisa terhubung. Periksa koneksi internet lalu coba lagi."
            is UnknownHostException -> "Layanan sedang tidak tersedia. Silakan coba lagi nanti."
            is IllegalStateException -> throwable.message ?: "Terjadi kesalahan saat checkout."
            else -> {
                if (message.contains("timeout", ignoreCase = true)) {
                    "Permintaan sedang padat. Silakan coba beberapa saat lagi."
                } else {
                    "Koneksi sedang bermasalah. Silakan coba lagi."
                }
            }
        }
    }
}

