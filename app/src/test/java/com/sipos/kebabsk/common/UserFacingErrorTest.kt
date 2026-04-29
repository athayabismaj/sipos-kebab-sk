package com.sipos.kebabsk.common

import org.junit.Assert.assertEquals
import org.junit.Test

class UserFacingErrorTest {

    @Test
    fun sanitizeUserMessage_doesNotTreatAplikasiAsApiKeyword() {
        val msg = "Aplikasi belum bisa memuat data hari ini."
        val result = sanitizeUserMessage(msg, "Fallback")
        assertEquals(msg, result)
    }

    @Test
    fun sanitizeUserMessage_replacesTechnicalMessageWithFallback() {
        val result = sanitizeUserMessage(
            "Endpoint /api/transactions tidak ditemukan",
            "Terjadi kesalahan. Silakan coba lagi."
        )
        assertEquals("Terjadi kesalahan. Silakan coba lagi.", result)
    }

    @Test
    fun mapHttpCodeToUserMessage_maps401ToReloginMessage() {
        val result = mapHttpCodeToUserMessage(401, "Fallback")
        assertEquals("Sesi login sudah berakhir. Silakan login ulang.", result)
    }

    @Test
    fun mapThrowableToUserMessage_mapsTimeoutToFriendlyMessage() {
        val result = mapThrowableToUserMessage(
            RuntimeException("timeout while connecting"),
            "Fallback"
        )
        assertEquals("Permintaan sedang padat. Silakan coba beberapa saat lagi.", result)
    }
}
