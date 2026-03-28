package com.sipos.kebabsk.feature.auth.data.repository

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sipos.kebabsk.feature.auth.data.remote.AuthApiService
import com.sipos.kebabsk.feature.auth.domain.model.AuthSession
import com.sipos.kebabsk.feature.auth.domain.repository.AuthRepository
import retrofit2.Response

class AuthRepositoryImpl(
    private val authApiService: AuthApiService
) : AuthRepository {
    override suspend fun login(identifier: String, password: String): Result<AuthSession> {
        return runCatching {
            val request = mapOf(
                "email" to identifier,
                "username" to identifier,
                "login" to identifier,
                "password" to password
            )

            val response = authApiService.login(request)
            val body = response.body()
            val token = extractToken(body)

            if (response.isSuccessful && !token.isNullOrBlank()) {
                parseUserSession(body, token, identifier)
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), body)
                    ?: "Login belum berhasil. Periksa kembali email/username dan password Anda."
                throw IllegalStateException(msg)
            }
        }
    }

    override suspend fun me(token: String): Result<AuthSession> {
        return runCatching {
            val response = authApiService.me("Bearer $token")
            val body = response.body()

            if (response.isSuccessful && body != null) {
                parseUserSession(body, token, "")
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), body)
                    ?: "Gagal memuat profil user."
                throw IllegalStateException(msg)
            }
        }
    }

    override suspend fun updateProfile(token: String, name: String, username: String, email: String): Result<AuthSession> {
        return runCatching {
            val request = mapOf(
                "name" to name,
                "username" to username,
                "email" to email
            )
            val response = authApiService.updateProfile("Bearer $token", request)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                parseUserSession(body, token, username)
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), body)
                    ?: "Update profile gagal."
                throw IllegalStateException(msg)
            }
        }
    }

    override suspend fun changePassword(token: String, currentPassword: String, newPassword: String): Result<String> {
        return runCatching {
            val request = mapOf(
                "current_password" to currentPassword,
                "password" to newPassword,
                "password_confirmation" to newPassword
            )
            val response = authApiService.changePassword("Bearer $token", request)
            val body = response.body()

            if (response.isSuccessful) {
                extractMessage(body) ?: "Password berhasil diubah."
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), body)
                    ?: "Ubah password gagal."
                throw IllegalStateException(msg)
            }
        }
    }

    override suspend fun forgotPassword(email: String): Result<String> {
        return runCatching {
            val request = mapOf(
                "email" to email,
                "identifier" to email
            )
            executeMultiEndpoint(
                requests = listOf(
                    { authApiService.forgotPasswordAuth(request) },
                    { authApiService.forgotPassword(request) },
                    { authApiService.forgotPasswordLegacy(request) }
                ),
                fallbackError = "Permintaan reset sandi belum berhasil. Silakan coba lagi."
            )
        }
    }

    override suspend fun verifyResetCode(email: String, code: String): Result<String> {
        return runCatching {
            val request = mapOf(
                "email" to email,
                "code" to code,
                "otp" to code,
                "token" to code
            )
            executeMultiEndpoint(
                requests = listOf(
                    { authApiService.verifyResetCodeAuth(request) },
                    { authApiService.verifyResetCode(request) },
                    { authApiService.verifyResetCodeLegacy(request) }
                ),
                fallbackError = "Verifikasi kode belum berhasil. Silakan coba lagi."
            )
        }
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<String> {
        return runCatching {
            val request = mapOf(
                "email" to email,
                "code" to code,
                "otp" to code,
                "token" to code,
                "password" to newPassword,
                "password_confirmation" to newPassword,
                "new_password" to newPassword,
                "newPassword" to newPassword
            )
            executeMultiEndpoint(
                requests = listOf(
                    { authApiService.resetPasswordAuth(request) },
                    { authApiService.resetPassword(request) },
                    { authApiService.resetPasswordLegacy(request) }
                ),
                fallbackError = "Ganti password belum berhasil. Silakan coba lagi."
            )
        }
    }

    private fun parseUserSession(body: JsonObject?, token: String, fallbackIdentifier: String): AuthSession {
        val userJson = extractUserJson(body)
        val displayName = firstString(userJson, "name") ?: firstString(body, "name") ?: fallbackIdentifier
        val username = firstString(userJson, "username") ?: fallbackIdentifier
        val email = firstString(userJson, "email") ?: ""
        val role = firstString(userJson, "role")

        return AuthSession(
            token = token,
            displayName = displayName,
            username = username,
            email = email,
            role = role
        )
    }

    private suspend fun executeMultiEndpoint(
        requests: List<suspend () -> Response<JsonObject>>,
        fallbackError: String
    ): String {
        val responses = requests.map { it() }
        val successful = responses.firstOrNull { it.isSuccessful }
        if (successful != null) {
            val successBody = successful.body()
            return extractMessage(successBody) ?: "Operasi berhasil."
        }

        val allErrors = responses
            .asSequence()
            .map { response -> extractErrorMessage(response.errorBody()?.string(), response.body()) }
            .filterNotNull()
            .toList()

        val bestError = allErrors.firstOrNull { !isRouteNotFoundMessage(it) }
            ?: allErrors.firstOrNull()
            ?: fallbackError

        throw IllegalStateException(bestError)
    }

    private fun extractToken(body: JsonObject?): String? {
        if (body == null) return null

        val topLevel = firstString(body, "token", "access_token", "accessToken")
        if (!topLevel.isNullOrBlank()) return topLevel

        val data = body.getAsJsonObjectOrNull("data")
        return firstString(data, "token", "access_token", "accessToken")
    }

    private fun extractUserJson(body: JsonObject?): JsonObject? {
        if (body == null) return null
        val direct = body.getAsJsonObjectOrNull("user")
        if (direct != null) return direct

        val data = body.getAsJsonObjectOrNull("data") ?: return null
        val nested = data.getAsJsonObjectOrNull("user")
        return nested ?: data
    }

    private fun extractMessage(body: JsonObject?): String? {
        return firstString(body, "message", "status")
            ?: firstString(body?.getAsJsonObjectOrNull("data"), "message", "status")
    }

    private fun extractErrorMessage(errorBodyString: String?, body: JsonObject?): String? {
        val parsedError = parseJson(errorBodyString)

        return firstNonBlank(
            firstString(parsedError, "message", "error"),
            extractValidationErrors(parsedError),
            firstString(body, "message", "error")
        )
    }

    private fun extractValidationErrors(json: JsonObject?): String? {
        val errorsObj = json?.getAsJsonObjectOrNull("errors") ?: return null
        val firstEntry = errorsObj.entrySet().firstOrNull() ?: return null
        val firstValue = firstEntry.value

        return when {
            firstValue.isJsonArray && firstValue.asJsonArray.size() > 0 -> {
                firstValue.asJsonArray[0].asStringOrNull()
            }
            firstValue.isJsonPrimitive -> firstValue.asStringOrNull()
            else -> null
        }
    }

    private fun parseJson(raw: String?): JsonObject? {
        if (raw.isNullOrBlank()) return null
        return runCatching { JsonParser.parseString(raw) }
            .getOrNull()
            ?.takeIf { it.isJsonObject }
            ?.asJsonObject
    }

    private fun firstString(json: JsonObject?, vararg keys: String): String? {
        if (json == null) return null
        for (key in keys) {
            val value = json.get(key).asStringOrNull()
            if (!value.isNullOrBlank()) return value
        }
        return null
    }

    private fun firstNonBlank(vararg values: String?): String? {
        return values.firstOrNull { !it.isNullOrBlank() }
    }

    private fun isRouteNotFoundMessage(message: String): Boolean {
        val normalized = message.lowercase()
        return normalized.contains("route") && normalized.contains("could not be found")
    }

    private fun JsonObject.getAsJsonObjectOrNull(key: String): JsonObject? {
        val value = get(key) ?: return null
        return if (value.isJsonObject) value.asJsonObject else null
    }

    private fun JsonElement?.asStringOrNull(): String? {
        if (this == null || !isJsonPrimitive) return null
        return runCatching { asString }.getOrNull()
    }
}

