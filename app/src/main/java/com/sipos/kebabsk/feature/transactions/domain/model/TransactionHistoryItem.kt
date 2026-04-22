package com.sipos.kebabsk.feature.transactions.domain.model

data class TransactionHistoryItem(
    val id: Long,
    val code: String,
    val time: String,
    val itemCount: Int,
    val total: Double,
    val status: String,
    val originalDate: String
)

data class TransactionPageData(
    val items: List<TransactionHistoryItem>,
    val totalPages: Int
)

data class RevenueSummaryResult(
    val totalRevenue: Double,
    val totalCount: Int,
    val transactionGrowthPercentage: Double?,
    val dominantItemName: String?,
    val revenueTargetPercentage: Double?
)
