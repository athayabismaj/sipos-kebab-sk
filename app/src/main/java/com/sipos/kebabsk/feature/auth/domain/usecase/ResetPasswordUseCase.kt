package com.sipos.kebabsk.feature.auth.domain.usecase

import com.sipos.kebabsk.feature.auth.domain.repository.AuthRepository

class ResetPasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String, newPassword: String): Result<String> {
        return repository.resetPassword(email, code, newPassword)
    }
}
