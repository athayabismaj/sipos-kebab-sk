package com.sipos.kebabsk.feature.auth.data.remote

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApiService {
    @Headers("Accept: application/json")
    @POST("auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<JsonObject>

    @Headers("Accept: application/json")
    @POST("auth/forgot-password")
    suspend fun forgotPasswordAuth(@Body request: Map<String, String>): Response<JsonObject>

    @Headers("Accept: application/json")
    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: Map<String, String>): Response<JsonObject>

    @Headers("Accept: application/json")
    @POST("password/email")
    suspend fun forgotPasswordLegacy(@Body request: Map<String, String>): Response<JsonObject>

    @Headers("Accept: application/json")
    @POST("auth/verify-reset-code")
    suspend fun verifyResetCodeAuth(@Body request: Map<String, String>): Response<JsonObject>

    @Headers("Accept: application/json")
    @POST("verify-reset-code")
    suspend fun verifyResetCode(@Body request: Map<String, String>): Response<JsonObject>

    @Headers("Accept: application/json")
    @POST("password/verify-otp")
    suspend fun verifyResetCodeLegacy(@Body request: Map<String, String>): Response<JsonObject>

    @Headers("Accept: application/json")
    @POST("auth/reset-password")
    suspend fun resetPasswordAuth(@Body request: Map<String, String>): Response<JsonObject>

    @Headers("Accept: application/json")
    @POST("reset-password")
    suspend fun resetPassword(@Body request: Map<String, String>): Response<JsonObject>

    @Headers("Accept: application/json")
    @POST("password/reset")
    suspend fun resetPasswordLegacy(@Body request: Map<String, String>): Response<JsonObject>

    @Headers("Accept: application/json")
    @GET("auth/me")
    suspend fun me(@Header("Authorization") authorization: String): Response<JsonObject>

    @Headers("Accept: application/json")
    @PUT("auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: Map<String, String>
    ): Response<JsonObject>

    @Headers("Accept: application/json")
    @POST("auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") authorization: String,
        @Body request: Map<String, String>
    ): Response<JsonObject>
}
