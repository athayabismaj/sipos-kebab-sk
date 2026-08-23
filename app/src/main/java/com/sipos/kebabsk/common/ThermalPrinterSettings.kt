package com.sipos.kebabsk.common

import android.content.Context
import androidx.core.content.edit

enum class ThermalPaperSize(
    val storageValue: String,
    val label: String,
    val charactersPerLine: Int,
    val description: String
) {
    MM_58(
        storageValue = "58_mm",
        label = "58 mm",
        charactersPerLine = 32,
        description = "Printer kasir mini"
    ),
    MM_80(
        storageValue = "80_mm",
        label = "80 mm",
        charactersPerLine = 48,
        description = "Printer kasir lebar"
    );

    companion object {
        fun fromStorage(value: String?): ThermalPaperSize {
            return entries.firstOrNull { it.storageValue == value } ?: MM_58
        }
    }
}

object ThermalPrinterPreferences {
    private const val PREF_NAME = "printer_prefs"
    private const val KEY_PAPER_SIZE = "printer_paper_size"

    fun loadPaperSize(context: Context): ThermalPaperSize {
        val value = context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PAPER_SIZE, null)
        return ThermalPaperSize.fromStorage(value)
    }

    fun savePaperSize(context: Context, size: ThermalPaperSize) {
        context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_PAPER_SIZE, size.storageValue) }
    }
}
