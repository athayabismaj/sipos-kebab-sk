package com.sipos.kebabsk.feature.menu.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class MenuListPayload(
    val user: MenuUser,
    val menus: List<MenuItem>,
    val dailySession: DailySessionStatus,
    val dailyStockItems: List<DailyStockItem>,
    val categories: List<MenuCategory> = emptyList(),
    val pagination: MenuPagination = MenuPagination()
)

@Immutable
data class MenuCategory(
    val id: Long,
    val name: String
)

@Immutable
data class MenuPagination(
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val perPage: Int = 20,
    val total: Int = 0,
    val hasMore: Boolean = false
)

@Immutable
data class MenuUser(
    val id: Long,
    val name: String,
    val role: String?,
    val isPrivileged: Boolean
)

@Immutable
data class DailySessionStatus(
    val isOpen: Boolean,
    val label: String?,
    /** False means the API response could not establish the session state safely. */
    val isKnown: Boolean = true
)

@Immutable
data class DailyStockItem(
    val ingredientId: Long,
    val name: String,
    val qty: Double,
    val remainingQty: Double? = null,
    val unit: String?
)

@Immutable
data class MenuItem(
    val id: Long,
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val categoryName: String?,
    val variants: List<MenuVariant>,
    val categoryId: Long? = null
)

@Immutable
data class MenuVariant(
    val id: Long,
    val name: String,
    val price: Long,
    val isAvailable: Boolean,
    val insufficientStock: Boolean = false,
    val imageUrl: String? = null
)
