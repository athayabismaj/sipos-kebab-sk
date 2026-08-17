package com.sipos.kebabsk.feature.menu.data.local

import com.sipos.kebabsk.feature.menu.domain.model.DailySessionStatus
import com.sipos.kebabsk.feature.menu.domain.model.MenuCategory
import com.sipos.kebabsk.feature.menu.domain.model.MenuItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuListPayload
import com.sipos.kebabsk.feature.menu.domain.model.MenuPagination
import com.sipos.kebabsk.feature.menu.domain.model.MenuUser
import com.sipos.kebabsk.feature.menu.domain.model.MenuVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.Locale

class MenuCatalogCacheStore(
    private val dao: MenuCatalogDao
) {
    suspend fun read(
        token: String,
        search: String?,
        categoryId: Long?
    ): MenuListPayload? = withContext(Dispatchers.IO) {
        val scope = tokenScope(token)
        val items = dao.getItems(scope, filterKey(search, categoryId))
        if (items.isEmpty()) return@withContext null

        val menus = items
            .groupByTo(LinkedHashMap()) { it.menuId }
            .map { (_, rows) ->
                val first = rows.first()
                MenuItem(
                    id = first.menuId,
                    name = first.menuName,
                    description = first.menuDescription,
                    isActive = first.menuIsActive,
                    categoryName = first.categoryName,
                    categoryId = first.categoryId,
                    variants = rows.map { row ->
                        MenuVariant(
                            id = row.variantId,
                            name = row.variantName,
                            price = row.price,
                            isAvailable = row.isAvailable,
                            insufficientStock = row.insufficientStock,
                            imageUrl = row.imageUrl
                        )
                    }
                )
            }
        val categories = dao.getCategories(scope).map { cached ->
            MenuCategory(id = cached.categoryId, name = cached.name)
        }

        MenuListPayload(
            user = MenuUser(id = 0L, name = "", role = null, isPrivileged = false),
            menus = menus,
            dailySession = DailySessionStatus(
                isOpen = false,
                label = "Status sesi sedang diperbarui",
                isKnown = false
            ),
            dailyStockItems = emptyList(),
            categories = categories,
            pagination = MenuPagination(
                currentPage = 1,
                lastPage = 1,
                perPage = items.size,
                total = items.size,
                hasMore = false
            )
        )
    }

    suspend fun write(
        token: String,
        search: String?,
        categoryId: Long?,
        page: Int,
        perPage: Int,
        payload: MenuListPayload
    ) = withContext(Dispatchers.IO) {
        val scope = tokenScope(token)
        val key = filterKey(search, categoryId)
        val cachedAt = System.currentTimeMillis()
        var position = (page - 1).coerceAtLeast(0) * perPage
        val entities = payload.menus.flatMap { menu ->
            menu.variants.map { variant ->
                MenuCatalogEntity(
                    scope = scope,
                    filterKey = key,
                    variantId = variant.id,
                    menuId = menu.id,
                    menuName = menu.name,
                    menuDescription = menu.description,
                    menuIsActive = menu.isActive,
                    categoryId = menu.categoryId,
                    categoryName = menu.categoryName,
                    variantName = variant.name,
                    price = variant.price,
                    isAvailable = variant.isAvailable,
                    insufficientStock = variant.insufficientStock,
                    imageUrl = variant.imageUrl,
                    position = position++,
                    cachedAt = cachedAt
                )
            }
        }
        val categories = payload.categories.map { category ->
            MenuCategoryCacheEntity(
                scope = scope,
                categoryId = category.id,
                name = category.name,
                cachedAt = cachedAt
            )
        }

        if (page <= 1) {
            dao.replaceFirstPage(scope, key, entities, categories)
        } else if (entities.isNotEmpty()) {
            dao.insertItems(entities)
        }
    }

    private fun filterKey(search: String?, categoryId: Long?): String {
        val normalizedSearch = search
            ?.trim()
            ?.lowercase(Locale.ROOT)
            .orEmpty()

        return "category=${categoryId ?: 0}|search=$normalizedSearch"
    }

    private fun tokenScope(token: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }
}
