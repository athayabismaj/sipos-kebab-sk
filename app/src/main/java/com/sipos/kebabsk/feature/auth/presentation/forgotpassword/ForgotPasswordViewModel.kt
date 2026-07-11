package com.sipos.kebabsk.feature.auth.presentation.forgotpassword

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.data.network.NetworkModule
import com.sipos.kebabsk.feature.auth.data.repository.AuthRepositoryImpl
import com.sipos.kebabsk.feature.auth.domain.usecase.ForgotPasswordUseCase
import com.sipos.kebabsk.feature.auth.domain.usecase.ResetPasswordUseCase
import com.sipos.kebabsk.feature.auth.domain.usecase.VerifyResetCodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ForgotPasswordStep {
    REQUEST,
    VERIFY,
    RESET,
    DONE
}

data class ForgotPasswordUiState(
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val step: ForgotPasswordStep = ForgotPasswordStep.REQUEST,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ForgotPasswordViewModel : ViewModel() {
    private val repository = AuthRepositoryImpl(NetworkModule.authApiService)
    private val forgotPasswordUseCase = ForgotPasswordUseCase(repository)
    private val verifyResetCodeUseCase = VerifyResetCodeUseCase(repository)
    private val resetPasswordUseCase = ResetPasswordUseCase(repository)

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null, successMessage = null) }
    }

    fun onCodeChanged(value: String) {
        _uiState.update { it.copy(code = value, errorMessage = null, successMessage = null) }
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.update { it.copy(newPassword = value, errorMessage = null, successMessage = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null, successMessage = null) }
    }

    fun submitForgotPassword() {
        val current = _uiState.value
        if (current.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email wajib diisi") }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(current.email.trim()).matches()) {
            _uiState.update { it.copy(errorMessage = "Format email belum valid") }
            return
        }

        viewModelScope.launch {
            setLoading()
            forgotPasswordUseCase(current.email.trim())
                .onSuccess { message ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = ForgotPasswordStep.VERIFY,
                            successMessage = message,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = sanitizeUserMessage(error.message, "Kode reset belum berhasil dikirim. Silakan coba lagi."),
                            successMessage = null
                        )
                    }
                }
        }
    }

    fun submitCodeVerification() {
        val current = _uiState.value
        if (current.code.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Kode OTP wajib diisi") }
            return
        }

        viewModelScope.launch {
            setLoading()
            verifyResetCodeUseCase(current.email.trim(), current.code.trim())
                .onSuccess { message ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = ForgotPasswordStep.RESET,
                            successMessage = message,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = sanitizeUserMessage(error.message, "Verifikasi kode belum berhasil. Silakan coba lagi."),
                            successMessage = null
                        )
                    }
                }
        }
    }

    fun submitResetPassword() {
        val current = _uiState.value

        if (current.newPassword.isBlank() || current.confirmPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Password baru wajib diisi") }
            return
        }

        if (current.newPassword != current.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Konfirmasi password tidak sama") }
            return
        }

        viewModelScope.launch {
            setLoading()
            resetPasswordUseCase(current.email.trim(), current.code.trim(), current.newPassword)
                .onSuccess { message ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = ForgotPasswordStep.DONE,
                            successMessage = message,
                            errorMessage = null,
                            code = "",
                            newPassword = "",
                            confirmPassword = ""
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = sanitizeUserMessage(error.message, "Password belum berhasil diganti. Silakan coba lagi."),
                            successMessage = null
                        )
                    }
                }
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordUiState()
    }

    private fun setLoading() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
    }
}
