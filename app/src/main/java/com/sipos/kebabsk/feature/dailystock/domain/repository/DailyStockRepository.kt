package com.sipos.kebabsk.feature.dailystock.domain.repository

import com.sipos.kebabsk.feature.dailystock.domain.model.DailyStockResult
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipeAnchorInput
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipePreview

interface DailyStockRepository {
    suspend fun getDailyStock(token: String): Result<DailyStockResult>
    
    suspend fun closeSession(
        token: String,
        remaining: Map<Long, Double>,
        notes: String?
    ): Result<String>

    suspend fun previewClosing(
        token: String,
        anchors: List<ClosingRecipeAnchorInput>
    ): Result<ClosingRecipePreview> = Result.failure(UnsupportedOperationException("Preview closing belum tersedia."))

    suspend fun closeSessionWithRecipe(
        token: String,
        remainingOverrides: Map<Long, Double>,
        anchors: List<ClosingRecipeAnchorInput>,
        notes: String?,
        idempotencyKey: String
    ): Result<String> = closeSession(token, remainingOverrides, notes)
}
