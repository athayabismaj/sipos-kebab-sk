package com.sipos.kebabsk.feature.auth.presentation.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.common.validation.ValidationResult
import com.sipos.kebabsk.feature.auth.domain.repository.AuthRepository
import com.sipos.kebabsk.feature.auth.domain.usecase.ForgotPasswordUseCase
import com.sipos.kebabsk.feature.auth.domain.usecase.ResetPasswordUseCase
import com.sipos.kebabsk.feature.auth.domain.usecase.VerifyResetCodeUseCase
import com.sipos.kebabsk.feature.auth.domain.validation.AuthInputValidator
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

class ForgotPasswordViewModel(
    private val repository: AuthRepository,
    private val inputValidator: AuthInputValidator = AuthInputValidator()
) : ViewModel() {
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
        val validation = inputValidator.validateForgotPasswordEmail(current.email)
        if (validation is ValidationResult.Invalid) {
            _uiState.update { it.copy(errorMessage = validation.message) }
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
        val validation = inputValidator.validateResetCode(current.code)
        if (validation is ValidationResult.Invalid) {
            _uiState.update { it.copy(errorMessage = validation.message) }
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

        val validation = inputValidator.validateResetPassword(current.newPassword, current.confirmPassword)
        if (validation is ValidationResult.Invalid) {
            _uiState.update { it.copy(errorMessage = validation.message) }
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
