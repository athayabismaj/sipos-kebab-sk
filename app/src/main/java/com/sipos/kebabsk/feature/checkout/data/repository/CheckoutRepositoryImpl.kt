package com.sipos.kebabsk.feature.checkout.data.repository

import com.google.gson.JsonParser
import com.sipos.kebabsk.feature.checkout.data.remote.CheckoutApiService
import com.sipos.kebabsk.feature.checkout.data.remote.CreateTransactionItemRequest
import com.sipos.kebabsk.feature.checkout.data.remote.CreateTransactionRequest
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutRequestData
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutResult
import com.sipos.kebabsk.feature.checkout.domain.model.PaymentMethod
import com.sipos.kebabsk.feature.checkout.domain.repository.CheckoutRepository
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CheckoutRepositoryImpl(
    private val checkoutApiService: CheckoutApiService
) : CheckoutRepository {
    override suspend fun getPaymentMethods(token: String): Result<List<PaymentMethod>> {
        return runCatching {
            val response = checkoutApiService.getPaymentMethods("Bearer $token")
            val body = response.body()

            if (!response.isSuccessful || body?.success != true) {
                throw IllegalStateException(body?.message ?: "Gagal memuat metode pembayaran")
            }

            body.data?.paymentMethods.orEmpty().map {
                PaymentMethod(
                    id = it.id ?: 0L,
                    name = it.name ?: "Unknown"
                )
            }
        }.recoverCatching { throwable ->
            throw IllegalStateException(mapNetworkError(throwable))
        }
    }

    override suspend fun createTransaction(token: String, request: CheckoutRequestData): Result<CheckoutResult> {
        return runCatching {
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
                val errorMessage = extractErrorMessage(rawError)
                    ?: body?.message
                    ?: "Transaksi belum berhasil diproses. Silakan coba lagi."
                throw IllegalStateException(errorMessage)
            }

            val data = body.data
            CheckoutResult(
                transactionId = data.transactionId ?: 0L,
                transactionCode = data.transactionCode ?: "-",
                totalAmount = data.totalAmount ?: 0.0,
                paidAmount = data.paidAmount ?: 0.0,
                changeAmount = data.changeAmount ?: 0.0
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
                val total = data?.get("total_amount")?.asDouble
                val paid = data?.get("paid_amount")?.asDouble
                val deficit = data?.get("deficit_amount")?.asDouble
                if (deficit != null) {
                    "Pembayaran kurang. Total ${total ?: 0.0}, dibayar ${paid ?: 0.0}, kurang ${deficit}."
                } else {
                    null
                }
            }
        }.getOrNull()
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

