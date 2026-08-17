package com.sipos.kebabsk.feature.menu.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class MenuUiHelpersTest {

    @Test
    fun `variant is placed beside menu name`() {
        assertEquals(
            "Kebab Original Mini",
            buildMenuVariantTitle("Kebab Original", "Mini")
        )
    }

    @Test
    fun `shared product prefix is not repeated`() {
        assertEquals(
            "Kebab Original Mini",
            buildMenuVariantTitle("Kebab Original", "Kebab Mini")
        )
    }

    @Test
    fun `identical variant name is not repeated`() {
        assertEquals(
            "Kebab Original",
            buildMenuVariantTitle("Kebab Original", "Kebab Original")
        )
    }
}
