package com.sipos.kebabsk.feature.dailystock.domain.model

data class CashReconciliation(
    val sessionId: Long,
    val businessDate: String?,
    val openingCash: Long,
    val cashSales: Long,
    val cashExpenses: Long,
    val expectedCash: Long
)
