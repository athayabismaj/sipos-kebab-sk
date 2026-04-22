package com.sipos.kebabsk.feature.expense.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

data class OperationalExpenseRequest(
    @SerializedName("amount") val amount: Double,
    @SerializedName("source") val source: String,
    @SerializedName("note") val note: String?
)

data class OperationalExpenseResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?
)

interface OperationalExpenseApiService {
    @POST
    suspend fun createExpense(
        @Header("Authorization") authorization: String,
        @Url url: String,
        @Body body: OperationalExpenseRequest
    ): Response<OperationalExpenseResponse>
}
