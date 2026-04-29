package com.sipos.kebabsk.feature.menu.presentation

import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutRequestData
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutResult
import com.sipos.kebabsk.feature.checkout.domain.model.PaymentMethod
import com.sipos.kebabsk.feature.checkout.domain.repository.CheckoutRepository
import com.sipos.kebabsk.feature.checkout.domain.usecase.CreateTransactionUseCase
import com.sipos.kebabsk.feature.checkout.domain.usecase.GetPaymentMethodsUseCase
import com.sipos.kebabsk.feature.menu.domain.model.DailySessionStatus
import com.sipos.kebabsk.feature.menu.domain.model.MenuItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuListPayload
import com.sipos.kebabsk.feature.menu.domain.model.MenuUser
import com.sipos.kebabsk.feature.menu.domain.model.MenuVariant
import com.sipos.kebabsk.feature.menu.domain.repository.MenuRepository
import com.sipos.kebabsk.feature.menu.domain.usecase.GetMenusUseCase
import com.sipos.kebabsk.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun submitCheckout_whenCartEmpty_setsErrorAndSkipsCreateRequest() = runTest {
        val fakeMenuRepo = FakeMenuRepository(isDailyOpen = true)
        val fakeCheckoutRepo = FakeCheckoutRepository()
        val viewModel = buildViewModel(fakeMenuRepo, fakeCheckoutRepo)

        viewModel.loadMenus("token", forceRefresh = true)
        advanceUntilIdle()

        viewModel.submitCheckout("token")
        advanceUntilIdle()

        assertEquals("Keranjang masih kosong", viewModel.uiState.value.errorMessage)
        assertEquals(0, fakeCheckoutRepo.createCalls)
    }

    @Test
    fun submitCheckout_whenDailySessionClosed_showsGateMessage() = runTest {
        val fakeMenuRepo = FakeMenuRepository(isDailyOpen = false)
        val fakeCheckoutRepo = FakeCheckoutRepository()
        val viewModel = buildViewModel(fakeMenuRepo, fakeCheckoutRepo)

        viewModel.loadMenus("token", forceRefresh = true)
        advanceUntilIdle()
        viewModel.addVariantToCart("Burger", 11L, "Single", 12000.0)
        viewModel.onQuickAmountSelected(12000)

        viewModel.submitCheckout("token")
        advanceUntilIdle()

        assertEquals(
            "Sesi harian belum dibuka admin. Checkout belum bisa dilakukan.",
            viewModel.uiState.value.errorMessage
        )
        assertEquals(0, fakeCheckoutRepo.createCalls)
    }

    @Test
    fun submitCheckout_doubleTap_onlyCreatesOneTransaction() = runTest {
        val fakeMenuRepo = FakeMenuRepository(isDailyOpen = true)
        val fakeCheckoutRepo = FakeCheckoutRepository(createDelayMs = 200)
        val viewModel = buildViewModel(fakeMenuRepo, fakeCheckoutRepo)

        viewModel.loadMenus("token", forceRefresh = true)
        advanceUntilIdle()
        viewModel.addVariantToCart("Burger", 11L, "Single", 12000.0)
        viewModel.onQuickAmountSelected(12000)

        viewModel.submitCheckout("token")
        viewModel.submitCheckout("token")
        advanceUntilIdle()

        assertEquals(1, fakeCheckoutRepo.createCalls)
        assertNotNull(viewModel.uiState.value.checkoutTransactionCode)
        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
    }

    @Test
    fun submitCheckout_whenBackendSaysSessionClosed_mapsToFriendlyMessage() = runTest {
        val fakeMenuRepo = FakeMenuRepository(isDailyOpen = true)
        val fakeCheckoutRepo = FakeCheckoutRepository(
            shouldFail = true,
            failureMessage = "sesi harian belum dibuka"
        )
        val viewModel = buildViewModel(fakeMenuRepo, fakeCheckoutRepo)

        viewModel.loadMenus("token", forceRefresh = true)
        advanceUntilIdle()
        viewModel.addVariantToCart("Burger", 11L, "Single", 12000.0)
        viewModel.onQuickAmountSelected(12000)

        viewModel.submitCheckout("token")
        advanceUntilIdle()

        assertEquals(
            "Sesi harian belum dibuka admin. Checkout belum bisa dilakukan.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun submitCheckout_whenBackendSaysPaymentDeficit_mapsToFriendlyMessage() = runTest {
        val fakeMenuRepo = FakeMenuRepository(isDailyOpen = true)
        val fakeCheckoutRepo = FakeCheckoutRepository(
            shouldFail = true,
            failureMessage = "deficit amount 2000"
        )
        val viewModel = buildViewModel(fakeMenuRepo, fakeCheckoutRepo)

        viewModel.loadMenus("token", forceRefresh = true)
        advanceUntilIdle()
        viewModel.addVariantToCart("Burger", 11L, "Single", 12000.0)
        viewModel.onQuickAmountSelected(10000)

        viewModel.submitCheckout("token")
        advanceUntilIdle()

        assertEquals(
            "Nominal pembayaran kurang. Silakan periksa kembali.",
            viewModel.uiState.value.errorMessage
        )
    }

    private fun buildViewModel(
        fakeMenuRepo: FakeMenuRepository,
        fakeCheckoutRepo: FakeCheckoutRepository
    ): MenuViewModel {
        return MenuViewModel(
            getMenusUseCase = GetMenusUseCase(fakeMenuRepo),
            getPaymentMethodsUseCase = GetPaymentMethodsUseCase(fakeCheckoutRepo),
            createTransactionUseCase = CreateTransactionUseCase(fakeCheckoutRepo)
        )
    }
}

private class FakeMenuRepository(
    private val isDailyOpen: Boolean
) : MenuRepository {
    override suspend fun getMenus(
        token: String,
        search: String?,
        categoryId: Long?
    ): Result<MenuListPayload> {
        return Result.success(
            MenuListPayload(
                user = MenuUser(
                    id = 1L,
                    name = "Kasir Test",
                    role = "kasir",
                    isPrivileged = false
                ),
                menus = listOf(
                    MenuItem(
                        id = 1L,
                        name = "Burger",
                        description = null,
                        isActive = true,
                        categoryName = "Makanan",
                        variants = listOf(
                            MenuVariant(
                                id = 11L,
                                name = "Single",
                                price = 12000.0,
                                isAvailable = true
                            )
                        )
                    )
                ),
                dailySession = DailySessionStatus(
                    isOpen = isDailyOpen,
                    label = if (isDailyOpen) "Sesi harian aktif" else "Sesi belum dibuka"
                ),
                dailyStockItems = emptyList()
            )
        )
    }
}

private class FakeCheckoutRepository(
    private val createDelayMs: Long = 0L,
    private val shouldFail: Boolean = false,
    private val failureMessage: String = "Pembayaran belum berhasil."
) : CheckoutRepository {
    var createCalls: Int = 0

    override suspend fun getPaymentMethods(token: String): Result<List<PaymentMethod>> {
        return Result.success(listOf(PaymentMethod(id = 1L, name = "Cash")))
    }

    override suspend fun createTransaction(
        token: String,
        request: CheckoutRequestData
    ): Result<CheckoutResult> {
        createCalls += 1
        if (createDelayMs > 0) {
            delay(createDelayMs)
        }
        if (shouldFail) {
            return Result.failure(IllegalStateException(failureMessage))
        }
        return Result.success(
            CheckoutResult(
                transactionId = 1001L,
                transactionCode = "TRX-TEST-1001",
                totalAmount = 12000.0,
                paidAmount = request.paidAmount,
                changeAmount = request.paidAmount - 12000.0
            )
        )
    }
}
