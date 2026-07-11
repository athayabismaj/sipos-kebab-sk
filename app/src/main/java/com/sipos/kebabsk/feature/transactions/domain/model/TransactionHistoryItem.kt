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

data class TransactionReceipt(
    val id: Long,
    val code: String,
    val createdAtLabel: String,
    val paymentMethod: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val changeAmount: Double,
    val status: String,
    val items: List<TransactionReceiptItem>,
    val cashierName: String = "Kebab SK POS",
    val displayCode: String? = null,
    val isDetailed: Boolean = true
)

data class TransactionReceiptItem(
    val name: String,
    val variantName: String?,
    val qty: Int,
    val price: Double,
    val subtotal: Double
)

data class RevenueSummaryResult(
    val totalRevenue: Double,
    val totalCount: Int,
    val transactionGrowthPercentage: Double?,
    val dominantItemName: String?,
    val revenueTargetPercentage: Double?,
    val dailyTargetRevenue: Double?
)
