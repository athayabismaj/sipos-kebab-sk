package com.sipos.kebabsk.feature.menu.data.repository

import com.sipos.kebabsk.feature.menu.data.remote.MenuApiService
import com.sipos.kebabsk.feature.menu.domain.model.MenuItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuListPayload
import com.sipos.kebabsk.feature.menu.domain.model.MenuUser
import com.sipos.kebabsk.feature.menu.domain.model.MenuVariant
import com.sipos.kebabsk.feature.menu.domain.repository.MenuRepository

class MenuRepositoryImpl(
    private val menuApiService: MenuApiService
) : MenuRepository {
    override suspend fun getMenus(token: String, search: String?, categoryId: Long?): Result<MenuListPayload> {
        return runCatching {
            val response = menuApiService.getMenus(
                authorization = "Bearer $token",
                search = search,
                categoryId = categoryId
            )

            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.data == null) {
                throw IllegalStateException(
                    body?.message ?: "Gagal mengambil data menu"
                )
            }

            val userResponse = body.data.user
            val user = MenuUser(
                id = userResponse?.id ?: 0L,
                name = userResponse?.name ?: "Kasir",
                role = userResponse?.role,
                isPrivileged = userResponse?.isPrivileged ?: false
            )

            val menus = body.data.menus.orEmpty().map { item ->
                MenuItem(
                    id = item.id ?: 0L,
                    name = item.name ?: "Tanpa nama",
                    description = item.description,
                    isActive = item.isActive ?: false,
                    categoryName = item.category?.name,
                    variants = item.variants.orEmpty().map { variant ->
                        MenuVariant(
                            id = variant.id ?: 0L,
                            name = variant.name ?: "Varian",
                            price = variant.price ?: 0.0,
                            isAvailable = variant.isAvailable ?: false
                        )
                    }
                )
            }

            MenuListPayload(user = user, menus = menus)
        }
    }
}
