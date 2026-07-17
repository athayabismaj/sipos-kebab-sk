package com.sipos.kebabsk.feature.menu.presentation

import com.sipos.kebabsk.feature.menu.domain.model.DailySessionStatus
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuListPayload
import com.sipos.kebabsk.feature.menu.domain.model.MenuUser
import com.sipos.kebabsk.feature.menu.domain.model.MenuVariant
import com.sipos.kebabsk.feature.menu.domain.repository.MenuRepository
import com.sipos.kebabsk.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
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

        viewModel.onCategorySelected("Kebab")

        assertEquals("Kebab", viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun clear_resetsStateAndAllowsReloadForSameToken() = runTest {
        val fakeRepository = FakeMenuRepository()
        val viewModel = MenuViewModel(fakeRepository)

        viewModel.loadMenus("token")
        advanceUntilIdle()
        viewModel.onCategorySelected("Kebab")
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
}

private class FakeMenuRepository(
    private var result: Result<MenuListPayload> = Result.success(samplePayload()),
    private val delayMs: Long = 0L
) : MenuRepository {
    var getMenusCalls: Int = 0
    var lastToken: String? = null

    override suspend fun getMenus(
        token: String,
        search: String?,
        categoryId: Long?
    ): Result<MenuListPayload> {
        getMenusCalls += 1
        lastToken = token
        if (delayMs > 0) delay(delayMs)
        return result
    }
}

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
