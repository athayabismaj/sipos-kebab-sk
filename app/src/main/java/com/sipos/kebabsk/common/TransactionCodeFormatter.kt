package com.sipos.kebabsk.common

object TransactionCodeFormatter {
    fun formatForDisplay(originalCode: String): String {
        val code = originalCode.trim()
        val segments = code.split('-')
        val sequence = segments.lastOrNull().orEmpty()
        val hasExpectedPrefix = segments.firstOrNull().equals("TRX", ignoreCase = true)

        return if (hasExpectedPrefix && segments.size >= 4 && sequence.isNotEmpty() && sequence.all(Char::isDigit)) {
            "TRX-$sequence"
        } else {
            originalCode
        }
    }
}
