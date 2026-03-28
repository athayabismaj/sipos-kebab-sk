package com.sipos.kebabsk.feature.menu.domain.usecase

import com.sipos.kebabsk.feature.menu.domain.model.MenuListPayload
import com.sipos.kebabsk.feature.menu.domain.repository.MenuRepository

class GetMenusUseCase(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(token: String, search: String? = null): Result<MenuListPayload> {
        return menuRepository.getMenus(token = token, search = search)
    }
}
