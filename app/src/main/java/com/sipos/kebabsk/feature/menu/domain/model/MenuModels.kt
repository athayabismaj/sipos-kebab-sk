package com.sipos.kebabsk.feature.menu.domain.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class MenuListPayload(
    val user: MenuUser,
    val menus: List<MenuItem>,
    val dailySession: DailySessionStatus,
    val dailyStockItems: List<DailyStockItem>
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
    val targetRevenue: Double?
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
    val variants: List<MenuVariant>
)

@Immutable
data class MenuVariant(
    val id: Long,
    val name: String,
    val price: Double,
    val isAvailable: Boolean,
    val insufficientStock: Boolean = false
)
