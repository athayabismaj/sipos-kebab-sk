package com.sipos.kebabsk.feature.checkout.domain

import com.sipos.kebabsk.feature.checkout.domain.model.ReceiptData
import com.sipos.kebabsk.feature.checkout.domain.model.ReceiptItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptBuilderTest {

    private val builder = ReceiptBuilder()

    @Test
    fun buildText_containsCoreReceiptFields() {
        val receipt = sampleReceipt()

        val text = builder.buildText(receipt)

        assertTrue(text.contains("KEBAB SK"))
        assertTrue(text.contains("Kebab"))
        assertTrue(text.contains("Kecil"))
        assertTrue(text.contains("2x Rp 20.000"))
        assertTrue(text.contains("Total: Rp 40.000"))
        assertTrue(text.endsWith("\n"))
    }

    @Test
    fun buildEscPos_containsHeaderTransactionItemAndAmounts() {
        val receipt = sampleReceipt()

        val text = builder.buildEscPos(receipt).toString(Charsets.ISO_8859_1)

        assertTrue(text.startsWith("\u001B@"))
        assertTrue(text.contains("KEBAB SK"))
        assertTrue(text.contains("05 Jul 2026, 17:04"))
        assertTrue(text.contains("TRX-001"))
        assertTrue(text.contains("Kebab"))
        assertTrue(text.contains("Kecil"))
        assertTrue(text.contains("2x Rp 20.000"))
        assertTrue(text.contains("Total Belanja"))
        assertTrue(text.contains("Rp 40.000"))
        assertTrue(text.contains("Tunai/Dibayar"))
        assertTrue(text.contains("Rp 50.000"))
        assertTrue(text.contains("Kembalian"))
        assertTrue(text.contains("Rp 10.000"))
        assertTrue(text.contains("Terima kasih"))
    }

    @Test
    fun buildEscPos_returnsNonEmptyBytes() {
        assertTrue(builder.buildEscPos(sampleReceipt()).isNotEmpty())
    }

    @Test
    fun receiptData_keepsPaymentMethodAndNoteForCallers() {
        val receipt = sampleReceipt()

        assertEquals("Tunai", receipt.paymentMethodName)
        assertEquals("Tanpa pedas", receipt.note)
        assertEquals("Cahyo", receipt.cashierName)
    }

    private fun sampleReceipt(): ReceiptData {
        return ReceiptData(
            transactionCode = "TRX-001",
            cashierName = "Cahyo",
            items = listOf(
                ReceiptItem(
                    menuName = "Kebab",
                    variantName = "Kecil",
                    quantity = 2,
                    unitPrice = 20_000L
                )
            ),
            totalAmount = 40_000L,
            paidAmount = 50_000L,
            changeAmount = 10_000L,
            paymentMethodName = "Tunai",
            note = "Tanpa pedas",
            createdAt = "05 Jul 2026, 17:04"
        )
    }
}
