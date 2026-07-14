package com.sipos.kebabsk.feature.checkout.domain

interface ReceiptPrinter {
    suspend fun print(data: ByteArray): Result<Unit>
}
