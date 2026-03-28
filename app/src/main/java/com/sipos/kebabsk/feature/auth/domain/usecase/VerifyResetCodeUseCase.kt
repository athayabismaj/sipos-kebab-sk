package com.sipos.kebabsk.feature.auth.domain.usecase

import com.sipos.kebabsk.feature.auth.domain.repository.AuthRepository

class VerifyResetCodeUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String): Result<String> {
        return repository.verifyResetCode(email, code)
    }
}
