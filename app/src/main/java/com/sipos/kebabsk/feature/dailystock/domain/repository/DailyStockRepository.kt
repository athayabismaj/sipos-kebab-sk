package com.sipos.kebabsk.feature.dailystock.domain.repository

import com.sipos.kebabsk.feature.dailystock.domain.model.DailyStockResult

interface DailyStockRepository {
    suspend fun getDailyStock(token: String): Result<DailyStockResult>
    
    suspend fun closeSession(
        token: String,
        remaining: Map<Long, Double>,
        notes: String?
    ): Result<String>
}
