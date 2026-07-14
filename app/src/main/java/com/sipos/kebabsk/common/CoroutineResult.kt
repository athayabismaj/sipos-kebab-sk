package com.sipos.kebabsk.common

import kotlinx.coroutines.CancellationException

suspend inline fun <T> suspendRunCatching(
    crossinline block: suspend () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        Result.failure(error)
    }
}
