package com.sipos.kebabsk.common

import com.google.gson.JsonParseException

object NetworkErrorMapper {
    fun mapHttpCodeToUserMessage(
        code: Int,
        fallback: String,
        message404: String? = null,
        message422: String? = null
    ): String {
        return when (code) {
            400 -> "Permintaan tidak valid. Silakan periksa kembali."
            401 -> "Sesi login sudah berakhir. Silakan login ulang."
            403 -> "Akses tidak diizinkan."
            404 -> message404 ?: fallback
            422 -> message422 ?: "Data permintaan belum valid. Silakan periksa kembali."
            429 -> "Permintaan terlalu sering. Coba lagi beberapa saat."
            in 500..599 -> "Layanan sedang bermasalah. Silakan coba lagi nanti."
            else -> fallback
        }
    }

    fun mapThrowableToUserMessage(throwable: Throwable, fallback: String): String {
        val message = throwable.message.orEmpty()
        return when {
            throwable is JsonParseException ->
                "Respons server tidak dapat dibaca. Silakan coba lagi."
            message.contains("timeout", ignoreCase = true) ->
                "Permintaan sedang padat. Silakan coba beberapa saat lagi."
            message.contains("unable to resolve host", ignoreCase = true) ||
                message.contains("failed to connect", ignoreCase = true) ||
                message.contains("connection", ignoreCase = true) ->
                "Koneksi internet bermasalah. Periksa jaringan lalu coba lagi."
            throwable is IllegalStateException -> sanitizeUserMessage(throwable.message, fallback)
            else -> fallback
        }
    }
}
