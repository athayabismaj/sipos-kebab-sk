package com.sipos.kebabsk.feature.expense.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OperationalExpenseValidatorTest {
    private val validator = OperationalExpenseValidator()

    @Test
    fun amountEmptyIsInvalid() {
        val result = validator.validate(input(amountInput = ""))
        assertTrue(result is OperationalExpenseValidationResult.Invalid)
    }

    @Test
    fun amountLettersIsInvalid() {
        val result = validator.validate(input(amountInput = "abc"))
        assertTrue(result is OperationalExpenseValidationResult.Invalid)
    }

    @Test
    fun amountZeroIsInvalid() {
        val result = validator.validate(input(amountInput = "0"))
        assertTrue(result is OperationalExpenseValidationResult.Invalid)
    }

    @Test
    fun amountOverflowIsInvalid() {
        val result = validator.validate(input(amountInput = "999999999999999999999"))
        assertTrue(result is OperationalExpenseValidationResult.Invalid)
    }

    @Test
    fun categoryBlankIsInvalid() {
        val result = validator.validate(input(categoryInput = "   "))
        assertTrue(result is OperationalExpenseValidationResult.Invalid)
    }

    @Test
    fun validInputTrimsArguments() {
        val result = validator.validate(input(categoryInput = "  Kas Besar  ", noteInput = "  catatan  "))

        assertTrue(result is OperationalExpenseValidationResult.Valid)
        val value = (result as OperationalExpenseValidationResult.Valid).value
        assertEquals(150_000L, value.amount)
        assertEquals("Kas Besar", value.category)
        assertEquals("catatan", value.note)
    }

    @Test
    fun blankNoteBecomesNull() {
        val result = validator.validate(input(noteInput = "   "))

        assertNull((result as OperationalExpenseValidationResult.Valid).value.note)
    }

    private fun input(
        amountInput: String = "150000",
        categoryInput: String = "Kas Besar",
        noteInput: String = ""
    ): OperationalExpenseValidationInput {
        return OperationalExpenseValidationInput(amountInput, categoryInput, noteInput)
    }
}
