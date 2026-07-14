package com.sipos.kebabsk.feature.dailystock.domain.model

import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem

data class DailyStockResult(
    val sessionId: Long?,
    val items: List<DailyStockItem>
)
