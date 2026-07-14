package com.sipos.kebabsk.common

import java.text.NumberFormat
import java.util.Locale

object MoneyUtils {
    /**
     * Format Long ke Rupiah. Contoh: 15000 -> "Rp 15.000"
     */
    fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("id").setRegion("ID").build())
        formatter.maximumFractionDigits = 0
        return formatter.format(amount).replace("Rp", "Rp ").replace(",00", "")
    }

    /**
     * Konversi string input (hanya angka) ke Long.
     */
    fun parseRupiahInput(input: String): Long? {
        val sanitized = sanitizeMoneyInput(input)
        return sanitized.toLongOrNull()
    }

    /**
     * Membersihkan input string menyisakan hanya angka
     * Catatan: Karakter minus (-) juga dibuang, karena jumlah tidak boleh negatif.
     */
    fun sanitizeMoneyInput(input: String): String {
        val filtered = input.filter { it.isDigit() }
        // Hapus leading zero kecuali inputnya hanya "0"
        return if (filtered.startsWith("0") && filtered.length > 1) {
            filtered.trimStart('0').ifEmpty { "0" }
        } else {
            filtered
        }
    }

    /**
     * Memformat string angka untuk input text field (misal: 100000 -> 100.000)
     */
    fun formatRupiahInputForDisplay(input: String): String {
        val clean = sanitizeMoneyInput(input)
        if (clean.isBlank()) return ""
        return clean.reversed().chunked(3).joinToString(".").reversed()
    }
}
