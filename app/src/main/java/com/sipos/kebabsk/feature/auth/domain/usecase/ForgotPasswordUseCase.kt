package com.sipos.kebabsk.feature.auth.domain.usecase

import com.sipos.kebabsk.feature.auth.domain.repository.AuthRepository

class ForgotPasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<String> {
        return repository.forgotPassword(email)
    }
}
