package com.sipos.kebabsk.feature.checkout.domain

import com.sipos.kebabsk.common.MoneyUtils
import com.sipos.kebabsk.common.ThermalTextFormatter
import com.sipos.kebabsk.common.TransactionCodeFormatter
import com.sipos.kebabsk.common.VariantDisplayUtils
import com.sipos.kebabsk.feature.checkout.domain.model.ReceiptData
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

class ReceiptBuilder {

    fun buildText(receipt: ReceiptData): String {
        val sb = StringBuilder()
        sb.append("KEBAB SK\n")
        receipt.branchAddress?.trim()?.takeIf { it.isNotEmpty() }?.let { sb.append("$it\n") }
        sb.append("-----------------------------\n")
        sb.append("No. ${TransactionCodeFormatter.formatForDisplay(receipt.transactionCode)}\n")
        sb.append("Kasir ${receipt.cashierName}\n")
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
        sb.append("Sub Total: ${MoneyUtils.formatRupiah(receipt.totalAmount)}\n")
        sb.append("Total: ${MoneyUtils.formatRupiah(receipt.totalAmount)}\n")
        sb.append("Bayar: ${MoneyUtils.formatRupiah(receipt.paidAmount)}\n")
        sb.append("Kembalian: ${MoneyUtils.formatRupiah(receipt.changeAmount)}\n")
        sb.append("-----------------------------\n")
        sb.append("Terima kasih telah berbelanja\n")
        sb.append("${receipt.createdAt}\n")
        return sb.toString()
    }

    fun buildEscPos(
        receipt: ReceiptData,
        charactersPerLine: Int = DEFAULT_CHARACTERS_PER_LINE
    ): ByteArray {
        val printerWidth = charactersPerLine.coerceAtLeast(MIN_CHARACTERS_PER_LINE)
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
        fun line() = text("-".repeat(printerWidth))

        command(0x1B, 0x40)
        align(1)
        bold(true)
        size(0x11)
        text("KEBAB SK")
        size(0x00)
        bold(false)
        receipt.branchAddress
            ?.trim()
            ?.takeIf(String::isNotEmpty)
            ?.let { address ->
                ThermalTextFormatter.wrap(address, printerWidth).forEach(::text)
            }
        align(0)
        line()
        text(receiptColumns("No.", TransactionCodeFormatter.formatForDisplay(receipt.transactionCode), printerWidth))
        text(receiptColumns("Kasir", receipt.cashierName, printerWidth))
        line()

        receipt.items.forEach { item ->
            val hasVariant = !item.variantName.equals("Regular", ignoreCase = true) &&
                !item.variantName.equals("Default", ignoreCase = true)
            val displayVariant = VariantDisplayUtils.formatVariantName(item.menuName, item.variantName)

            bold(true)
            text(item.menuName.take(printerWidth))
            bold(false)
            if (hasVariant) {
                text(displayVariant.take(printerWidth))
            }
            text(receiptColumns("${item.quantity}x ${MoneyUtils.formatRupiah(item.unitPrice)}", MoneyUtils.formatRupiah(item.subtotal), printerWidth))
        }

        line()
        bold(true)
        text(receiptColumns("Total Belanja", MoneyUtils.formatRupiah(receipt.totalAmount), printerWidth))
        bold(false)
        text(receiptColumns("Tunai/Dibayar", MoneyUtils.formatRupiah(receipt.paidAmount), printerWidth))
        line()
        align(1)
        text("Kembalian")
        bold(true)
        size(0x11)
        text(MoneyUtils.formatRupiah(receipt.changeAmount))
        size(0x00)
        bold(false)
        text("Terima kasih")
        text(receipt.createdAt.take(printerWidth))
        text()
        text()
        command(0x1D, 0x56, 0x42, 0x00)

        return buffer.toByteArray()
    }

    private fun receiptColumns(left: String, right: String, printerWidth: Int): String {
        val safeLeft = left.take(printerWidth)
        val safeRight = right.take(printerWidth)
        val spaces = (printerWidth - safeLeft.length - safeRight.length).coerceAtLeast(1)
        return safeLeft + " ".repeat(spaces) + safeRight
    }

    private companion object {
        const val DEFAULT_CHARACTERS_PER_LINE = 32
        const val MIN_CHARACTERS_PER_LINE = 16
    }
}
