package com.sipos.kebabsk.feature.dailystock.domain.validation

import com.sipos.kebabsk.common.validation.ValidationResult
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem

class DailyStockValidator {
    fun validateRemainingQuantity(value: Double?): ValidationResult {
        if (value == null) {
            return ValidationResult.Invalid("Sisa bahan wajib diisi.")
        }
        if (!value.isFinite()) {
            return ValidationResult.Invalid("Sisa bahan tidak valid.")
        }
        if (value < 0.0) {
            return ValidationResult.Invalid("Sisa bahan tidak boleh negatif.")
        }
        return ValidationResult.Valid
    }

    fun validateCloseSession(
        sessionId: Long?,
        items: List<DailyStockItem>,
        remaining: Map<Long, Double>
    ): ValidationResult {
        if (sessionId == null) {
            return ValidationResult.Invalid("Tidak ada sesi stok harian yang aktif.")
        }
        if (items.isEmpty()) {
            return ValidationResult.Invalid("Tidak ada data stok harian. Hubungi admin.")
        }

        for (item in items) {
            val result = validateRemainingQuantity(remaining[item.ingredientId])
            if (result is ValidationResult.Invalid) return result
        }

        return ValidationResult.Valid
    }
}
