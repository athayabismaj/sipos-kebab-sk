package com.sipos.kebabsk.feature.auth.domain.repository

import com.sipos.kebabsk.feature.auth.domain.model.AuthSession

interface AuthRepository {
    suspend fun login(identifier: String, password: String): Result<AuthSession>
    suspend fun me(token: String): Result<AuthSession>
    suspend fun updateProfile(token: String, name: String, username: String, email: String): Result<AuthSession>
    suspend fun changePassword(token: String, currentPassword: String, newPassword: String): Result<String>
    suspend fun forgotPassword(email: String): Result<String>
    suspend fun verifyResetCode(email: String, code: String): Result<String>
    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<String>

    /**
     * Validasi token login terhadap server.
     * Status sesi harian/shift tidak boleh dianggap sebagai status token login.
     */
    suspend fun validateSessionOnServer(token: String): Result<Boolean>
}
