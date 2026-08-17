package com.sipos.kebabsk.feature.menu.domain.repository

import com.sipos.kebabsk.feature.menu.domain.model.MenuListPayload

interface MenuRepository {
    suspend fun getCachedMenus(
        token: String,
        search: String? = null,
        categoryId: Long? = null
    ): MenuListPayload? = null

    suspend fun getMenus(
        token: String,
        search: String? = null,
        categoryId: Long? = null,
        page: Int = 1,
        perPage: Int = 20
    ): Result<MenuListPayload>
}
