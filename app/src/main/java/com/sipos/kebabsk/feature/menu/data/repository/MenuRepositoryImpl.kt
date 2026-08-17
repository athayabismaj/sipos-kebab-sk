package com.sipos.kebabsk.feature.menu.data.repository

import com.sipos.kebabsk.BuildConfig
import com.sipos.kebabsk.common.retryNetworkRequest
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.common.suspendRunCatching
import com.sipos.kebabsk.common.NetworkErrorMapper
import com.sipos.kebabsk.feature.menu.data.local.MenuCatalogCacheStore
import com.sipos.kebabsk.feature.menu.data.remote.MenuApiService
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuCategory
import com.sipos.kebabsk.feature.menu.domain.model.MenuPagination
import com.sipos.kebabsk.feature.menu.domain.model.DailySessionStatus
import com.sipos.kebabsk.feature.menu.domain.model.MenuListPayload
import com.sipos.kebabsk.feature.menu.domain.model.MenuUser
import com.sipos.kebabsk.feature.menu.domain.model.MenuVariant
import com.sipos.kebabsk.feature.menu.domain.repository.MenuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MenuRepositoryImpl(
    private val menuApiService: MenuApiService,
    private val menuCacheStore: MenuCatalogCacheStore? = null
) : MenuRepository {
    override suspend fun getCachedMenus(
        token: String,
        search: String?,
        categoryId: Long?
    ): MenuListPayload? {
        return runCatching {
            menuCacheStore?.read(token, search, categoryId)
                ?.withResolvedImageUrls()
        }.getOrNull()
    }

    override suspend fun getMenus(
        token: String,
        search: String?,
        categoryId: Long?,
        page: Int,
        perPage: Int
    ): Result<MenuListPayload> {
        return suspendRunCatching {
            val response = retryNetworkRequest {
                menuApiService.getMenus(
                    authorization = "Bearer $token",
                    search = search,
                    categoryId = categoryId,
                    page = page,
                    perPage = perPage
                )
            }

            val body = response.body()
            if (!response.isSuccessful || body?.success != true || body.data == null) {
                throw IllegalStateException(
                    mapMenuError(response.code(), body?.message)
                )
            }

            val payload = withContext(Dispatchers.Default) {
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
                        categoryId = item.category?.id,
                        variants = item.variants.orEmpty().map { variant ->
                            MenuVariant(
                                id = variant.id ?: 0L,
                                name = variant.name ?: "Varian",
                                imageUrl = resolveMenuImageUrl(
                                    rawImageUrl = variant.imageUrl,
                                    apiBaseUrl = BuildConfig.API_BASE_URL
                                ),
                                price = variant.price ?: 0L,
                                isAvailable = variant.isAvailable ?: false,
                                insufficientStock = variant.insufficientStock ?: false
                            )
                        }
                    )
                }
                val categories = body.data.categories.orEmpty()
                    .mapNotNull { category ->
                        val id = category.id ?: return@mapNotNull null
                        val name = category.name?.trim().orEmpty()
                        if (name.isBlank()) return@mapNotNull null
                        MenuCategory(id = id, name = name)
                    }
                    .distinctBy { it.id }
                val paginationResponse = body.data.pagination
                val pagination = MenuPagination(
                    currentPage = paginationResponse?.currentPage ?: page,
                    lastPage = paginationResponse?.lastPage ?: page,
                    perPage = paginationResponse?.perPage ?: perPage,
                    total = paginationResponse?.total ?: menus.sumOf { it.variants.size },
                    hasMore = paginationResponse?.hasMore ?: false
                )

                // Missing session fields must not be interpreted as a closed session.
                // The UI blocks checkout until the server can confirm the actual state.
                val reportedSessionState = body.data.dailySession?.isOpen
                    ?: body.data.isDailySessionOpen
                val isDailySessionKnown = reportedSessionState != null
                val isDailySessionOpen = reportedSessionState == true

                val dailySession = DailySessionStatus(
                    isOpen = isDailySessionOpen,
                    label = body.data.dailySession?.statusLabel
                        ?: body.data.dailySessionStatusLabel
                        ?: when (reportedSessionState) {
                            true -> "Sesi Harian Aktif"
                            false -> "Sesi Harian Belum Dibuka"
                            null -> "Status sesi harian belum dapat diverifikasi"
                        },
                    isKnown = isDailySessionKnown
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
                    dailyStockItems = dailyStockItems,
                    categories = categories,
                    pagination = pagination
                )
            }

            runCatching {
                menuCacheStore?.write(
                    token = token,
                    search = search,
                    categoryId = categoryId,
                    page = page,
                    perPage = perPage,
                    payload = payload
                )
            }

            payload
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

    private fun MenuListPayload.withResolvedImageUrls(): MenuListPayload {
        return copy(
            menus = menus.map { menu ->
                menu.copy(
                    variants = menu.variants.map { variant ->
                        variant.copy(
                            imageUrl = resolveMenuImageUrl(
                                rawImageUrl = variant.imageUrl,
                                apiBaseUrl = BuildConfig.API_BASE_URL
                            )
                        )
                    }
                )
            }
        )
    }
}
