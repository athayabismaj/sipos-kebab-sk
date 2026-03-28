package com.sipos.kebabsk.feature.auth.domain.model

data class AuthSession(
    val token: String,
    val displayName: String,
    val username: String,
    val email: String,
    val role: String?
)
