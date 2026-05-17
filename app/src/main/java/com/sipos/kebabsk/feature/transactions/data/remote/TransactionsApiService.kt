package com.sipos.kebabsk.feature.transactions.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface TransactionsApiService {
    @GET("transactions")
    suspend fun getTransactions(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Query("date") date: String? = null,
        @retrofit2.http.Query("page") page: Int? = null
    ): Response<TransactionsResponse>
    @GET("revenue/summary")
    suspend fun getRevenueSummary(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Query("date") date: String? = null
    ): Response<RevenueSummaryResponse>

    @GET("revenue/trend")
    suspend fun getRevenueTrend(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Query("date") date: String? = null
    ): Response<RevenueTrendResponse>

    @retrofit2.http.POST("transactions/{id}/void")
    suspend fun voidTransaction(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Path("id") id: Long,
        @retrofit2.http.Body request: VoidTransactionRequest
    ): Response<VoidTransactionResponse>
}
