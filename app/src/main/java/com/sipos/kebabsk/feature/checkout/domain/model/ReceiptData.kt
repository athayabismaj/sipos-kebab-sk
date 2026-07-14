package com.sipos.kebabsk.feature.checkout.domain.model

data class ReceiptData(
    val transactionCode: String,
    val cashierName: String,
    val items: List<ReceiptItem>,
    val totalAmount: Long,
    val paidAmount: Long,
    val changeAmount: Long,
    val paymentMethodName: String?,
    val note: String?,
    val createdAt: String
)

data class ReceiptItem(
    val menuName: String,
    val variantName: String,
    val quantity: Int,
    val unitPrice: Long
) {
    val subtotal: Long
        get() = unitPrice * quantity
}
