package com.sipos.kebabsk.feature.auth.data.repository

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.sipos.kebabsk.common.firstString
import com.google.gson.JsonParser
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.common.suspendRunCatching
import com.sipos.kebabsk.feature.auth.data.remote.AuthApiService
import com.sipos.kebabsk.feature.auth.data.mapper.AuthSessionMapper
import com.sipos.kebabsk.feature.auth.domain.model.AuthSession
import com.sipos.kebabsk.feature.auth.domain.repository.AuthRepository
import retrofit2.Response

class AuthRepositoryImpl(
    private val authApiService: AuthApiService
) : AuthRepository {
    override suspend fun login(identifier: String, password: String): Result<AuthSession> {
        return suspendRunCatching {
            val normalizedIdentifier = identifier.trim()
            val request = buildLoginRequest(normalizedIdentifier, password)

            val response = authApiService.login(request)
            val body = response.body()
            val token = extractToken(body)

            if (response.isSuccessful && !token.isNullOrBlank()) {
                AuthSessionMapper.fromResponse(body, normalizeAccessToken(token), identifier)
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), body)
                    ?: "Login belum berhasil. Periksa kembali username dan password Anda."
                throw IllegalStateException(normalizeAuthError(msg, "Login belum berhasil. Silakan coba lagi."))
            }
        }
    }

    override suspend fun logout(token: String): Result<Unit> {
        return suspendRunCatching {
            val response = authApiService.logout("Bearer $token")
            if (!response.isSuccessful) {
                val msg = extractErrorMessage(response.errorBody()?.string(), response.body())
                    ?: "Logout server belum berhasil."
                throw IllegalStateException(normalizeAuthError(msg, "Logout server belum berhasil."))
            }
        }
    }

    private fun buildLoginRequest(identifier: String, password: String): Map<String, String> {
        return mapOf(
            "username" to identifier,
            "password" to password
        )
    }

    override suspend fun me(token: String): Result<AuthSession> {
        return suspendRunCatching {
            val response = authApiService.me("Bearer $token")
            val body = response.body()

            if (response.isSuccessful && body != null) {
                AuthSessionMapper.fromResponse(body, token, "")
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), body)
                    ?: "Gagal memuat profil user."
                throw IllegalStateException(normalizeAuthError(msg, "Profil belum bisa dimuat. Silakan coba lagi."))
            }
        }
    }

    override suspend fun updateProfile(token: String, name: String, username: String, email: String): Result<AuthSession> {
        return suspendRunCatching {
            val request = mapOf(
                "name" to name,
                "username" to username,
                "email" to email
            )
            val response = authApiService.updateProfile("Bearer $token", request)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                AuthSessionMapper.fromResponse(body, token, username)
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), body)
                    ?: "Update profile gagal."
                throw IllegalStateException(normalizeAuthError(msg, "Profil belum berhasil diperbarui. Silakan coba lagi."))
            }
        }
    }

    override suspend fun changePassword(token: String, currentPassword: String, newPassword: String): Result<String> {
        return suspendRunCatching {
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
                throw IllegalStateException(normalizeAuthError(msg, "Password belum berhasil diubah. Silakan coba lagi."))
            }
        }
    }

    override suspend fun forgotPassword(email: String): Result<String> {
        return suspendRunCatching {
            val request = mapOf("email" to email)
            val response = authApiService.forgotPasswordAuth(request)
            val body = response.body()

            if (response.isSuccessful) {
                extractMessage(body) ?: "Instruksi reset sandi telah dikirim."
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), body)
                    ?: "Permintaan reset sandi belum berhasil. Silakan coba lagi."
                throw IllegalStateException(msg)
            }
        }
    }

    override suspend fun verifyResetCode(email: String, code: String): Result<String> {
        return suspendRunCatching {
            val request = mapOf(
                "email" to email,
                "otp" to code,
                "token" to code
            )
            val response = authApiService.verifyResetCodeAuth(request)
            val body = response.body()

            if (response.isSuccessful) {
                extractMessage(body) ?: "Kode verifikasi valid."
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), body)
                    ?: "Kode reset tidak valid. Silakan periksa kembali email Anda."
                throw IllegalStateException(msg)
            }
        }
    }

    override suspend fun resetPassword(email: String, code: String, newPassword: String): Result<String> {
        return suspendRunCatching {
            val request = mapOf(
                "email" to email,
                "otp" to code,
                "token" to code,
                "password" to newPassword,
                "password_confirmation" to newPassword,
                "new_password" to newPassword,
                "newPassword" to newPassword
            )
            val response = authApiService.resetPasswordAuth(request)
            val body = response.body()

            if (response.isSuccessful) {
                extractMessage(body) ?: "Sandi baru berhasil disimpan."
            } else {
                val msg = extractErrorMessage(response.errorBody()?.string(), body)
                    ?: "Sandi baru gagal disimpan. Silakan coba lagi."
                throw IllegalStateException(msg)
            }
        }
    }

    private fun extractToken(body: JsonObject?): String? {
        if (body == null) return null

        val topLevel = body.firstString("token", "access_token", "accessToken")
        if (!topLevel.isNullOrBlank()) return topLevel

        val data = body.getAsJsonObjectOrNull("data")
        return data?.firstString("token", "access_token", "accessToken")
    }

    private fun normalizeAccessToken(raw: String): String {
        val token = raw.trim()
        return if (token.startsWith("Bearer ", ignoreCase = true)) {
            token.substringAfter(" ", "").trim()
        } else {
            token
        }
    }

    private fun extractMessage(body: JsonObject?): String? {
        return body?.firstString("message", "status")
            ?: body?.getAsJsonObjectOrNull("data")?.firstString("message", "status")
    }

    private fun extractErrorMessage(errorBodyString: String?, body: JsonObject?): String? {
        val parsedError = parseJson(errorBodyString)

        return firstNonBlank(
            parsedError?.firstString("message", "error"),
            extractValidationErrors(parsedError),
            body?.firstString("message", "error")
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

    private fun firstNonBlank(vararg values: String?): String? {
        return values.firstOrNull { !it.isNullOrBlank() }
    }

    private fun normalizeAuthError(rawMessage: String?, fallback: String): String {
        val safe = sanitizeUserMessage(rawMessage, fallback)
        val lower = safe.lowercase()

        return when {
            lower.contains("password") && lower.contains("salah") ->
                "Password yang dimasukkan belum sesuai."
            lower.contains("user") && lower.contains("tidak ditemukan") ->
                "Akun tidak ditemukan. Periksa kembali email atau username."
            lower.contains("otp") && lower.contains("tidak valid") ->
                "Kode OTP tidak valid. Silakan cek kembali."
            lower.contains("expired") || lower.contains("kadaluarsa") ->
                "Sesi verifikasi sudah kedaluwarsa. Silakan minta kode baru."
            lower.contains("terlalu sering") || lower.contains("too many") ->
                "Permintaan terlalu sering. Coba lagi beberapa saat."
            else -> safe
        }
    }

    private fun JsonObject.getAsJsonObjectOrNull(key: String): JsonObject? {
        val value = get(key) ?: return null
        return if (value.isJsonObject) value.asJsonObject else null
    }

    private fun JsonElement?.asStringOrNull(): String? {
        if (this == null || !isJsonPrimitive) return null
        return runCatching { asString }.getOrNull()
    }

    /**
     * Validasi token login terhadap server.
     * Status sesi harian/shift tidak boleh dianggap sebagai status token login.
     */
    override suspend fun validateSessionOnServer(token: String): Result<Boolean> {
        return suspendRunCatching {
            val response = authApiService.me("Bearer $token")

            when {
                response.isSuccessful -> true
                response.code() == 401 || response.code() == 403 -> false
                else -> throw IllegalStateException(
                    extractErrorMessage(response.errorBody()?.string(), response.body())
                        ?: "Sesi belum bisa divalidasi."
                )
            }
        }
    }
}


