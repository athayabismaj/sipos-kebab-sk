package com.sipos.kebabsk.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.data.network.NetworkModule
import com.sipos.kebabsk.feature.auth.data.repository.AuthRepositoryImpl
import com.sipos.kebabsk.feature.auth.domain.model.AuthSession
import com.sipos.kebabsk.feature.auth.domain.repository.AuthRepository
import com.sipos.kebabsk.feature.auth.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val session: AuthSession? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl(NetworkModule.authApiService),
    private val loginUseCase: LoginUseCase = LoginUseCase(authRepository)
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIdentifierChanged(value: String) {
        _uiState.update { it.copy(identifier = value, errorMessage = null, successMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null, successMessage = null) }
    }

    fun login() {
        val current = _uiState.value
        if (current.identifier.isBlank() || current.password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Email/username dan password wajib diisi")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = loginUseCase(current.identifier.trim(), current.password)

            result
                .onSuccess { session ->
                    val meResult = authRepository.me(session.token)
                    val finalSession = meResult.getOrElse { session }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            session = finalSession,
                            password = ""
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = sanitizeUserMessage(error.message, "Login belum berhasil. Silakan coba lagi.")
                        )
                    }
                }
        }
    }

    fun refreshSession() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            authRepository.me(session.token)
                .onSuccess { fresh ->
                    _uiState.update {
                        it.copy(session = fresh, errorMessage = null)
                    }
                }
        }
    }

    fun updateProfile(name: String, username: String, email: String) {
        val session = _uiState.value.session
        if (session == null) {
            _uiState.update { it.copy(errorMessage = "Sesi login tidak ditemukan.") }
            return
        }

        if (name.isBlank() || username.isBlank() || email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama, username, dan email wajib diisi.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            authRepository.updateProfile(session.token, name, username, email)
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            session = updated,
                            successMessage = "Profil berhasil diperbarui.",
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = null,
                            errorMessage = sanitizeUserMessage(error.message, "Profil belum berhasil diperbarui. Silakan coba lagi.")
                        )
                    }
                }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        val session = _uiState.value.session
        if (session == null) {
            _uiState.update { it.copy(errorMessage = "Sesi login tidak ditemukan.") }
            return
        }

        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Semua field password wajib diisi.") }
            return
        }

        if (newPassword.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password baru minimal 6 karakter.") }
            return
        }

        if (newPassword != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Konfirmasi password tidak sama.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            authRepository.changePassword(session.token, currentPassword, newPassword)
                .onSuccess { message ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = message,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = null,
                            errorMessage = sanitizeUserMessage(error.message, "Password belum berhasil diubah. Silakan coba lagi.")
                        )
                    }
                }
        }
    }

    fun clearProfileMessage() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                session = null,
                password = "",
                errorMessage = null,
                successMessage = null
            )
        }
    }
}
