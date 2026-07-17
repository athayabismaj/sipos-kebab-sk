package com.sipos.kebabsk.feature.checkout.domain

import com.sipos.kebabsk.feature.checkout.domain.model.ReceiptData
import com.sipos.kebabsk.feature.checkout.domain.model.ReceiptItem
import com.sipos.kebabsk.testutil.ContractFixtureLoader
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
        assertTrue(text.contains("Jl. Kampus UMK, Kudus"))
        assertTrue(text.contains("TRX-0001"))
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
        assertTrue(text.contains("Jl. Kampus UMK, Kudus"))
        assertTrue(text.contains("05 Jul 2026, 17:04"))
        assertTrue(text.contains("TRX-0001"))
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
        assertEquals("Jl. Kampus UMK, Kudus", receipt.branchAddress)
    }

    @Test
    fun shareAndPrinterUseTheSameShortCodeAndTransactionBranchAddress() {
        val receipt = sampleReceipt().copy(transactionCode = "TRX-UMK-20260717-0001")

        val sharedText = builder.buildText(receipt)
        val printerText = builder.buildEscPos(receipt).toString(Charsets.ISO_8859_1)

        listOf(sharedText, printerText).forEach { output ->
            assertTrue(output.contains("TRX-0001"))
            assertTrue(output.contains("Jl. Kampus UMK, Kudus"))
            assertTrue(!output.contains("Alamat Cabang Pengguna Lain"))
            assertTrue(!output.contains("TRX-UMK-20260717-0001"))
        }
    }

    @Test
    fun nullBranchAddressIsSafeAndDoesNotAddAnAddressLine() {
        val receipt = sampleReceipt().copy(branchAddress = null)

        val sharedText = builder.buildText(receipt)
        val printerText = builder.buildEscPos(receipt).toString(Charsets.ISO_8859_1)

        assertTrue(!sharedText.contains("Jl. Kampus UMK, Kudus"))
        assertTrue(!printerText.contains("Jl. Kampus UMK, Kudus"))
    }

    @Test
    fun backendReceiptFixture_formatsLongItemAndLargeAmountsWithoutOverflow() {
        val data = ContractFixtureLoader.jsonObject("receipt.json").getAsJsonObject("data")
        val item = data.getAsJsonArray("items").single().asJsonObject
        val receipt = ReceiptData(
            transactionCode = data.get("transaction_code").asString,
            cashierName = "Kasir Fixture",
            branchAddress = data.getAsJsonObject("branch").get("address").asString,
            items = listOf(
                ReceiptItem(
                    menuName = item.get("menu_name").asString,
                    variantName = item.get("variant_name").asString,
                    quantity = item.get("qty").asInt,
                    unitPrice = item.get("price").asLong
                )
            ),
            totalAmount = data.get("total_amount").asLong,
            paidAmount = data.get("paid_amount").asLong,
            changeAmount = data.get("change_amount").asLong,
            paymentMethodName = data.get("payment_method_name").asString,
            note = null,
            createdAt = data.get("created_at").asString
        )

        val plainText = builder.buildText(receipt)
        val printerText = builder.buildEscPos(receipt).toString(Charsets.ISO_8859_1)

        assertTrue(plainText.contains("Kebab Spesial Keju Mozzarella Panjang"))
        assertTrue(plainText.contains("Rp 1.250.000"))
        assertTrue(printerText.contains("TRX-003"))
        assertTrue(printerText.contains("Rp 1.300.000"))
        assertTrue(printerText.contains("Rp 50.000"))
    }

    @Test
    fun buildEscPos_nonAsciiItemDoesNotCrashPrinterEncoding() {
        val receipt = sampleReceipt().copy(
            items = listOf(
                ReceiptItem(
                    menuName = "Kebab Jalapeño",
                    variantName = "Spesial",
                    quantity = 12,
                    unitPrice = 250_000L
                )
            ),
            totalAmount = 3_000_000L,
            paidAmount = 3_100_000L,
            changeAmount = 100_000L
        )

        assertTrue(builder.buildEscPos(receipt).isNotEmpty())
    }

    private fun sampleReceipt(): ReceiptData {
        return ReceiptData(
            transactionCode = "TRX-UMK-20260717-0001",
            cashierName = "Cahyo",
            branchAddress = "Jl. Kampus UMK, Kudus",
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
