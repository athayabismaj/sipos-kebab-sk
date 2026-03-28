package com.sipos.kebabsk.feature.transactions.data.remote

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class TransactionsResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: JsonElement?
)

data class TransactionItemResponse(
    @SerializedName("id") val id: Long?,
    @SerializedName("transaction_code") val transactionCode: String?,
    @SerializedName("total_amount") val totalAmount: Double?,
    @SerializedName("status") val status: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("items_count") val itemsCount: Int?
)

data class RevenueSummaryResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: RevenueSummaryData?
)

data class RevenueSummaryData(
    @SerializedName("total_revenue") val totalRevenue: Double?,
    @SerializedName("total_count") val totalCount: Int?
)

data class RevenueTrendResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<RevenueTrendData>?
)

data class RevenueTrendData(
    @SerializedName("date") val date: String?,
    @SerializedName("total_revenue") val totalRevenue: Double?
)
