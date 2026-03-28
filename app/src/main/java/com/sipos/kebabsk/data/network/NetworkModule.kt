package com.sipos.kebabsk.data.network

import com.sipos.kebabsk.BuildConfig
import com.sipos.kebabsk.feature.auth.data.remote.AuthApiService
import com.sipos.kebabsk.feature.checkout.data.remote.CheckoutApiService
import com.sipos.kebabsk.feature.menu.data.remote.MenuApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .callTimeout(75, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(normalizeBaseUrl(BuildConfig.API_BASE_URL))
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val menuApiService: MenuApiService by lazy {
        retrofit.create(MenuApiService::class.java)
    }

    val checkoutApiService: CheckoutApiService by lazy {
        retrofit.create(CheckoutApiService::class.java)
    }

    val transactionsApiService: com.sipos.kebabsk.feature.transactions.data.remote.TransactionsApiService by lazy {
        retrofit.create(com.sipos.kebabsk.feature.transactions.data.remote.TransactionsApiService::class.java)
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        return if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
    }
}
