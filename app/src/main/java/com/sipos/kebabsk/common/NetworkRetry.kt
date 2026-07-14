package com.sipos.kebabsk.common

import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.math.min

suspend fun <T> retryNetworkRequest(
    maxAttempts: Int = 3,
    initialDelayMs: Long = 350,
    maxDelayMs: Long = 1200,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelayMs
    var lastError: Throwable? = null

    repeat(maxAttempts) { attempt ->
        try {
            return block()
        } catch (e: Throwable) {
            if (e is CancellationException) throw e

            val retryable = e is IOException ||
                e is SocketTimeoutException ||
                e is ConnectException ||
                e is UnknownHostException

            if (!retryable) throw e
            lastError = e
            if (attempt == maxAttempts - 1) return@repeat
            delay(currentDelay)
            currentDelay = min((currentDelay * factor).toLong(), maxDelayMs)
        }
    }

    throw lastError ?: IllegalStateException("Permintaan jaringan gagal.")
}

