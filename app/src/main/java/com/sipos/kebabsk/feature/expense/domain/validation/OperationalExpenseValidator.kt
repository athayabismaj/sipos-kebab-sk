package com.sipos.kebabsk.feature.expense.domain.validation

import com.sipos.kebabsk.common.MoneyUtils
import com.sipos.kebabsk.common.validation.ValidationResult

data class OperationalExpenseValidationInput(
    val amountInput: String,
    val categoryInput: String,
    val noteInput: String
)

data class OperationalExpenseValidationSuccess(
    val amount: Long,
    val category: String,
    val note: String?
)

sealed interface OperationalExpenseValidationResult {
    data class Valid(val value: OperationalExpenseValidationSuccess) : OperationalExpenseValidationResult
    data class Invalid(val message: String) : OperationalExpenseValidationResult
}

class OperationalExpenseValidator {
    fun validate(input: OperationalExpenseValidationInput): OperationalExpenseValidationResult {
        val amount = MoneyUtils.parseRupiahInput(input.amountInput)
            ?: return OperationalExpenseValidationResult.Invalid("Nominal pengeluaran tidak valid.")

        if (amount <= 0L) {
            return OperationalExpenseValidationResult.Invalid("Nominal pengeluaran tidak valid.")
        }

        val category = input.categoryInput.trim()
        if (category.isBlank()) {
            return OperationalExpenseValidationResult.Invalid("Kategori pengeluaran wajib diisi.")
        }

        return OperationalExpenseValidationResult.Valid(
            OperationalExpenseValidationSuccess(
                amount = amount,
                category = category,
                note = input.noteInput.trim().takeIf { it.isNotBlank() }
            )
        )
    }

    fun validateAmount(input: String): ValidationResult {
        val amount = MoneyUtils.parseRupiahInput(input)
            ?: return ValidationResult.Invalid("Nominal pengeluaran tidak valid.")
        return if (amount > 0L) ValidationResult.Valid else ValidationResult.Invalid("Nominal pengeluaran tidak valid.")
    }
}
