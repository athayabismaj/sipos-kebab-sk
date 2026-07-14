package com.sipos.kebabsk.feature.auth.domain.validation

import com.sipos.kebabsk.common.validation.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthInputValidatorTest {
    private val validator = AuthInputValidator()

    @Test
    fun loginBlankIdentifierIsInvalid() {
        assertTrue(validator.validateLogin(" ", "password") is ValidationResult.Invalid)
    }

    @Test
    fun loginBlankPasswordIsInvalid() {
        assertTrue(validator.validateLogin("kasir", " ") is ValidationResult.Invalid)
    }

    @Test
    fun loginValidInputIsValid() {
        assertEquals(ValidationResult.Valid, validator.validateLogin(" kasir ", "password"))
    }

    @Test
    fun forgotPasswordBlankEmailIsInvalid() {
        assertTrue(validator.validateForgotPasswordEmail(" ") is ValidationResult.Invalid)
    }

    @Test
    fun resetPasswordMismatchIsInvalid() {
        assertTrue(validator.validateResetPassword("abcdef", "abcdeg") is ValidationResult.Invalid)
    }

    @Test
    fun profileBlankNameIsInvalid() {
        assertTrue(validator.validateProfile(" ", "cahyo", "cahyo@example.com") is ValidationResult.Invalid)
    }

    @Test
    fun changePasswordBlankCurrentIsInvalid() {
        assertTrue(validator.validateChangePassword(" ", "abcdef", "abcdef") is ValidationResult.Invalid)
    }

    @Test
    fun changePasswordValidInputIsValid() {
        assertEquals(ValidationResult.Valid, validator.validateChangePassword("oldpass", "newpass", "newpass"))
    }
}
