package com.sipos.kebabsk.feature.cart.domain.validation

import com.sipos.kebabsk.common.validation.ValidationResult
import com.sipos.kebabsk.common.validation.safeAdd
import com.sipos.kebabsk.common.validation.safeMultiply
import com.sipos.kebabsk.feature.cart.domain.model.CartItem

class CartValidator {
    fun validate(items: List<CartItem>): ValidationResult {
        if (items.isEmpty()) {
            return ValidationResult.Invalid("Keranjang masih kosong")
        }

        val seenVariantIds = mutableSetOf<Long>()
        for (item in items) {
            if (item.variantId <= 0L) {
                return ValidationResult.Invalid("Keranjang tidak valid. Silakan pilih ulang menu.")
            }
            if (!seenVariantIds.add(item.variantId)) {
                return ValidationResult.Invalid("Keranjang tidak valid. Silakan pilih ulang menu.")
            }
            if (item.quantity < 1) {
                return ValidationResult.Invalid("Jumlah item tidak valid.")
            }
            if (item.unitPrice < 0L) {
                return ValidationResult.Invalid("Harga item tidak valid.")
            }
            if (calculateSubtotal(item) == null) {
                return ValidationResult.Invalid("Total keranjang terlalu besar. Silakan periksa kembali.")
            }
        }

        if (calculateTotal(items) == null) {
            return ValidationResult.Invalid("Total keranjang terlalu besar. Silakan periksa kembali.")
        }

        return ValidationResult.Valid
    }

    fun calculateSubtotal(item: CartItem): Long? {
        if (item.quantity < 0) return null
        if (item.unitPrice < 0L) return null
        return safeMultiply(item.unitPrice, item.quantity)
    }

    fun calculateTotal(items: List<CartItem>): Long? {
        var total = 0L
        for (item in items) {
            val subtotal = calculateSubtotal(item) ?: return null
            total = safeAdd(total, subtotal) ?: return null
        }
        return total
    }
}
