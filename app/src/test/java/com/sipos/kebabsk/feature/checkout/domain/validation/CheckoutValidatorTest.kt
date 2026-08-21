package com.sipos.kebabsk.feature.checkout.domain.validation

import com.sipos.kebabsk.feature.cart.domain.model.CartItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutValidatorTest {
    private val validator = CheckoutValidator()

    @Test
    fun validExactPaymentReturnsParsedValues() {
        val result = validator.validate(input(paidAmountInput = "12000"))

        assertTrue(result is CheckoutValidationResult.Valid)
        val value = (result as CheckoutValidationResult.Valid).value
        assertEquals(12_000L, value.paidAmount)
        assertEquals(12_000L, value.totalAmount)
    }

    @Test
    fun validPaymentWithChangeReturnsParsedValues() {
        val result = validator.validate(input(paidAmountInput = "15000"))

        assertTrue(result is CheckoutValidationResult.Valid)
        assertEquals(15_000L, (result as CheckoutValidationResult.Valid).value.paidAmount)
    }

    @Test
    fun emptyAmountIsInvalidAmountNotDeficit() {
        val result = validator.validate(input(paidAmountInput = ""))

        assertEquals("Nominal pembayaran tidak valid.", (result as CheckoutValidationResult.Invalid).message)
    }

    @Test
    fun malformedAmountIsInvalidAmountNotDeficit() {
        val result = validator.validate(input(paidAmountInput = "abc"))

        assertEquals("Nominal pembayaran tidak valid.", (result as CheckoutValidationResult.Invalid).message)
    }

    @Test
    fun zeroAmountIsInvalid() {
        val result = validator.validate(input(paidAmountInput = "0"))

        assertEquals("Nominal pembayaran tidak valid.", (result as CheckoutValidationResult.Invalid).message)
    }

    @Test
    fun paymentDeficitKeepsFriendlyMessage() {
        val result = validator.validate(input(paidAmountInput = "10000"))

        assertEquals(
            "Nominal pembayaran kurang. Silakan periksa kembali.",
            (result as CheckoutValidationResult.Invalid).message
        )
    }

    @Test
    fun qrisUsesExactCartTotalWithoutCashInput() {
        val result = validator.validate(
            input(paidAmountInput = "", requiresCashAmount = false)
        )

        assertTrue(result is CheckoutValidationResult.Valid)
        val value = (result as CheckoutValidationResult.Valid).value
        assertEquals(12_000L, value.paidAmount)
        assertEquals(12_000L, value.totalAmount)
    }

    @Test
    fun totalOverflowIsInvalid() {
        val result = validator.validate(
            input(
                cartItems = listOf(
                    cartItem(1L, Long.MAX_VALUE),
                    cartItem(2L, 1L)
                ),
                paidAmountInput = Long.MAX_VALUE.toString()
            )
        )

        assertEquals(
            "Total keranjang terlalu besar. Silakan periksa kembali.",
            (result as CheckoutValidationResult.Invalid).message
        )
    }

    private fun input(
        cartItems: List<CartItem> = listOf(cartItem()),
        isDailySessionOpen: Boolean = true,
        paymentMethodId: Long? = 1L,
        paidAmountInput: String = "12000",
        requiresCashAmount: Boolean = true
    ): CheckoutValidationInput {
        return CheckoutValidationInput(
            cartItems = cartItems,
            isDailySessionOpen = isDailySessionOpen,
            paymentMethodId = paymentMethodId,
            paidAmountInput = paidAmountInput,
            requiresCashAmount = requiresCashAmount
        )
    }

    private fun cartItem(variantId: Long = 11L, unitPrice: Long = 12_000L): CartItem {
        return CartItem(
            variantId = variantId,
            menuName = "Burger",
            variantName = "Single",
            quantity = 1,
            unitPrice = unitPrice
        )
    }
}
