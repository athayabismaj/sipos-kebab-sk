package com.sipos.kebabsk.feature.menu.domain.usecase

import com.sipos.kebabsk.feature.menu.domain.model.MenuListPayload
import com.sipos.kebabsk.feature.menu.domain.repository.MenuRepository

class GetMenusUseCase(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(
        token: String,
        search: String? = null,
        categoryId: Long? = null,
        page: Int = 1,
        perPage: Int = 20
    ): Result<MenuListPayload> {
        return menuRepository.getMenus(
            token = token,
            search = search,
            categoryId = categoryId,
            page = page,
            perPage = perPage
        )
    }
}
