package com.sipos.kebabsk.feature.checkout.domain

import com.sipos.kebabsk.common.MoneyUtils
import com.sipos.kebabsk.common.VariantDisplayUtils
import com.sipos.kebabsk.feature.checkout.domain.model.ReceiptData
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

class ReceiptBuilder {

    fun buildText(receipt: ReceiptData): String {
        val sb = StringBuilder()
        sb.append("KEBAB SK\n")
        sb.append("-----------------------------\n")
        receipt.items.forEach { item ->
            val hasVariant = !item.variantName.equals("Regular", ignoreCase = true) &&
                !item.variantName.equals("Default", ignoreCase = true)
            val displayVariant = VariantDisplayUtils.formatVariantName(item.menuName, item.variantName)
            val totalStr = MoneyUtils.formatRupiah(item.subtotal)

            if (hasVariant) {
                sb.append("${item.menuName}\n")
                val spaces = " ".repeat(maxOf(1, 29 - displayVariant.length - totalStr.length))
                sb.append("$displayVariant$spaces$totalStr\n")
            } else {
                val spaces = " ".repeat(maxOf(1, 29 - item.menuName.length - totalStr.length))
                sb.append("${item.menuName}$spaces$totalStr\n")
            }

            sb.append("${item.quantity}x ${MoneyUtils.formatRupiah(item.unitPrice)}\n")
        }
        sb.append("-----------------------------\n")
        sb.append("Total: ${MoneyUtils.formatRupiah(receipt.totalAmount)}\n")
        return sb.toString()
    }

    fun buildEscPos(receipt: ReceiptData): ByteArray {
        val charset = Charset.forName("CP437")
        val buffer = ByteArrayOutputStream()

        fun command(vararg bytes: Int) {
            buffer.write(bytes.map { it.toByte() }.toByteArray())
        }

        fun text(value: String = "") {
            buffer.write(value.toByteArray(charset))
            buffer.write('\n'.code)
        }

        fun align(mode: Int) = command(0x1B, 0x61, mode)
        fun bold(enabled: Boolean) = command(0x1B, 0x45, if (enabled) 1 else 0)
        fun size(mode: Int) = command(0x1D, 0x21, mode)
        fun line() = text("-".repeat(PRINTER_RECEIPT_WIDTH))

        command(0x1B, 0x40)
        align(1)
        bold(true)
        size(0x11)
        text("KEBAB SK")
        size(0x00)
        bold(false)
        text(receipt.createdAt)
        receipt.transactionCode.takeIf { it.isNotBlank() }?.let { text(it) }
        align(0)
        line()

        receipt.items.forEach { item ->
            val hasVariant = !item.variantName.equals("Regular", ignoreCase = true) &&
                !item.variantName.equals("Default", ignoreCase = true)
            val displayVariant = VariantDisplayUtils.formatVariantName(item.menuName, item.variantName)

            bold(true)
            text(item.menuName.take(PRINTER_RECEIPT_WIDTH))
            bold(false)
            if (hasVariant) {
                text(displayVariant.take(PRINTER_RECEIPT_WIDTH))
            }
            text(receiptColumns("${item.quantity}x ${MoneyUtils.formatRupiah(item.unitPrice)}", MoneyUtils.formatRupiah(item.subtotal)))
        }

        line()
        bold(true)
        text(receiptColumns("Total Belanja", MoneyUtils.formatRupiah(receipt.totalAmount)))
        bold(false)
        text(receiptColumns("Tunai/Dibayar", MoneyUtils.formatRupiah(receipt.paidAmount)))
        line()
        align(1)
        text("Kembalian")
        bold(true)
        size(0x11)
        text(MoneyUtils.formatRupiah(receipt.changeAmount))
        size(0x00)
        bold(false)
        text("Terima kasih")
        text()
        text()
        command(0x1D, 0x56, 0x42, 0x00)

        return buffer.toByteArray()
    }

    private fun receiptColumns(left: String, right: String): String {
        val safeLeft = left.take(PRINTER_RECEIPT_WIDTH)
        val safeRight = right.take(PRINTER_RECEIPT_WIDTH)
        val spaces = (PRINTER_RECEIPT_WIDTH - safeLeft.length - safeRight.length).coerceAtLeast(1)
        return safeLeft + " ".repeat(spaces) + safeRight
    }

    private companion object {
        const val PRINTER_RECEIPT_WIDTH = 32
    }
}
