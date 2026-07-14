package com.sipos.kebabsk.feature.shift.domain.validation

import com.sipos.kebabsk.common.validation.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CloseShiftValidatorTest {
    private val validator = CloseShiftValidator()

    @Test
    fun missingSessionIsInvalid() {
        assertTrue(validator.validate(null, isReadyToClose = true, actualPhysicalCash = 0L) is ValidationResult.Invalid)
    }

    @Test
    fun notReadyIsInvalid() {
        assertTrue(validator.validate(1L, isReadyToClose = false, actualPhysicalCash = 0L) is ValidationResult.Invalid)
    }

    @Test
    fun zeroCashIsValid() {
        assertEquals(ValidationResult.Valid, validator.validate(1L, isReadyToClose = true, actualPhysicalCash = 0L))
    }

    @Test
    fun negativeCashIsInvalid() {
        assertTrue(validator.validate(1L, isReadyToClose = true, actualPhysicalCash = -1L) is ValidationResult.Invalid)
    }

    @Test
    fun parseCashOverflowReturnsNull() {
        assertNull(validator.parseActualPhysicalCash("999999999999999999999"))
    }
}
