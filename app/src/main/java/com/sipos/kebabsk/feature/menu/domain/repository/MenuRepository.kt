package com.sipos.kebabsk.feature.menu.domain.repository

import com.sipos.kebabsk.feature.menu.domain.model.MenuListPayload

interface MenuRepository {
    suspend fun getMenus(token: String, search: String? = null, categoryId: Long? = null): Result<MenuListPayload>
}
