package com.sipos.kebabsk.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThermalTextFormatterTest {

    @Test
    fun wrap_keepsEveryAddressWordWithinPrinterWidth() {
        val address = "Jl. Raya Gulang Cilik, Pekeng, Kecamatan Mejobo, Kabupaten Kudus"

        val lines = ThermalTextFormatter.wrap(address, 32)

        assertEquals(address, lines.joinToString(" "))
        assertTrue(lines.all { it.length <= 32 })
        assertTrue(lines.size > 1)
    }

    @Test
    fun wrap_splitsSingleTokenThatExceedsPrinterWidth() {
        val lines = ThermalTextFormatter.wrap("ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789", 32)

        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789", lines.joinToString(""))
        assertTrue(lines.all { it.length <= 32 })
    }
}
