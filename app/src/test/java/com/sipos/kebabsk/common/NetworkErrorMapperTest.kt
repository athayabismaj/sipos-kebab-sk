package com.sipos.kebabsk.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.UnknownHostException
import java.net.ConnectException
import java.net.SocketTimeoutException

class NetworkErrorMapperTest {

    @Test
    fun mapHttpCodeToUserMessage_maps401() {
        assertEquals("Sesi login sudah berakhir. Silakan login ulang.", NetworkErrorMapper.mapHttpCodeToUserMessage(401, "Fallback"))
    }

    @Test
    fun mapHttpCodeToUserMessage_maps403() {
        assertEquals("Akses tidak diizinkan.", NetworkErrorMapper.mapHttpCodeToUserMessage(403, "Fallback"))
    }

    @Test
    fun mapHttpCodeToUserMessage_maps404() {
        assertEquals("Not Found Custom", NetworkErrorMapper.mapHttpCodeToUserMessage(404, "Fallback", message404 = "Not Found Custom"))
        assertEquals("Fallback", NetworkErrorMapper.mapHttpCodeToUserMessage(404, "Fallback"))
    }

    @Test
    fun mapHttpCodeToUserMessage_maps422() {
        assertEquals("Data tidak lengkap", NetworkErrorMapper.mapHttpCodeToUserMessage(422, "Fallback", message422 = "Data tidak lengkap"))
        assertEquals("Data permintaan belum valid. Silakan periksa kembali.", NetworkErrorMapper.mapHttpCodeToUserMessage(422, "Fallback"))
    }

    @Test
    fun mapHttpCodeToUserMessage_maps429() {
        assertEquals("Permintaan terlalu sering. Coba lagi beberapa saat.", NetworkErrorMapper.mapHttpCodeToUserMessage(429, "Fallback"))
    }

    @Test
    fun mapHttpCodeToUserMessage_keepsBusinessFallbackFor409() {
        assertEquals("Konflik stok harian", NetworkErrorMapper.mapHttpCodeToUserMessage(409, "Konflik stok harian"))
    }

    @Test
    fun mapHttpCodeToUserMessage_maps500() {
        assertEquals("Layanan sedang bermasalah. Silakan coba lagi nanti.", NetworkErrorMapper.mapHttpCodeToUserMessage(500, "Fallback"))
        assertEquals("Layanan sedang bermasalah. Silakan coba lagi nanti.", NetworkErrorMapper.mapHttpCodeToUserMessage(503, "Fallback"))
        assertEquals("Layanan sedang bermasalah. Silakan coba lagi nanti.", NetworkErrorMapper.mapHttpCodeToUserMessage(599, "Fallback"))
    }

    @Test
    fun mapHttpCodeToUserMessage_mapsUnknownCode() {
        assertEquals("Fallback", NetworkErrorMapper.mapHttpCodeToUserMessage(999, "Fallback"))
    }

    @Test
    fun mapThrowableToUserMessage_mapsTimeout() {
        assertEquals("Permintaan sedang padat. Silakan coba beberapa saat lagi.", NetworkErrorMapper.mapThrowableToUserMessage(SocketTimeoutException("timeout"), "Fallback"))
    }

    @Test
    fun mapThrowableToUserMessage_mapsUnableToResolveHost() {
        assertEquals("Koneksi internet bermasalah. Periksa jaringan lalu coba lagi.", NetworkErrorMapper.mapThrowableToUserMessage(UnknownHostException("Unable to resolve host"), "Fallback"))
    }

    @Test
    fun mapThrowableToUserMessage_mapsFailedToConnect() {
        assertEquals("Koneksi internet bermasalah. Periksa jaringan lalu coba lagi.", NetworkErrorMapper.mapThrowableToUserMessage(ConnectException("Failed to connect"), "Fallback"))
    }

    @Test
    fun mapThrowableToUserMessage_sanitizesHtml() {
        val ex = IllegalStateException("<html><body>Error</body></html>")
        assertEquals("Fallback", NetworkErrorMapper.mapThrowableToUserMessage(ex, "Fallback"))
    }

    @Test
    fun mapThrowableToUserMessage_sanitizesToken() {
        val ex = IllegalStateException("Invalid token abcdef")
        assertEquals("Fallback", NetworkErrorMapper.mapThrowableToUserMessage(ex, "Fallback"))
    }
    
    @Test
    fun mapThrowableToUserMessage_allowsSafeValidationMessage() {
        val ex = IllegalStateException("Nama produk tidak boleh kosong")
        assertEquals("Nama produk tidak boleh kosong", NetworkErrorMapper.mapThrowableToUserMessage(ex, "Fallback"))
    }
}
