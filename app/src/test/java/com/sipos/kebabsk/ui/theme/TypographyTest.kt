package com.sipos.kebabsk.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypographyTest {
    @Test
    fun typographyUsesSystemFontFallback() {
        assertEquals(FontFamily.SansSerif, AppFontFamily)
    }

    @Test
    fun primaryTypographyStylesHaveSafeLineHeights() {
        val styles = listOf(
            Typography.headlineLarge,
            Typography.headlineMedium,
            Typography.titleLarge,
            Typography.titleMedium,
            Typography.bodyLarge,
            Typography.bodyMedium,
            Typography.labelLarge,
            Typography.labelMedium
        )

        styles.forEach { style ->
            assertPositiveSp(style)
            assertTrue(
                "lineHeight must be >= fontSize for $style",
                style.lineHeight.value >= style.fontSize.value
            )
        }
    }

    private fun assertPositiveSp(style: TextStyle) {
        assertTrue("fontSize must be positive for $style", style.fontSize.value > 0f)
        assertTrue("lineHeight must be positive for $style", style.lineHeight.value > 0f)
    }
}
