package com.sipos.kebabsk.feature.menu.presentation

import com.sipos.kebabsk.feature.menu.domain.model.DailySessionStatus
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuListPayload
import com.sipos.kebabsk.feature.menu.domain.model.MenuPagination
import com.sipos.kebabsk.feature.menu.domain.model.MenuUser
import com.sipos.kebabsk.feature.menu.domain.model.MenuVariant
import com.sipos.kebabsk.feature.menu.domain.repository.MenuRepository
import com.sipos.kebabsk.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun loadMenus_whenSuccess_populatesMenuCashierSessionAndStockData() = runTest {
        val fakeRepository = FakeMenuRepository()
        val viewModel = MenuViewModel(fakeRepository)

        viewModel.loadMenus("token")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(samplePayload().menus, state.menus)
        assertEquals("Cahyo", state.cashierName)
        assertEquals("kasir", state.cashierRole)
        assertTrue(state.isDailySessionOpen)
        assertTrue(state.isDailySessionStatusKnown)
        assertEquals("Sesi Harian Aktif", state.dailySessionStatusLabel)
        assertEquals(samplePayload().dailyStockItems, state.dailyStockItems)
        assertNull(state.errorMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun loadMenus_whenFailure_setsErrorAndStopsLoading() = runTest {
        val fakeRepository = FakeMenuRepository(
            result = Result.failure(IllegalStateException("menu gagal dimuat"))
        )
        val viewModel = MenuViewModel(fakeRepository)

        viewModel.loadMenus("token")
        advanceUntilIdle()

        assertEquals("menu gagal dimuat", viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.menus.isEmpty())
        assertFalse(viewModel.uiState.value.isDailySessionStatusKnown)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadMenus_sameTokenWhenMenusAlreadyLoaded_doesNotDuplicateRepositoryCall() = runTest {
        val fakeRepository = FakeMenuRepository()
        val viewModel = MenuViewModel(fakeRepository)
        var onLoadedCalls = 0

        viewModel.loadMenus("token") { onLoadedCalls += 1 }
        advanceUntilIdle()
        viewModel.loadMenus("token") { onLoadedCalls += 1 }
        advanceUntilIdle()

        assertEquals(1, fakeRepository.getMenusCalls)
        assertEquals(2, onLoadedCalls)
    }

    @Test
    fun loadMenus_parallelCallsOnlyCallRepositoryOnce() = runTest {
        val fakeRepository = FakeMenuRepository(delayMs = 200)
        val viewModel = MenuViewModel(fakeRepository)

        viewModel.loadMenus("token")
        viewModel.loadMenus("token")
        runCurrent()

        assertEquals(1, fakeRepository.getMenusCalls)
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(samplePayload().menus, viewModel.uiState.value.menus)
    }

    @Test
    fun forceRefreshMenus_callsRepositoryAgainForSameToken() = runTest {
        val fakeRepository = FakeMenuRepository()
        val viewModel = MenuViewModel(fakeRepository)

        viewModel.loadMenus("token")
        advanceUntilIdle()
        viewModel.forceRefreshMenus("token")
        advanceUntilIdle()

        assertEquals(2, fakeRepository.getMenusCalls)
    }

    @Test
    fun onCategorySelected_updatesSelectedCategory() = runTest {
        val viewModel = MenuViewModel(FakeMenuRepository())

        viewModel.onCategorySelected(12L)

        assertEquals(12L, viewModel.uiState.value.selectedCategoryId)
    }

    @Test
    fun clear_resetsStateAndAllowsReloadForSameToken() = runTest {
        val fakeRepository = FakeMenuRepository()
        val viewModel = MenuViewModel(fakeRepository)

        viewModel.loadMenus("token")
        advanceUntilIdle()
        viewModel.onCategorySelected(12L)
        viewModel.clear()

        assertEquals(MenuUiState(), viewModel.uiState.value)

        viewModel.loadMenus("token")
        advanceUntilIdle()

        assertEquals(2, fakeRepository.getMenusCalls)
        assertEquals(samplePayload().menus, viewModel.uiState.value.menus)
    }

    @Test
    fun prefetchMenusIfNeeded_whenMenusEmpty_loadsOnce() = runTest {
        val fakeRepository = FakeMenuRepository()
        val viewModel = MenuViewModel(fakeRepository)

        viewModel.prefetchMenusIfNeeded("token")
        advanceUntilIdle()
        viewModel.prefetchMenusIfNeeded("token")
        advanceUntilIdle()

        assertEquals(1, fakeRepository.getMenusCalls)
        assertEquals(samplePayload().menus, viewModel.uiState.value.menus)
    }

    @Test
    fun loadMenus_passesTokenToRepository() = runTest {
        val fakeRepository = FakeMenuRepository()
        val viewModel = MenuViewModel(fakeRepository)

        viewModel.loadMenus("token-123")
        advanceUntilIdle()

        assertEquals("token-123", fakeRepository.lastToken)
    }

    @Test
    fun refresh_keepsExistingMenuVisibleAndUsesRefreshingState() = runTest {
        val fakeRepository = FakeMenuRepository(delayMs = 200)
        val viewModel = MenuViewModel(fakeRepository)

        viewModel.loadMenus("token")
        advanceUntilIdle()
        val existingMenus = viewModel.uiState.value.menus

        viewModel.forceRefreshMenus("token")
        runCurrent()

        assertEquals(existingMenus, viewModel.uiState.value.menus)
        assertTrue(viewModel.uiState.value.isRefreshing)
        assertFalse(viewModel.uiState.value.isInitialLoading)

        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun loadNextPage_appendsVariantsWithoutDuplicatesAndStopsAtLastPage() = runTest {
        val firstPage = samplePayload().copy(
            pagination = MenuPagination(
                currentPage = 1,
                lastPage = 2,
                perPage = 20,
                total = 2,
                hasMore = true
            )
        )
        val secondVariant = MenuVariant(
            id = 12L,
            name = "Jumbo",
            price = 20_000L,
            isAvailable = true
        )
        val secondPage = samplePayload().copy(
            menus = listOf(
                samplePayload().menus.single().copy(
                    variants = listOf(samplePayload().menus.single().variants.single(), secondVariant)
                )
            ),
            pagination = MenuPagination(
                currentPage = 2,
                lastPage = 2,
                perPage = 20,
                total = 2,
                hasMore = false
            )
        )
        val fakeRepository = FakeMenuRepository(
            resultsByPage = mapOf(
                1 to Result.success(firstPage),
                2 to Result.success(secondPage)
            )
        )
        val viewModel = MenuViewModel(fakeRepository)

        viewModel.loadMenus("token")
        advanceUntilIdle()
        viewModel.loadNextPage()
        viewModel.loadNextPage()
        advanceUntilIdle()

        val variants = viewModel.uiState.value.menus.single().variants
        assertEquals(listOf(11L, 12L), variants.map { it.id })
        assertEquals(2, viewModel.uiState.value.currentPage)
        assertFalse(viewModel.uiState.value.hasMore)
        assertEquals(listOf(1, 2), fakeRepository.requests.map { it.page })
    }

    @Test
    fun categoryChange_resetsToFirstPageAndUsesServerCategoryId() = runTest {
        val firstPage = samplePayload().copy(
            pagination = MenuPagination(currentPage = 1, lastPage = 2, total = 2, hasMore = true)
        )
        val fakeRepository = FakeMenuRepository(result = Result.success(firstPage))
        val viewModel = MenuViewModel(fakeRepository)

        viewModel.loadMenus("token")
        advanceUntilIdle()
        viewModel.onCategorySelected(99L)
        advanceUntilIdle()

        assertEquals(99L, viewModel.uiState.value.selectedCategoryId)
        assertEquals(1, viewModel.uiState.value.currentPage)
        assertEquals(99L, fakeRepository.requests.last().categoryId)
        assertEquals(1, fakeRepository.requests.last().page)
    }

    @Test
    fun searchQuery_isDebouncedBeforeResettingTheFirstPage() = runTest {
        val fakeRepository = FakeMenuRepository()
        val viewModel = MenuViewModel(fakeRepository)

        viewModel.loadMenus("token")
        advanceUntilIdle()
        viewModel.onSearchQueryChanged("k")
        viewModel.onSearchQueryChanged("ke")
        viewModel.onSearchQueryChanged("keb")
        advanceTimeBy(349)
        runCurrent()

        assertEquals(1, fakeRepository.getMenusCalls)

        advanceTimeBy(1)
        advanceUntilIdle()

        assertEquals(2, fakeRepository.getMenusCalls)
        assertEquals("keb", fakeRepository.requests.last().search)
        assertEquals(1, fakeRepository.requests.last().page)
    }

    @Test
    fun cachedCatalog_isShownWhileFreshNetworkPageLoads() = runTest {
        val cached = samplePayload().copy(
            menus = listOf(samplePayload().menus.single().copy(name = "Kebab Cache"))
        )
        val fakeRepository = FakeMenuRepository(
            cachedPayload = cached,
            delayMs = 200
        )
        val viewModel = MenuViewModel(fakeRepository)

        viewModel.loadMenus("token")
        runCurrent()

        assertEquals("Kebab Cache", viewModel.uiState.value.menus.single().name)
        assertTrue(viewModel.uiState.value.isRefreshing)
        assertFalse(viewModel.uiState.value.isInitialLoading)

        advanceUntilIdle()
        assertEquals("Kebab", viewModel.uiState.value.menus.single().name)
    }
}

private class FakeMenuRepository(
    private var result: Result<MenuListPayload> = Result.success(samplePayload()),
    private val delayMs: Long = 0L,
    private val resultsByPage: Map<Int, Result<MenuListPayload>> = emptyMap(),
    private val cachedPayload: MenuListPayload? = null
) : MenuRepository {
    var getMenusCalls: Int = 0
    var lastToken: String? = null
    val requests = mutableListOf<MenuRequest>()

    override suspend fun getCachedMenus(
        token: String,
        search: String?,
        categoryId: Long?
    ): MenuListPayload? = cachedPayload

    override suspend fun getMenus(
        token: String,
        search: String?,
        categoryId: Long?,
        page: Int,
        perPage: Int
    ): Result<MenuListPayload> {
        getMenusCalls += 1
        lastToken = token
        requests += MenuRequest(search, categoryId, page)
        if (delayMs > 0) delay(delayMs)
        return resultsByPage[page] ?: result
    }
}

private data class MenuRequest(
    val search: String?,
    val categoryId: Long?,
    val page: Int
)

private fun samplePayload(): MenuListPayload {
    return MenuListPayload(
        user = MenuUser(
            id = 7L,
            name = "Cahyo",
            role = "kasir",
            isPrivileged = false
        ),
        menus = listOf(
            MenuItem(
                id = 1L,
                name = "Kebab",
                description = "Kebab kecil",
                isActive = true,
                categoryName = "Kebab",
                variants = listOf(
                    MenuVariant(
                        id = 11L,
                        name = "Mini",
                        price = 10_000L,
                        isAvailable = true
                    )
                )
            )
        ),
        dailySession = DailySessionStatus(
            isOpen = true,
            label = "Sesi Harian Aktif",
        ),
        dailyStockItems = listOf(
            DailyStockItem(
                ingredientId = 3L,
                name = "Selada",
                qty = 1.0,
                remainingQty = 0.8,
                unit = "kg"
            )
        )
    )
}
