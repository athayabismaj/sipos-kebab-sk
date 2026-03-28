package com.sipos.kebabsk.common

private val TECHNICAL_KEYWORDS = listOf(
    "api",
    "endpoint",
    "backend",
    "server",
    "host",
    "http",
    "https",
    "socket",
    "timeout",
    "route",
    "token",
    "bearer",
    "stacktrace",
    "exception",
    "sql",
    "trace",
    "undefined",
    "nullpointer",
    "internal error"
)

fun sanitizeUserMessage(rawMessage: String?, fallback: String): String {
    val message = rawMessage?.trim().orEmpty()
    if (message.isBlank()) return fallback

    val lower = message.lowercase()
    return if (TECHNICAL_KEYWORDS.any { lower.contains(it) }) fallback else message
}
