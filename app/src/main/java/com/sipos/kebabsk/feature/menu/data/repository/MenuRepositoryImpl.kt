package com.sipos.kebabsk.feature.menu.data.repository

import com.sipos.kebabsk.common.retryNetworkRequest
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.common.suspendRunCatching
import com.sipos.kebabsk.common.NetworkErrorMapper
import com.sipos.kebabsk.feature.menu.data.remote.MenuApiService
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuItem
import com.sipos.kebabsk.feature.menu.domain.model.DailySessionStatus
import com.sipos.kebabsk.feature.menu.domain.model.MenuListPayload
import com.sipos.kebabsk.feature.menu.domain.model.MenuUser
import com.sipos.kebabsk.feature.menu.domain.model.MenuVariant
import com.sipos.kebabsk.feature.menu.domain.repository.MenuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MenuRepositoryImpl(
    private val menuApiService: MenuApiService
) : MenuRepository {
    override suspend fun getMenus(token: String, search: String?, categoryId: Long?): Result<MenuListPayload> {
        return suspendRunCatching {
            val response = retryNetworkRequest {
                menuApiService.getMenus(
                    authorization = "Bearer $token",
                    search = search,
                    categoryId = categoryId
                )
            }

            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.data == null) {
                throw IllegalStateException(
                    mapMenuError(response.code(), body?.message)
                )
            }

            withContext(Dispatchers.Default) {
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
                                price = variant.price ?: 0L,
                                isAvailable = variant.isAvailable ?: false,
                                insufficientStock = variant.insufficientStock ?: false
                            )
                        }
                    )
                }

                val isDailySessionOpen = body.data.dailySession?.isOpen
                    ?: body.data.isDailySessionOpen
                    ?: false

                val dailySession = DailySessionStatus(
                    isOpen = isDailySessionOpen,
                    label = body.data.dailySession?.statusLabel
                        ?: body.data.dailySessionStatusLabel
                        ?: if (isDailySessionOpen) "Sesi Harian Aktif" else "Sesi Harian Belum Dibuka",
                    targetRevenue = body.data.dailySession?.targetRevenue
                )

                val dailyStockItems = body.data.dailyStockItems.orEmpty().mapNotNull { stock ->
                    val name = stock.name?.trim().orEmpty()
                    if (name.isBlank()) return@mapNotNull null
                    DailyStockItem(
                        ingredientId = stock.ingredientId ?: 0L,
                        name = name,
                        qty = stock.qty ?: 0.0,
                        unit = stock.unit?.trim()?.takeIf { it.isNotBlank() }
                    )
                }

                MenuListPayload(
                    user = user,
                    menus = menus,
                    dailySession = dailySession,
                    dailyStockItems = dailyStockItems
                )
            }
        }.recoverCatching { throwable ->
            throw IllegalStateException(
                mapThrowableError(throwable)
            )
        }
    }

    private fun mapMenuError(code: Int, rawMessage: String?): String {
        return when (code) {
            404 -> "Data menu belum tersedia."
            else -> {
                val fallback = "Menu belum bisa dimuat. Silakan coba lagi."
                val httpMapped = NetworkErrorMapper.mapHttpCodeToUserMessage(code, fallback)
                if (httpMapped == fallback) {
                    sanitizeUserMessage(rawMessage, fallback)
                } else {
                    httpMapped
                }
            }
        }
    }

    private fun mapThrowableError(throwable: Throwable): String {
        return NetworkErrorMapper.mapThrowableToUserMessage(throwable, "Menu belum bisa dimuat. Silakan coba lagi.")
    }
}
