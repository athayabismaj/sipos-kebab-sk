package com.sipos.kebabsk.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyUtilsTest {

    @Test
    fun `formatRupiah formats correctly`() {
        assertEquals("Rp 15.000", MoneyUtils.formatRupiah(15000))
        assertEquals("Rp 0", MoneyUtils.formatRupiah(0))
    }

    @Test
    fun `parseRupiahInput returns Long correctly`() {
        assertEquals(15000L, MoneyUtils.parseRupiahInput("Rp 15.000"))
        assertEquals(15000L, MoneyUtils.parseRupiahInput("15000"))
    }

    @Test
    fun `parseRupiahInput handles empty input`() {
        assertNull(MoneyUtils.parseRupiahInput(""))
        assertNull(MoneyUtils.parseRupiahInput("   "))
    }

    @Test
    fun `parseRupiahInput handles only letters`() {
        assertNull(MoneyUtils.parseRupiahInput("abc"))
    }

    @Test
    fun `sanitizeMoneyInput removes non digits`() {
        assertEquals("15000", MoneyUtils.sanitizeMoneyInput("Rp 15.000"))
        assertEquals("15000", MoneyUtils.sanitizeMoneyInput("abc15.000xyz"))
    }

    @Test
    fun `sanitizeMoneyInput removes leading zeros`() {
        assertEquals("15000", MoneyUtils.sanitizeMoneyInput("00015000"))
        assertEquals("0", MoneyUtils.sanitizeMoneyInput("0"))
        assertEquals("0", MoneyUtils.sanitizeMoneyInput("0000"))
    }

    @Test
    fun `parseRupiahInput handles Long MAX_VALUE`() {
        val maxString = Long.MAX_VALUE.toString()
        assertEquals(Long.MAX_VALUE, MoneyUtils.parseRupiahInput(maxString))
    }

    @Test
    fun `parseRupiahInput returns null on overflow`() {
        val overflowString = "999999999999999999999"
        assertNull(MoneyUtils.parseRupiahInput(overflowString))
    }

    @Test
    fun `parseRupiahInput rejects negative text`() {
        assertNull(MoneyUtils.parseRupiahInput("-10000"))
        assertNull(MoneyUtils.parseRupiahInput("Rp -10.000"))
    }

    @Test
    fun `formatRupiahInputForDisplay formats correctly`() {
        assertEquals("100.000", MoneyUtils.formatRupiahInputForDisplay("100000"))
        assertEquals("100.000", MoneyUtils.formatRupiahInputForDisplay("00100000"))
        assertEquals("", MoneyUtils.formatRupiahInputForDisplay(""))
        assertEquals("0", MoneyUtils.formatRupiahInputForDisplay("0"))
    }
}
