package com.sipos.kebabsk.feature.dailystock.data.remote

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface DailyStockApiService {
    @GET("daily-stock-items")
    suspend fun getDailyStock(
        @Header("Authorization") authorization: String
    ): Response<JsonObject>

    @POST("daily-stock-sessions/close")
    suspend fun closeSession(
        @Header("Authorization") authorization: String,
        @Body body: JsonObject
    ): Response<JsonObject>

    @POST("daily-stock-sessions/closing-preview")
    suspend fun previewClosing(
        @Header("Authorization") authorization: String,
        @Body body: JsonObject
    ): Response<JsonObject>

    @GET("daily-stock-sessions/closing-cash-review")
    suspend fun getCashReconciliation(
        @Header("Authorization") authorization: String
    ): Response<JsonObject>
}
