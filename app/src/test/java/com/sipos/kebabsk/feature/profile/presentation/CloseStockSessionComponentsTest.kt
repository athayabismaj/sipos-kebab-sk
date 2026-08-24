package com.sipos.kebabsk.feature.profile.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class CloseStockSessionComponentsTest {
    @Test
    fun reviewConvertsKilogramsAndLitersToSmallerUnits() {
        assertEquals(250.0 to "g", convertUsedQuantityForReview(0.25, "kg"))
        assertEquals(1_500.0 to "ml", convertUsedQuantityForReview(1.5, "liter"))
    }

    @Test
    fun reviewKeepsUnitsThatAreAlreadySmall() {
        assertEquals(125.0 to "g", convertUsedQuantityForReview(125.0, "g"))
        assertEquals(80.0 to "ml", convertUsedQuantityForReview(80.0, "ml"))
    }
}
