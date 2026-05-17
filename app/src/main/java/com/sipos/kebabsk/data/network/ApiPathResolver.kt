package com.sipos.kebabsk.data.network

import java.util.Locale

object ApiPathResolver {
    fun resolve(baseUrl: String, endpoint: String): String {
        val normalizedEndpoint = endpoint.trim().trimStart('/')
        if (normalizedEndpoint.isBlank()) return normalizedEndpoint

        val normalizedBaseUrl = baseUrl.trim().lowercase(Locale.ROOT)
        return if (normalizedBaseUrl.endsWith("/api/") || normalizedBaseUrl.endsWith("/api")) {
            normalizedEndpoint.removePrefix("api/").removePrefix("api")
        } else {
            normalizedEndpoint
        }
    }
}
