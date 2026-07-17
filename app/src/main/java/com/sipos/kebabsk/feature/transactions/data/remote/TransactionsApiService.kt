package com.sipos.kebabsk.feature.transactions.data.remote

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface TransactionsApiService {
    @GET("transactions")
    suspend fun getTransactions(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Query("date") date: String? = null,
        @retrofit2.http.Query("page") page: Int? = null
    ): Response<TransactionsResponse>

    @GET("transactions/{reference}")
    suspend fun getTransactionDetail(
        @Header("Authorization") authorization: String,
        @Path("reference") reference: String
    ): Response<JsonElement>

    @GET("transactions/{reference}/receipt")
    suspend fun getTransactionReceiptDetail(
        @Header("Authorization") authorization: String,
        @Path("reference") reference: String
    ): Response<JsonElement>

    @GET("revenue/summary")
    suspend fun getRevenueSummary(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Query("date") date: String? = null
    ): Response<RevenueSummaryResponse>

    @retrofit2.http.POST("transactions/{id}/void")
    suspend fun voidTransaction(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Path("id") id: Long,
        @retrofit2.http.Body request: VoidTransactionRequest
    ): Response<VoidTransactionResponse>
}
