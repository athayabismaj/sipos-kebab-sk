package com.sipos.kebabsk.feature.menu.data.remote

import com.google.gson.annotations.SerializedName

data class MenusResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: MenusDataResponse?
)

data class MenusDataResponse(
    @SerializedName("user") val user: UserResponse?,
    @SerializedName("categories") val categories: List<CategoryResponse>?,
    @SerializedName("menus") val menus: List<MenuResponse>?,
    @SerializedName("pagination") val pagination: MenuPaginationResponse?,
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

data class MenuPaginationResponse(
    @SerializedName("current_page") val currentPage: Int?,
    @SerializedName("last_page") val lastPage: Int?,
    @SerializedName("per_page") val perPage: Int?,
    @SerializedName("total") val total: Int?,
    @SerializedName("has_more") val hasMore: Boolean?
)

data class DailySessionResponse(
    @SerializedName("id") val id: Long? = null,
    @SerializedName(value = "is_open", alternate = ["open", "isOpen"])
    val isOpen: Boolean?,
    @SerializedName(value = "status_label", alternate = ["label", "status"])
    val statusLabel: String?
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
    @SerializedName("image_url") val imageUrl: String?,
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
