package com.sipos.kebabsk.feature.auth.domain.usecase

import com.sipos.kebabsk.feature.auth.domain.model.AuthSession
import com.sipos.kebabsk.feature.auth.domain.repository.AuthRepository

class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(identifier: String, password: String): Result<AuthSession> {
        return repository.login(identifier, password)
    }
}
