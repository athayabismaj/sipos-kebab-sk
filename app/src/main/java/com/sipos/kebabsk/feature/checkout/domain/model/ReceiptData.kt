package com.sipos.kebabsk.feature.checkout.domain.model

import com.sipos.kebabsk.common.validation.safeMultiply

data class ReceiptData(
    val transactionCode: String,
    val cashierName: String,
    val branchAddress: String?,
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
        get() = if (quantity >= 0 && unitPrice >= 0L) {
            safeMultiply(unitPrice, quantity) ?: Long.MAX_VALUE
        } else {
            0L
        }
}
