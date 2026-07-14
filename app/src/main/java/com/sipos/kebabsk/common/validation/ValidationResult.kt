package com.sipos.kebabsk.common.validation

sealed interface ValidationResult {
    data object Valid : ValidationResult

    data class Invalid(
        val message: String
    ) : ValidationResult
}

fun ValidationResult.messageOrNull(): String? {
    return (this as? ValidationResult.Invalid)?.message
}
