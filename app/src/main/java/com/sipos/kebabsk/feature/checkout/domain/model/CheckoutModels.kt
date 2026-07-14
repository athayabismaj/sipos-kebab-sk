package com.sipos.kebabsk.feature.checkout.domain.model

data class PaymentMethod(
    val id: Long,
    val name: String
)

data class CheckoutRequestData(
    val paymentMethodId: Long,
    val paidAmount: Long,
    val items: List<CheckoutItemInput>,
    val note: String?
)

data class CheckoutItemInput(
    val variantId: Long,
    val qty: Int
)

data class CheckoutResult(
    val transactionId: Long,
    val transactionCode: String,
    val totalAmount: Long,
    val paidAmount: Long,
    val changeAmount: Long
)
