package com.sipos.kebabsk.feature.transactions.domain.model

data class TransactionHistoryItem(
    val id: Long,
    val code: String,
    val time: String,
    val itemCount: Int,
    val total: Long,
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
    val totalAmount: Long,
    val paidAmount: Long,
    val changeAmount: Long,
    val status: String,
    val items: List<TransactionReceiptItem>,
    val cashierName: String = "Kebab SK POS",
    val branchAddress: String? = null,
    val displayCode: String? = null,
    val isDetailed: Boolean = true
)

data class TransactionReceiptItem(
    val name: String,
    val variantName: String?,
    val qty: Int,
    val price: Long,
    val subtotal: Long
)

data class RevenueSummaryResult(
    val totalRevenue: Long,
    val totalCount: Int,
    val transactionGrowthPercentage: Double?,
    val dominantItemName: String?,
    val revenueTargetPercentage: Double?,
    val dailyTargetRevenue: Long?
)
