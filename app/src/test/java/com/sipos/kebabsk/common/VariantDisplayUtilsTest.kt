package com.sipos.kebabsk.common

import org.junit.Assert.assertEquals
import org.junit.Test

class VariantDisplayUtilsTest {

    @Test
    fun `formatVariantName handles Kebab + Kebab Besar`() {
        assertEquals("Besar", VariantDisplayUtils.formatVariantName("Kebab", "Kebab Besar"))
    }

    @Test
    fun `formatVariantName handles Kebab + Kebab-Besar`() {
        assertEquals("Besar", VariantDisplayUtils.formatVariantName("Kebab", "Kebab-Besar"))
    }

    @Test
    fun `formatVariantName handles Kebab + Kebab - Besar`() {
        assertEquals("Besar", VariantDisplayUtils.formatVariantName("Kebab", "Kebab - Besar"))
    }

    @Test
    fun `formatVariantName handles Kebab + Kebab colon Besar`() {
        assertEquals("Besar", VariantDisplayUtils.formatVariantName("Kebab", "Kebab: Besar"))
    }

    @Test
    fun `formatVariantName retains Kebabish when menu is Kebab`() {
        assertEquals("Kebabish", VariantDisplayUtils.formatVariantName("Kebab", "Kebabish"))
    }

    @Test
    fun `formatVariantName retains Kebab when variant is Kebab`() {
        assertEquals("Kebab", VariantDisplayUtils.formatVariantName("Kebab", "Kebab"))
    }

    @Test
    fun `formatVariantName retains original variant when menu is empty`() {
        assertEquals("Original Variant", VariantDisplayUtils.formatVariantName("", "Original Variant"))
    }

    @Test
    fun `formatVariantName returns empty string when variant is empty`() {
        assertEquals("", VariantDisplayUtils.formatVariantName("Kebab", ""))
    }

    @Test
    fun `formatVariantName handles case-insensitive prefix`() {
        assertEquals("Besar", VariantDisplayUtils.formatVariantName("kebab", "KEBAB Besar"))
    }

    @Test
    fun `formatVariantName retains variant if names do not match`() {
        assertEquals("Burger Besar", VariantDisplayUtils.formatVariantName("Kebab", "Burger Besar"))
    }
}
