package com.sipos.kebabsk.feature.menu.data.remote

import com.google.gson.annotations.SerializedName

data class MenusResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: MenusDataResponse?
)

data class MenusDataResponse(
    @SerializedName("user") val user: UserResponse?,
    @SerializedName("menus") val menus: List<MenuResponse>?,
    @SerializedName(value = "daily_session", alternate = ["dailySession", "sesi_harian", "cashier_session"])
    val dailySession: DailySessionResponse?,
    @SerializedName(value = "is_daily_session_open", alternate = ["daily_session_open", "session_open"])
    val isDailySessionOpen: Boolean?,
    @SerializedName(value = "daily_session_status_label", alternate = ["session_status_label"])
    val dailySessionStatusLabel: String?,
    @SerializedName(
        value = "daily_stock_items",
        alternate = ["dailyStocks", "daily_stock", "stok_harian_bawa", "carried_stock_items"]
    )
    val dailyStockItems: List<DailyStockItemResponse>?
)

data class DailySessionResponse(
    @SerializedName(value = "is_open", alternate = ["open", "isOpen"])
    val isOpen: Boolean?,
    @SerializedName(value = "status_label", alternate = ["label", "status"])
    val statusLabel: String?,
    @SerializedName(value = "target_revenue", alternate = ["targetRevenue"])
    val targetRevenue: Long?
)

data class UserResponse(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("is_privileged") val isPrivileged: Boolean?
)

data class MenuResponse(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("is_active") val isActive: Boolean?,
    @SerializedName("category") val category: CategoryResponse?,
    @SerializedName("variants") val variants: List<VariantResponse>?
)

data class CategoryResponse(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String?
)

data class VariantResponse(
    @SerializedName("id") val id: Long?,
    @SerializedName("name") val name: String?,
    @SerializedName("price") val price: Long?,
    @SerializedName("is_available") val isAvailable: Boolean?,
    @SerializedName(value = "insufficient_stock", alternate = ["stock_insufficient", "is_stock_insufficient", "out_of_stock"])
    val insufficientStock: Boolean?
)

data class DailyStockItemResponse(
    @SerializedName(value = "ingredient_id", alternate = ["id"])
    val ingredientId: Long?,
    @SerializedName(value = "name", alternate = ["item_name", "material_name", "bahan_name"])
    val name: String?,
    @SerializedName(value = "qty", alternate = ["quantity", "amount"])
    val qty: Double?,
    @SerializedName(value = "unit", alternate = ["uom", "satuan"])
    val unit: String?
)
