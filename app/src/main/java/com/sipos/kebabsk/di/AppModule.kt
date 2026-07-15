package com.sipos.kebabsk.di

import com.sipos.kebabsk.BuildConfig
import com.sipos.kebabsk.common.AppSessionStore
import com.sipos.kebabsk.common.AuthSessionEvents
import com.sipos.kebabsk.feature.auth.data.remote.AuthApiService
import com.sipos.kebabsk.feature.auth.data.repository.AuthRepositoryImpl
import com.sipos.kebabsk.feature.auth.domain.repository.AuthRepository
import com.sipos.kebabsk.feature.auth.presentation.forgotpassword.ForgotPasswordViewModel
import com.sipos.kebabsk.feature.auth.presentation.login.LoginViewModel
import com.sipos.kebabsk.feature.checkout.data.remote.CheckoutApiService
import com.sipos.kebabsk.feature.checkout.data.repository.CheckoutRepositoryImpl
import com.sipos.kebabsk.feature.checkout.domain.repository.CheckoutRepository
import com.sipos.kebabsk.feature.dailystock.data.remote.DailyStockApiService
import com.sipos.kebabsk.feature.dailystock.data.repository.DailyStockRepositoryImpl
import com.sipos.kebabsk.feature.dailystock.domain.repository.DailyStockRepository
import com.sipos.kebabsk.feature.dailystock.presentation.DailyStockViewModel
import com.sipos.kebabsk.feature.expense.data.remote.OperationalExpenseApiService
import com.sipos.kebabsk.feature.expense.data.repository.OperationalExpenseRepositoryImpl
import com.sipos.kebabsk.feature.expense.domain.repository.OperationalExpenseRepository
import com.sipos.kebabsk.feature.expense.presentation.OperationalExpenseViewModel
import com.sipos.kebabsk.feature.menu.data.remote.MenuApiService
import com.sipos.kebabsk.feature.menu.data.repository.MenuRepositoryImpl
import com.sipos.kebabsk.feature.menu.domain.repository.MenuRepository
import com.sipos.kebabsk.feature.menu.presentation.MenuViewModel
import com.sipos.kebabsk.feature.cart.presentation.CartViewModel
import com.sipos.kebabsk.feature.checkout.presentation.CheckoutViewModel
import com.sipos.kebabsk.feature.profile.presentation.RevenueViewModel
import com.sipos.kebabsk.feature.shift.presentation.ShiftSummaryViewModel
import com.sipos.kebabsk.feature.transactions.data.remote.TransactionsApiService
import com.sipos.kebabsk.feature.transactions.data.repository.TransactionsRepositoryImpl
import com.sipos.kebabsk.feature.transactions.domain.repository.TransactionsRepository
import com.sipos.kebabsk.feature.transactions.presentation.TransactionsViewModel
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

val publicAuthPaths = setOf(
    "/api/auth/login",
    "/api/auth/forgot-password",
    "/api/auth/verify-reset-code",
    "/api/auth/reset-password"
)

val networkModule = module {
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.HEADERS
            redactHeader("Authorization")
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
        }

        OkHttpClient.Builder()
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .build()

                val response = chain.proceed(request)

                val contentType = response.header("Content-Type") ?: ""
                if (contentType.contains("text/html", ignoreCase = true)) {
                    val code = response.code
                    val path = request.url.encodedPath
                    response.close()
                    throw IOException("Server mengembalikan respons non-JSON. HTTP $code pada $path")
                }
                response
            }
            .addInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)

                val authorizationHeader = request.header("Authorization")
                val hasBearerToken = authorizationHeader?.startsWith("Bearer ", ignoreCase = true) == true

                val requestPath = request.url.encodedPath.trimEnd('/')
                val isPublicAuthEndpoint = requestPath in publicAuthPaths

                val shouldForceLogout = response.code == 401 && hasBearerToken && !isPublicAuthEndpoint

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
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    single {
        fun normalizeBaseUrl(baseUrl: String): String {
            val sanitized = baseUrl.trim().replace(Regex("\\s+"), "")
            return if (sanitized.endsWith('/')) sanitized else "$sanitized/"
        }

        Retrofit.Builder()
            .baseUrl(normalizeBaseUrl(BuildConfig.API_BASE_URL))
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
    }

    // API Services
    single { get<Retrofit>().create(AuthApiService::class.java) }
    single { get<Retrofit>().create(MenuApiService::class.java) }
    single { get<Retrofit>().create(CheckoutApiService::class.java) }
    single { get<Retrofit>().create(DailyStockApiService::class.java) }
    single { get<Retrofit>().create(OperationalExpenseApiService::class.java) }
    single { get<Retrofit>().create(TransactionsApiService::class.java) }
}

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<MenuRepository> { MenuRepositoryImpl(get()) }
    single<CheckoutRepository> { CheckoutRepositoryImpl(get()) }
    single<DailyStockRepository> { DailyStockRepositoryImpl(get()) }
    single<OperationalExpenseRepository> { OperationalExpenseRepositoryImpl(get()) }
    single<TransactionsRepository> { TransactionsRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { ForgotPasswordViewModel(get()) }
    viewModel { MenuViewModel(get()) }
    viewModel { CartViewModel() }
    viewModel { CheckoutViewModel(get()) }
    viewModel { DailyStockViewModel(get(), get()) }
    viewModel { OperationalExpenseViewModel(get()) }
    viewModel { TransactionsViewModel(get()) }
    viewModel { ShiftSummaryViewModel(get()) }
    viewModel { RevenueViewModel(get()) }
}

val appModule = listOf(networkModule, repositoryModule, viewModelModule)
