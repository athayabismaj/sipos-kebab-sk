package com.sipos.kebabsk.feature.shift.data.remote

import com.google.gson.annotations.SerializedName

/**
 * Request body untuk POST sessions/{id}/close.
 */
data class CloseSessionRequest(
    @SerializedName("actual_physical_cash") val actualPhysicalCash: Double,
    @SerializedName("closing_notes") val closingNotes: String?
)

/**
 * Response wrapper dari server. Struktur:
 * { "data": { "system_cash": ..., "actual_cash": ..., "variance": ... } }
 */
data class CloseSessionResponse(
    @SerializedName("data") val data: CloseSessionData
)

data class CloseSessionData(
    @SerializedName("system_cash") val systemCash: Double,
    @SerializedName("actual_cash") val actualCash: Double,
    @SerializedName("variance") val variance: Double
)
