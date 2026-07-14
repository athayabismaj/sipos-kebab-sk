package com.sipos.kebabsk.feature.checkout.domain.validation

import com.sipos.kebabsk.common.MoneyUtils
import com.sipos.kebabsk.common.validation.ValidationResult
import com.sipos.kebabsk.feature.cart.domain.model.CartItem
import com.sipos.kebabsk.feature.cart.domain.validation.CartValidator

data class CheckoutValidationInput(
    val cartItems: List<CartItem>,
    val isDailySessionOpen: Boolean,
    val paymentMethodId: Long?,
    val paidAmountInput: String
)

data class CheckoutValidationSuccess(
    val paymentMethodId: Long,
    val paidAmount: Long,
    val totalAmount: Long
)

sealed interface CheckoutValidationResult {
    data class Valid(val value: CheckoutValidationSuccess) : CheckoutValidationResult
    data class Invalid(val message: String) : CheckoutValidationResult
}

class CheckoutValidator(
    private val cartValidator: CartValidator = CartValidator()
) {
    fun validate(input: CheckoutValidationInput): CheckoutValidationResult {
        when (val cartValidation = cartValidator.validate(input.cartItems)) {
            ValidationResult.Valid -> Unit
            is ValidationResult.Invalid -> return CheckoutValidationResult.Invalid(cartValidation.message)
        }

        if (!input.isDailySessionOpen) {
            return CheckoutValidationResult.Invalid("Sesi harian belum dibuka admin. Checkout belum bisa dilakukan.")
        }

        val paymentMethodId = input.paymentMethodId
            ?: return CheckoutValidationResult.Invalid("Metode pembayaran tunai belum tersedia")

        val paidAmount = MoneyUtils.parseRupiahInput(input.paidAmountInput)
            ?: return CheckoutValidationResult.Invalid("Nominal pembayaran tidak valid.")

        if (paidAmount <= 0L) {
            return CheckoutValidationResult.Invalid("Nominal pembayaran tidak valid.")
        }

        val totalAmount = cartValidator.calculateTotal(input.cartItems)
            ?: return CheckoutValidationResult.Invalid("Total keranjang terlalu besar. Silakan periksa kembali.")

        if (paidAmount < totalAmount) {
            return CheckoutValidationResult.Invalid("Nominal pembayaran kurang. Silakan periksa kembali.")
        }

        return CheckoutValidationResult.Valid(
            CheckoutValidationSuccess(
                paymentMethodId = paymentMethodId,
                paidAmount = paidAmount,
                totalAmount = totalAmount
            )
        )
    }
}
