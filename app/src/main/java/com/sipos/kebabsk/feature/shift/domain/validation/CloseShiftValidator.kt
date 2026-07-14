package com.sipos.kebabsk.feature.shift.domain.validation

import com.sipos.kebabsk.common.MoneyUtils
import com.sipos.kebabsk.common.validation.ValidationResult

class CloseShiftValidator {
    fun validate(
        sessionId: Long?,
        isReadyToClose: Boolean,
        actualPhysicalCash: Long
    ): ValidationResult {
        if (sessionId == null) {
            return ValidationResult.Invalid("Tidak ada sesi stok harian yang aktif.")
        }
        if (!isReadyToClose) {
            return ValidationResult.Invalid("Harap isi stok sisa bahan baku terlebih dahulu!")
        }
        if (actualPhysicalCash < 0L) {
            return ValidationResult.Invalid("Nominal kas fisik tidak valid.")
        }
        return ValidationResult.Valid
    }

    fun parseActualPhysicalCash(input: String): Long? {
        return MoneyUtils.parseRupiahInput(input)
    }
}
