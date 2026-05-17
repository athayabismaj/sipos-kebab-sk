package com.sipos.kebabsk.data.network

import android.content.Context
import com.sipos.kebabsk.BuildConfig
import com.sipos.kebabsk.common.AuthSessionEvents
import com.sipos.kebabsk.feature.auth.data.remote.AuthApiService
import com.sipos.kebabsk.feature.checkout.data.remote.CheckoutApiService
import com.sipos.kebabsk.feature.dailystock.data.remote.DailyStockApiService
import com.sipos.kebabsk.feature.expense.data.remote.OperationalExpenseApiService
import com.sipos.kebabsk.feature.menu.data.remote.MenuApiService
import com.sipos.kebabsk.feature.shift.data.remote.CloseShiftApiService
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object NetworkModule {
    @Volatile
    private var appContext: Context? = null

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY // Tampilkan response body di Logcat
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val httpCache: Cache? by lazy {
        val context = appContext ?: return@lazy null
        runCatching {
            Cache(File(context.cacheDir, "http_cache"), 15L * 1024L * 1024L)
        }.getOrNull()
    }

    private val httpClient = OkHttpClient.Builder()
        .apply {
            httpCache?.let { cache(it) }
        }
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
                val bodyString = response.peekBody(Long.MAX_VALUE).string()
                val snippet = if (bodyString.length > 100) bodyString.take(100) + "..." else bodyString
                throw java.io.IOException("Server mengembalikan halaman Web/HTML, bukan JSON. Cek konfigurasi server/domain Anda. Snippet: $snippet")
            }
            
            response
        }
        .addInterceptor { chain ->
            val response = chain.proceed(chain.request())
            if (response.code == 401) {
                AuthSessionEvents.notifyForceLogout()
            }
            response
        }
        .addInterceptor(loggingInterceptor)
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
        return if (baseUrl.endsWith('/')) baseUrl else "$baseUrl/"
    }
}
