package com.sipos.kebabsk.feature.menu.data.remote

import com.google.gson.annotations.SerializedName

data class MenusResponse(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: MenusDataResponse?
)

data class MenusDataResponse(
    @SerializedName("user") val user: UserResponse?,
    @SerializedName("menus") val menus: List<MenuResponse>?
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
    @SerializedName("price") val price: Double?,
    @SerializedName("is_available") val isAvailable: Boolean?
)
