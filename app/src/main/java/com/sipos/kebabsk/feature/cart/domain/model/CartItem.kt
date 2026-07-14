package com.sipos.kebabsk.feature.cart.domain.model

import com.sipos.kebabsk.common.validation.safeMultiply

data class CartItem(
    val variantId: Long,
    val menuName: String,
    val variantName: String,
    val quantity: Int,
    val unitPrice: Long
) {
    val safeSubtotal: Long?
        get() = if (quantity >= 0 && unitPrice >= 0L) safeMultiply(unitPrice, quantity) else null

    val subtotal: Long
        get() = safeSubtotal ?: Long.MAX_VALUE

    val qty: Int
        get() = quantity

    val price: Long
        get() = unitPrice
}
