package com.sipos.kebabsk.common

import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionCodeFormatterTest {
    @Test
    fun fullTransactionCodeKeepsCompleteSequenceSuffix() {
        assertEquals(
            "TRX-0001",
            TransactionCodeFormatter.formatForDisplay("TRX-UMK-20260717-0001")
        )
    }

    @Test
    fun malformedCodeFallsBackToOriginalWithoutCrashing() {
        val malformed = "TRX-UMK-NOT-A-SEQUENCE"

        assertEquals(malformed, TransactionCodeFormatter.formatForDisplay(malformed))
        assertEquals("", TransactionCodeFormatter.formatForDisplay(""))
    }
}
