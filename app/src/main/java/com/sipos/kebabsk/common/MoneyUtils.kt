package com.sipos.kebabsk.common

import java.text.NumberFormat
import java.util.Locale

object MoneyUtils {
    /**
     * Format Long ke Rupiah. Contoh: 15000 -> "Rp 15.000"
     */
    fun toRupiah(amount: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount).replace("Rp", "Rp ")
    }

    /**
     * Konversi string input (hanya angka) ke Long.
     */
    fun parseMoneyInput(input: String): Long {
        val sanitized = input.filter { it.isDigit() }
        return sanitized.toLongOrNull() ?: 0L
    }
}
