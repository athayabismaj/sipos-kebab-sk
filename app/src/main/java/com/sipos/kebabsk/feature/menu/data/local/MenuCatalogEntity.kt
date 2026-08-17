package com.sipos.kebabsk.feature.menu.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "menu_catalog_items",
    primaryKeys = ["scope", "filter_key", "variant_id"],
    indices = [
        Index(value = ["scope", "filter_key", "position"]),
        Index(value = ["variant_id"])
    ]
)
data class MenuCatalogEntity(
    val scope: String,
    @ColumnInfo(name = "filter_key") val filterKey: String,
    @ColumnInfo(name = "variant_id") val variantId: Long,
    @ColumnInfo(name = "menu_id") val menuId: Long,
    @ColumnInfo(name = "menu_name") val menuName: String,
    @ColumnInfo(name = "menu_description") val menuDescription: String?,
    @ColumnInfo(name = "menu_is_active") val menuIsActive: Boolean,
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "variant_name") val variantName: String,
    val price: Long,
    @ColumnInfo(name = "is_available") val isAvailable: Boolean,
    @ColumnInfo(name = "insufficient_stock") val insufficientStock: Boolean,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    val position: Int,
    @ColumnInfo(name = "cached_at") val cachedAt: Long
)

@Entity(
    tableName = "menu_catalog_categories",
    primaryKeys = ["scope", "category_id"]
)
data class MenuCategoryCacheEntity(
    val scope: String,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    val name: String,
    @ColumnInfo(name = "cached_at") val cachedAt: Long
)
