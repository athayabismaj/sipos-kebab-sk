package com.sipos.kebabsk.feature.cart.domain.validation

import com.sipos.kebabsk.common.validation.ValidationResult
import com.sipos.kebabsk.feature.cart.domain.model.CartItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CartValidatorTest {
    private val validator = CartValidator()

    @Test
    fun emptyCartIsInvalid() {
        assertTrue(validator.validate(emptyList()) is ValidationResult.Invalid)
    }

    @Test
    fun quantityZeroIsInvalid() {
        val result = validator.validate(listOf(item(quantity = 0)))
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun quantityNegativeIsInvalid() {
        val result = validator.validate(listOf(item(quantity = -1)))
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun negativeUnitPriceIsInvalid() {
        val result = validator.validate(listOf(item(unitPrice = -1L)))
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun duplicateVariantIsInvalid() {
        val result = validator.validate(listOf(item(variantId = 1L), item(variantId = 1L)))
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun subtotalOverflowIsInvalid() {
        val result = validator.validate(listOf(item(unitPrice = Long.MAX_VALUE, quantity = 2)))
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun totalOverflowIsInvalid() {
        val result = validator.validate(
            listOf(
                item(variantId = 1L, unitPrice = Long.MAX_VALUE, quantity = 1),
                item(variantId = 2L, unitPrice = 1L, quantity = 1)
            )
        )
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validCartReturnsValidAndTotal() {
        val items = listOf(item(variantId = 1L, quantity = 2), item(variantId = 2L, unitPrice = 5_000L))

        assertEquals(25_000L, validator.calculateTotal(items))
        assertEquals(ValidationResult.Valid, validator.validate(items))
    }

    private fun item(
        variantId: Long = 1L,
        quantity: Int = 1,
        unitPrice: Long = 10_000L
    ): CartItem {
        return CartItem(
            variantId = variantId,
            menuName = "Kebab",
            variantName = "Mini",
            quantity = quantity,
            unitPrice = unitPrice
        )
    }
}
