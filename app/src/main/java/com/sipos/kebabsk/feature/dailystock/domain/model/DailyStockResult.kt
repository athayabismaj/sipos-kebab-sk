package com.sipos.kebabsk.feature.dailystock.domain.model

import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem

data class DailyStockResult(
    val sessionId: Long?,
    val items: List<DailyStockItem>,
    val businessDate: String? = null,
    val cutoffTime: String? = null,
    val canClose: Boolean = false,
    val overdue: Boolean = false,
    val statusMessage: String? = null,
    val closingPresets: List<ClosingRecipePreset> = emptyList(),
    val closingGroups: List<ClosingRecipeGroup> = emptyList()
)

data class ClosingRecipeGroup(
    val groupId: Long,
    val label: String,
    val anchorIngredientId: Long,
    val anchorName: String,
    val anchorUnit: String,
    val systemRemaining: Double,
    val defaultMenuVariantId: Long,
    val requiresAllocation: Boolean,
    val ready: Boolean,
    val variants: List<ClosingRecipeGroupVariant>
)

data class ClosingRecipeGroupVariant(
    val menuVariantId: Long,
    val label: String,
    val anchorQuantity: Double,
    val isDefault: Boolean
)

data class ClosingRecipePreset(
    val menuVariantId: Long,
    val label: String,
    val anchorIngredientId: Long,
    val anchorName: String,
    val anchorUnit: String,
    val systemRemaining: Double,
    val quantityPerServing: Double,
    val ready: Boolean,
    val missingIngredients: List<String> = emptyList()
)

data class ClosingRecipeAnchorInput(
    val menuVariantId: Long,
    val actualRemaining: Double,
    val allocatedQuantity: Double? = null
)

data class ClosingRecipePreviewItem(
    val ingredientId: Long,
    val name: String,
    val remainingQty: Double,
    val autoUsedQty: Double,
    val unit: String
)

data class ClosingRecipeAffectedIngredient(
    val ingredientId: Long,
    val name: String,
    val usedQty: Double,
    val unit: String
)

data class ClosingRecipeSummary(
    val menuVariantId: Long,
    val label: String,
    val inferredServings: Int,
    val anchorIngredientId: Long? = null,
    val affectedIngredients: List<ClosingRecipeAffectedIngredient> = emptyList()
)

data class ClosingRecipePreview(
    val remainingItems: List<ClosingRecipePreviewItem>,
    val summaries: List<ClosingRecipeSummary>
)
