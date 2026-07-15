package com.sipos.kebabsk.feature.auth.data.repository

import com.google.gson.JsonObject
import com.sipos.kebabsk.feature.auth.data.remote.AuthApiService
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.net.SocketTimeoutException

class AuthRepositoryLogoutTest {
    @Test
    fun logoutCallsBackendWithBearerToken() = runTest {
        val api = FakeAuthApiService()
        val repository = AuthRepositoryImpl(api)

        val result = repository.logout("fixture-token")

        assertTrue(result.isSuccess)
        assertEquals("Bearer fixture-token", api.logoutAuthorization)
        assertEquals(1, api.logoutCalls)
    }

    @Test
    fun logoutReturnsFailureForUnauthorizedAndNetworkTimeout() = runTest {
        val unauthorizedApi = FakeAuthApiService(logoutCode = 401)
        val timeoutApi = FakeAuthApiService(logoutFailure = SocketTimeoutException("timeout"))

        assertTrue(AuthRepositoryImpl(unauthorizedApi).logout("expired-token").isFailure)
        assertTrue(AuthRepositoryImpl(timeoutApi).logout("offline-token").isFailure)
    }
}

private class FakeAuthApiService(
    private val logoutCode: Int = 200,
    private val logoutFailure: Throwable? = null
) : AuthApiService {
    var logoutAuthorization: String? = null
    var logoutCalls: Int = 0

    override suspend fun logout(authorization: String): Response<JsonObject> {
        logoutCalls += 1
        logoutAuthorization = authorization
        logoutFailure?.let { throw it }
        if (logoutCode == 200) {
            return Response.success(JsonObject().apply {
                addProperty("success", true)
                addProperty("message", "Logout berhasil.")
            })
        }

        return Response.error(
            logoutCode,
            "{\"success\":false,\"message\":\"Token tidak valid.\"}"
                .toResponseBody("application/json".toMediaType())
        )
    }

    override suspend fun login(request: Map<String, String>) = unsupported()
    override suspend fun forgotPasswordAuth(request: Map<String, String>) = unsupported()
    override suspend fun verifyResetCodeAuth(request: Map<String, String>) = unsupported()
    override suspend fun resetPasswordAuth(request: Map<String, String>) = unsupported()
    override suspend fun me(authorization: String) = unsupported()
    override suspend fun updateProfile(authorization: String, request: Map<String, String>) = unsupported()
    override suspend fun changePassword(authorization: String, request: Map<String, String>) = unsupported()
    override suspend fun sessionCurrentStatus(authorization: String) = unsupported()

    private fun unsupported(): Response<JsonObject> = error("Not used in logout contract test")
}
