package com.sipos.kebabsk.feature.cart.domain.model

data class CartItem(
    val variantId: Long,
    val menuName: String,
    val variantName: String,
    val quantity: Int,
    val unitPrice: Long
) {
    val subtotal: Long
        get() = unitPrice * quantity

    val qty: Int
        get() = quantity

    val price: Long
        get() = unitPrice
}
