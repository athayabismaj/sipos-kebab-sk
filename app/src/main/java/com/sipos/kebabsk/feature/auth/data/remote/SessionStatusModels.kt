package com.sipos.kebabsk.feature.auth.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Response dari GET sessions/current-status.
 *
 * Server mengembalikan:
 * - 200: { "active": true, "data": { "session_id": 42, ... } }
 * - 404: { "active": false, "message": "..." }
 *
 * Pada kasus 404, Retrofit akan mengembalikan response.isSuccessful = false,
 * sehingga kita parse error body secara manual.
 */
data class SessionStatusResponse(
    @SerializedName("active") val active: Boolean,
    @SerializedName("data") val data: SessionStatusData? = null,
    @SerializedName("message") val message: String? = null
)

data class SessionStatusData(
    @SerializedName("session_id") val sessionId: Long
)
