package com.sipos.kebabsk.feature.dailystock.domain.validation

import com.sipos.kebabsk.common.validation.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyStockValidatorTest {
    private val validator = DailyStockValidator()

    @Test
    fun nullQuantityIsInvalid() {
        assertTrue(validator.validateRemainingQuantity(null) is ValidationResult.Invalid)
    }

    @Test
    fun negativeQuantityIsInvalid() {
        assertTrue(validator.validateRemainingQuantity(-0.01) is ValidationResult.Invalid)
    }

    @Test
    fun zeroQuantityIsValid() {
        assertEquals(ValidationResult.Valid, validator.validateRemainingQuantity(0.0))
    }

    @Test
    fun positiveIntegerIsValid() {
        assertEquals(ValidationResult.Valid, validator.validateRemainingQuantity(2.0))
    }

    @Test
    fun positiveDecimalIsValid() {
        assertEquals(ValidationResult.Valid, validator.validateRemainingQuantity(0.25))
    }

    @Test
    fun nanIsInvalid() {
        assertTrue(validator.validateRemainingQuantity(Double.NaN) is ValidationResult.Invalid)
    }

    @Test
    fun positiveInfinityIsInvalid() {
        assertTrue(validator.validateRemainingQuantity(Double.POSITIVE_INFINITY) is ValidationResult.Invalid)
    }

    @Test
    fun negativeInfinityIsInvalid() {
        assertTrue(validator.validateRemainingQuantity(Double.NEGATIVE_INFINITY) is ValidationResult.Invalid)
    }
}
