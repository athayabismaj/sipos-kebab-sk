package com.sipos.kebabsk.feature.auth.data.repository

import com.google.gson.JsonObject
import com.sipos.kebabsk.feature.auth.data.remote.AuthApiService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AuthRepositoryLoginRequestTest {
    @Test
    fun loginWithUsernameDoesNotSendUsernameAsEmail() = runTest {
        val api = CapturingAuthApiService()
        val repository = AuthRepositoryImpl(api)

        val result = repository.login("cahyo_p", "kebabsk123")

        assertTrue(result.isSuccess)
        assertEquals("cahyo_p", api.loginRequest?.get("username"))
        assertEquals("kebabsk123", api.loginRequest?.get("password"))
        assertFalse(api.loginRequest.orEmpty().containsKey("email"))
        assertFalse(api.loginRequest.orEmpty().containsKey("login"))
    }

    @Test
    fun loginNeverSendsEmailFieldBecauseEmailIsOnlyForOtp() = runTest {
        val api = CapturingAuthApiService()
        val repository = AuthRepositoryImpl(api)

        val result = repository.login("kasir@skkebab.my.id", "kebabsk123")

        assertTrue(result.isSuccess)
        assertEquals("kasir@skkebab.my.id", api.loginRequest?.get("username"))
        assertFalse(api.loginRequest.orEmpty().containsKey("email"))
        assertFalse(api.loginRequest.orEmpty().containsKey("login"))
    }
}

private class CapturingAuthApiService : AuthApiService {
    var loginRequest: Map<String, String>? = null

    override suspend fun login(request: Map<String, String>): Response<JsonObject> {
        loginRequest = request
        return Response.success(loginSuccessBody())
    }

    override suspend fun forgotPasswordAuth(request: Map<String, String>) = unsupported()
    override suspend fun verifyResetCodeAuth(request: Map<String, String>) = unsupported()
    override suspend fun resetPasswordAuth(request: Map<String, String>) = unsupported()
    override suspend fun me(authorization: String) = unsupported()
    override suspend fun logout(authorization: String) = unsupported()
    override suspend fun updateProfile(authorization: String, request: Map<String, String>) = unsupported()
    override suspend fun changePassword(authorization: String, request: Map<String, String>) = unsupported()
    override suspend fun sessionCurrentStatus(authorization: String) = unsupported()

    private fun loginSuccessBody(): JsonObject {
        val user = JsonObject().apply {
            addProperty("name", "cahyo setiawan")
            addProperty("username", "cahyo_p")
            addProperty("email", "cahyo@example.test")
            addProperty("role", "kasir")
        }
        val data = JsonObject().apply {
            addProperty("token", "fixture-token")
            add("user", user)
        }
        return JsonObject().apply {
            addProperty("success", true)
            add("data", data)
        }
    }

    private fun unsupported(): Response<JsonObject> = error("Not used in login request test")
}
