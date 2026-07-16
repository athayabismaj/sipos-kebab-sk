package com.sipos.kebabsk.feature.auth.domain.validation

import com.sipos.kebabsk.common.validation.ValidationResult

class AuthInputValidator {
    fun validateLogin(identifier: String, password: String): ValidationResult {
        return if (identifier.trim().isBlank() || password.isBlank()) {
            ValidationResult.Invalid("Username dan password wajib diisi")
        } else {
            ValidationResult.Valid
        }
    }

    fun validateForgotPasswordEmail(email: String): ValidationResult {
        return if (email.trim().isBlank()) {
            ValidationResult.Invalid("Email wajib diisi")
        } else {
            ValidationResult.Valid
        }
    }

    fun validateResetCode(code: String): ValidationResult {
        return if (code.trim().isBlank()) {
            ValidationResult.Invalid("Kode OTP wajib diisi")
        } else {
            ValidationResult.Valid
        }
    }

    fun validateResetPassword(newPassword: String, confirmPassword: String): ValidationResult {
        if (newPassword.isBlank() || confirmPassword.isBlank()) {
            return ValidationResult.Invalid("Password baru wajib diisi")
        }
        if (newPassword != confirmPassword) {
            return ValidationResult.Invalid("Konfirmasi password tidak sama")
        }
        return ValidationResult.Valid
    }

    fun validateProfile(name: String, username: String, email: String): ValidationResult {
        return if (name.trim().isBlank() || username.trim().isBlank() || email.trim().isBlank()) {
            ValidationResult.Invalid("Nama, username, dan email wajib diisi.")
        } else {
            ValidationResult.Valid
        }
    }

    fun validateChangePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): ValidationResult {
        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            return ValidationResult.Invalid("Semua field password wajib diisi.")
        }
        if (newPassword.length < 6) {
            return ValidationResult.Invalid("Password baru minimal 6 karakter.")
        }
        if (newPassword != confirmPassword) {
            return ValidationResult.Invalid("Konfirmasi password tidak sama.")
        }
        return ValidationResult.Valid
    }
}
