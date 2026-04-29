package com.sipos.kebabsk.common

private val TECHNICAL_PATTERNS = listOf(
    Regex("""\bapi\b""", RegexOption.IGNORE_CASE),
    Regex("""\bendpoint\b""", RegexOption.IGNORE_CASE),
    Regex("""\bbackend\b""", RegexOption.IGNORE_CASE),
    Regex("""\bserver\b""", RegexOption.IGNORE_CASE),
    Regex("""\bhost\b""", RegexOption.IGNORE_CASE),
    Regex("""\bhttp(s)?\b""", RegexOption.IGNORE_CASE),
    Regex("""\bsocket\b""", RegexOption.IGNORE_CASE),
    Regex("""\btimeout\b""", RegexOption.IGNORE_CASE),
    Regex("""\broute\b""", RegexOption.IGNORE_CASE),
    Regex("""\btoken\b""", RegexOption.IGNORE_CASE),
    Regex("""\bbearer\b""", RegexOption.IGNORE_CASE),
    Regex("""\bstack\s*trace\b""", RegexOption.IGNORE_CASE),
    Regex("""\bexception\b""", RegexOption.IGNORE_CASE),
    Regex("""\bsql\b""", RegexOption.IGNORE_CASE),
    Regex("""\btrace\b""", RegexOption.IGNORE_CASE),
    Regex("""\bundefined\b""", RegexOption.IGNORE_CASE),
    Regex("""\bnullpointer\b""", RegexOption.IGNORE_CASE),
    Regex("""\binternal error\b""", RegexOption.IGNORE_CASE),
    Regex("""\bhttp error\b""", RegexOption.IGNORE_CASE),
    Regex("""unable to|failed to connect|connection refused""", RegexOption.IGNORE_CASE)
)

fun sanitizeUserMessage(rawMessage: String?, fallback: String): String {
    val message = rawMessage?.trim().orEmpty()
    if (message.isBlank()) return fallback

    return if (TECHNICAL_PATTERNS.any { it.containsMatchIn(message) }) fallback else message
}
