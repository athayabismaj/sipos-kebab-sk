package com.sipos.kebabsk.feature.menu.domain.model

data class MenuListPayload(
    val user: MenuUser,
    val menus: List<MenuItem>
)

data class MenuUser(
    val id: Long,
    val name: String,
    val role: String?,
    val isPrivileged: Boolean
)

data class MenuItem(
    val id: Long,
    val name: String,
    val description: String?,
    val isActive: Boolean,
    val categoryName: String?,
    val variants: List<MenuVariant>
)

data class MenuVariant(
    val id: Long,
    val name: String,
    val price: Double,
    val isAvailable: Boolean
)
