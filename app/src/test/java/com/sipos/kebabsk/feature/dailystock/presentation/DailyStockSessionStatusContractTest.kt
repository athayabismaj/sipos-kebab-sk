package com.sipos.kebabsk.feature.dailystock.presentation

import com.google.gson.JsonObject
import com.sipos.kebabsk.feature.auth.data.remote.AuthApiService
import com.sipos.kebabsk.feature.dailystock.domain.model.DailyStockResult
import com.sipos.kebabsk.feature.dailystock.domain.repository.DailyStockRepository
import com.sipos.kebabsk.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class DailyStockSessionStatusContractTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun currentStatus404MeansNoActiveSession() = runTest {
        val viewModel = DailyStockViewModel(
            repository = SessionStatusDailyStockRepository(),
            authApiService = SessionStatusAuthApiService(code = 404)
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSessionOpen ?: true)
    }

    @Test
    fun currentStatus200MeansActiveSession() = runTest {
        val viewModel = DailyStockViewModel(
            repository = SessionStatusDailyStockRepository(sessionId = 81L),
            authApiService = SessionStatusAuthApiService(code = 200)
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSessionOpen == true)
    }

    @Test
    fun currentStatusNetworkFailureRemainsUnknown() = runTest {
        val viewModel = DailyStockViewModel(
            repository = SessionStatusDailyStockRepository(),
            authApiService = SessionStatusAuthApiService(failure = SocketTimeoutException("timeout"))
        )

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.isSessionOpen)
    }
}

private class SessionStatusDailyStockRepository(
    private val sessionId: Long? = null
) : DailyStockRepository {
    override suspend fun getDailyStock(token: String) =
        Result.success(DailyStockResult(sessionId = sessionId, items = emptyList()))

    override suspend fun closeSession(
        token: String,
        remaining: Map<Long, Double>,
        notes: String?
    ) = Result.success("Sesi stok harian berhasil ditutup.")
}

private class SessionStatusAuthApiService(
    private val code: Int = 200,
    private val failure: Throwable? = null
) : AuthApiService {
    override suspend fun sessionCurrentStatus(authorization: String): Response<JsonObject> {
        failure?.let { throw it }

        if (code == 200) {
            return Response.success(JsonObject().apply {
                addProperty("active", true)
                add("data", JsonObject().apply {
                    addProperty("session_id", 81L)
                    addProperty("status", "open")
                })
            })
        }

        return Response.error(
            code,
            "{\"active\":false,\"message\":\"Tidak ada sesi aktif untuk user ini.\"}"
                .toResponseBody("application/json".toMediaType())
        )
    }

    override suspend fun login(request: Map<String, String>) = unsupported()
    override suspend fun forgotPasswordAuth(request: Map<String, String>) = unsupported()
    override suspend fun verifyResetCodeAuth(request: Map<String, String>) = unsupported()
    override suspend fun resetPasswordAuth(request: Map<String, String>) = unsupported()
    override suspend fun me(authorization: String) = unsupported()
    override suspend fun logout(authorization: String) = unsupported()
    override suspend fun updateProfile(authorization: String, request: Map<String, String>) = unsupported()
    override suspend fun changePassword(authorization: String, request: Map<String, String>) = unsupported()

    private fun unsupported(): Response<JsonObject> = error("Not used in session status contract test")
}
