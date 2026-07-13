package com.sipos.kebabsk.feature.checkout.data.remote

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class PaymentMethodsResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: PaymentMethodsData?
)

data class PaymentMethodsData(
    @SerializedName("payment_methods") val paymentMethods: List<PaymentMethodResponse>?
)

data class PaymentMethodResponse(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String?
)

data class CreateTransactionRequest(
    @SerializedName("payment_method_id") val paymentMethodId: Long,
    @SerializedName("paid_amount") val paidAmount: Long,
    @SerializedName("items") val items: List<CreateTransactionItemRequest>,
    @SerializedName("note") val note: String?
)

data class CreateTransactionItemRequest(
    @SerializedName("variant_id") val variantId: Long,
    @SerializedName("qty") val qty: Int
)

data class CreateTransactionResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: CreateTransactionData?
)

data class CreateTransactionData(
    @SerializedName("transaction_id") val transactionId: Long?,
    @SerializedName("transaction_code") val transactionCode: String?,
    @SerializedName("total_amount") val totalAmount: Long?,
    @SerializedName("paid_amount") val paidAmount: Long?,
    @SerializedName("change_amount") val changeAmount: Long?
)

interface CheckoutApiService {
    @GET("payment-methods")
    suspend fun getPaymentMethods(
        @Header("Authorization") authorization: String
    ): Response<PaymentMethodsResponse>

    @POST("transactions")
    suspend fun createTransaction(
        @Header("Authorization") authorization: String,
        @Body request: CreateTransactionRequest
    ): Response<CreateTransactionResponse>
}
