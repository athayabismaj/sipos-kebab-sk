package com.sipos.kebabsk.feature.menu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface MenuCatalogDao {
    @Query(
        """
        SELECT * FROM menu_catalog_items
        WHERE scope = :scope AND filter_key = :filterKey
        ORDER BY position ASC
        """
    )
    suspend fun getItems(scope: String, filterKey: String): List<MenuCatalogEntity>

    @Query(
        """
        SELECT * FROM menu_catalog_categories
        WHERE scope = :scope
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    suspend fun getCategories(scope: String): List<MenuCategoryCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<MenuCatalogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<MenuCategoryCacheEntity>)

    @Query("DELETE FROM menu_catalog_items WHERE scope = :scope AND filter_key = :filterKey")
    suspend fun deleteFilter(scope: String, filterKey: String)

    @Query("DELETE FROM menu_catalog_categories WHERE scope = :scope")
    suspend fun deleteCategories(scope: String)

    @Transaction
    suspend fun replaceFirstPage(
        scope: String,
        filterKey: String,
        items: List<MenuCatalogEntity>,
        categories: List<MenuCategoryCacheEntity>
    ) {
        deleteFilter(scope, filterKey)
        if (items.isNotEmpty()) insertItems(items)
        deleteCategories(scope)
        if (categories.isNotEmpty()) insertCategories(categories)
    }
}
