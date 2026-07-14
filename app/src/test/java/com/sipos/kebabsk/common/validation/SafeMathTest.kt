package com.sipos.kebabsk.common.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SafeMathTest {
    @Test
    fun safeMultiplyNormalReturnsValue() {
        assertEquals(20_000L, safeMultiply(10_000L, 2))
    }

    @Test
    fun safeAddNormalReturnsValue() {
        assertEquals(30_000L, safeAdd(10_000L, 20_000L))
    }

    @Test
    fun safeMultiplyOverflowReturnsNull() {
        assertNull(safeMultiply(Long.MAX_VALUE, 2))
    }

    @Test
    fun safeAddOverflowReturnsNull() {
        assertNull(safeAdd(Long.MAX_VALUE, 1L))
    }

    @Test
    fun safeMathHandlesZero() {
        assertEquals(0L, safeMultiply(10_000L, 0))
        assertEquals(10_000L, safeAdd(10_000L, 0L))
    }
}
