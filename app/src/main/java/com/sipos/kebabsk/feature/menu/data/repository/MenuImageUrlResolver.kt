package com.sipos.kebabsk.feature.menu.data.repository

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

private val unreliableImageHosts = setOf(
    "localhost",
    "127.0.0.1",
    "0.0.0.0",
    "10.0.2.2",
    "your-domain.com"
)

/**
 * Makes image URLs returned by a locally configured Laravel server reachable
 * from the Android device. Absolute CDN/production URLs are left untouched.
 */
internal fun resolveMenuImageUrl(rawImageUrl: String?, apiBaseUrl: String): String? {
    val raw = rawImageUrl?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val apiUrl = apiBaseUrl.trim().toHttpUrlOrNull() ?: return raw
    val parsedImageUrl = raw.toHttpUrlOrNull()

    if (parsedImageUrl == null) {
        val path = if (raw.startsWith('/')) raw else "/$raw"
        return apiUrl.newBuilder()
            .encodedPath(path.substringBefore('?'))
            .query(path.substringAfter('?', missingDelimiterValue = "").ifBlank { null })
            .build()
            .toString()
    }

    val mustUseApiOrigin = parsedImageUrl.host.lowercase() in unreliableImageHosts
    val mustUpgradeSameHost = parsedImageUrl.host.equals(apiUrl.host, ignoreCase = true) &&
        apiUrl.isHttps &&
        !parsedImageUrl.isHttps

    if (!mustUseApiOrigin && !mustUpgradeSameHost) return parsedImageUrl.toString()

    return parsedImageUrl.newBuilder()
        .scheme(apiUrl.scheme)
        .host(apiUrl.host)
        .port(apiUrl.port)
        .build()
        .toString()
}
