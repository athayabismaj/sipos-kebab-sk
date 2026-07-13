package com.sipos.kebabsk.data.network

import android.content.Context
import com.sipos.kebabsk.BuildConfig
import com.sipos.kebabsk.common.AppSessionStore
import com.sipos.kebabsk.common.AuthSessionEvents
import com.sipos.kebabsk.feature.auth.data.remote.AuthApiService
import com.sipos.kebabsk.feature.checkout.data.remote.CheckoutApiService
import com.sipos.kebabsk.feature.dailystock.data.remote.DailyStockApiService
import com.sipos.kebabsk.feature.expense.data.remote.OperationalExpenseApiService
import com.sipos.kebabsk.feature.menu.data.remote.MenuApiService
import com.sipos.kebabsk.feature.shift.data.remote.CloseShiftApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    @Volatile
    private var appContext: Context? = null

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    private val publicAuthPaths = setOf(
        "/api/auth/login",
        "/api/auth/forgot-password",
        "/api/auth/verify-reset-code",
        "/api/auth/reset-password"
    )

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
        redactHeader("Authorization")
        redactHeader("Cookie")
        redactHeader("Set-Cookie")
        redactHeader("X-CSRF-TOKEN")
        redactHeader("X-XSRF-TOKEN")
        redactHeader("X-API-KEY")
    }

    private val httpClient = OkHttpClient.Builder()
        // Explicit connection pool: max 5 connections, kept alive 5 min
        .connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Accept", "application/json") // Pastikan server tahu kita minta JSON
                // Jangan set Accept-Encoding secara manual — biarkan OkHttp handle gzip otomatis
                .build()
            
            val response = chain.proceed(request)
            
            // Cek apakah server mengembalikan HTML (biasanya halaman error/keamanan)
            val contentType = response.header("Content-Type") ?: ""
            if (contentType.contains("text/html", ignoreCase = true)) {
                val code = response.code
                val path = request.url.encodedPath

                response.close()

                throw java.io.IOException(
                    "Server mengembalikan respons non-JSON. HTTP $code pada $path"
                )
            }
            
            response
        }
        .addInterceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            
            val authorizationHeader = request.header("Authorization")
            val hasBearerToken = authorizationHeader?.startsWith("Bearer ", ignoreCase = true) == true &&
                authorizationHeader.substringAfter("Bearer ", "").trim().isNotEmpty()

            val requestPath = request.url.encodedPath.trimEnd('/')
            val isPublicAuthEndpoint = requestPath in publicAuthPaths

            val shouldForceLogout =
                response.code == 401 &&
                hasBearerToken &&
                !isPublicAuthEndpoint

            if (shouldForceLogout) {
                AppSessionStore.clearSession()
                AuthSessionEvents.notifyForceLogout()
            }
            
            response
        }
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(loggingInterceptor)
            }
        }
        // Reduced timeouts — fail fast on old/weak networks
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
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

    val dailyStockApiService: DailyStockApiService by lazy {
        retrofit.create(DailyStockApiService::class.java)
    }

    val operationalExpenseApiService: OperationalExpenseApiService by lazy {
        retrofit.create(OperationalExpenseApiService::class.java)
    }

    val transactionsApiService: com.sipos.kebabsk.feature.transactions.data.remote.TransactionsApiService by lazy {
        retrofit.create(com.sipos.kebabsk.feature.transactions.data.remote.TransactionsApiService::class.java)
    }

    val closeShiftApiService: CloseShiftApiService by lazy {
        retrofit.create(CloseShiftApiService::class.java)
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        val sanitizedBaseUrl = baseUrl
            .trim()
            .replace(Regex("\\s+"), "")

        return if (sanitizedBaseUrl.endsWith('/')) {
            sanitizedBaseUrl
        } else {
            "$sanitizedBaseUrl/"
        }
    }
}
