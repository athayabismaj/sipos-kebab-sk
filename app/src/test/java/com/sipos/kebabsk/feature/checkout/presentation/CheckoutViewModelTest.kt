package com.sipos.kebabsk.feature.checkout.presentation

import com.sipos.kebabsk.feature.cart.domain.model.CartItem
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutRequestData
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutResult
import com.sipos.kebabsk.feature.checkout.domain.model.PaymentMethod
import com.sipos.kebabsk.feature.checkout.domain.repository.CheckoutRepository
import com.sipos.kebabsk.testutil.MainDispatcherRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun loadPaymentMethods_whenSuccess_selectsCashMethod() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository(
            paymentMethodsResult = Result.success(
                listOf(
                    PaymentMethod(id = 1L, name = "QRIS"),
                    PaymentMethod(id = 2L, name = "Cash")
                )
            )
        )
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)

        viewModel.loadPaymentMethods("token")
        advanceUntilIdle()

        assertEquals(listOf(PaymentMethod(id = 2L, name = "Cash")), viewModel.uiState.value.paymentMethods)
        assertEquals(2L, viewModel.uiState.value.selectedPaymentMethodId)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadPaymentMethods_whenFailure_setsFriendlyError() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository(
            paymentMethodsResult = Result.failure(IllegalStateException("server down"))
        )
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)

        viewModel.loadPaymentMethods("token")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.paymentMethods.isEmpty())
        assertNull(viewModel.uiState.value.selectedPaymentMethodId)
        assertEquals(
            "Metode pembayaran belum tersedia. Hubungi admin untuk pengecekan.",
            viewModel.uiState.value.errorMessage
        )
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadPaymentMethods_filtersOnlyCashAndTunai() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository(
            paymentMethodsResult = Result.success(
                listOf(
                    PaymentMethod(id = 1L, name = "QRIS"),
                    PaymentMethod(id = 2L, name = "Tunai"),
                    PaymentMethod(id = 3L, name = "Transfer"),
                    PaymentMethod(id = 4L, name = "Cash")
                )
            )
        )
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)

        viewModel.loadPaymentMethods("token")
        advanceUntilIdle()

        assertEquals(listOf(2L, 4L), viewModel.uiState.value.paymentMethods.map { it.id })
        assertEquals(2L, viewModel.uiState.value.selectedPaymentMethodId)
    }

    @Test
    fun submitCheckout_whenCartEmpty_setsErrorAndSkipsCreateRequest() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository()
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)

        viewModel.loadPaymentMethods("token")
        advanceUntilIdle()

        viewModel.submitCheckout("token", emptyList(), isDailySessionOpen = true)
        advanceUntilIdle()

        assertEquals("Keranjang masih kosong", viewModel.uiState.value.errorMessage)
        assertEquals(0, fakeCheckoutRepo.createCalls)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun submitCheckout_whenDailySessionClosed_showsGateMessage() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository()
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)

        viewModel.loadPaymentMethods("token")
        advanceUntilIdle()
        viewModel.onPaidAmountChanged("12000")

        viewModel.submitCheckout("token", sampleCart(), isDailySessionOpen = false)
        advanceUntilIdle()

        assertEquals(
            "Sesi harian belum dibuka admin. Checkout belum bisa dilakukan.",
            viewModel.uiState.value.errorMessage
        )
        assertEquals(0, fakeCheckoutRepo.createCalls)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun submitCheckout_whenPaymentMethodNull_setsErrorAndSkipsCreateRequest() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository()
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)

        viewModel.onPaidAmountChanged("12000")

        viewModel.submitCheckout("token", sampleCart(), isDailySessionOpen = true)
        advanceUntilIdle()

        assertEquals("Metode pembayaran tunai belum tersedia", viewModel.uiState.value.errorMessage)
        assertEquals(0, fakeCheckoutRepo.createCalls)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun submitCheckout_whenPaidAmountEmpty_setsDeficitMessage() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository()
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)

        viewModel.loadPaymentMethods("token")
        advanceUntilIdle()

        viewModel.submitCheckout("token", sampleCart(), isDailySessionOpen = true)
        advanceUntilIdle()

        assertEquals(
            "Nominal pembayaran kurang. Silakan periksa kembali.",
            viewModel.uiState.value.errorMessage
        )
        assertEquals(0, fakeCheckoutRepo.createCalls)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun submitCheckout_whenPaidAmountLessThanTotal_setsFriendlyDeficitMessage() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository()
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)

        viewModel.loadPaymentMethods("token")
        advanceUntilIdle()
        viewModel.onPaidAmountChanged("10000")

        viewModel.submitCheckout("token", sampleCart(), isDailySessionOpen = true)
        advanceUntilIdle()

        assertEquals(
            "Nominal pembayaran kurang. Silakan periksa kembali.",
            viewModel.uiState.value.errorMessage
        )
        assertEquals(0, fakeCheckoutRepo.createCalls)
    }

    @Test
    fun submitCheckout_whenSuccess_setsReceiptSnapshotAndCallsSuccessOnce() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository()
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)
        var successCalls = 0

        viewModel.loadPaymentMethods("token")
        advanceUntilIdle()
        viewModel.onPaidAmountChanged("15000")

        viewModel.submitCheckout("token", sampleCart(), isDailySessionOpen = true) {
            successCalls += 1
        }
        advanceUntilIdle()

        assertEquals(1, fakeCheckoutRepo.createCalls)
        assertEquals(1, successCalls)
        assertEquals("TRX-TEST-1001", viewModel.uiState.value.checkoutTransactionCode)
        assertEquals(sampleCart(), viewModel.uiState.value.checkoutReceiptItems)
        assertEquals(12_000L, viewModel.uiState.value.checkoutTotalAmount)
        assertEquals(15_000L, viewModel.uiState.value.checkoutPaidAmount)
        assertEquals(3_000L, viewModel.uiState.value.checkoutChangeAmount)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun submitCheckout_whenRepositoryFailure_keepsCheckoutResultEmptyAndDoesNotCallSuccess() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository(
            createResult = Result.failure(IllegalStateException("network failure"))
        )
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)
        var successCalls = 0

        viewModel.loadPaymentMethods("token")
        advanceUntilIdle()
        viewModel.onPaidAmountChanged("15000")

        viewModel.submitCheckout("token", sampleCart(), isDailySessionOpen = true) {
            successCalls += 1
        }
        advanceUntilIdle()

        assertEquals(1, fakeCheckoutRepo.createCalls)
        assertEquals(0, successCalls)
        assertEquals("network failure", viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.checkoutTransactionCode)
        assertTrue(viewModel.uiState.value.checkoutReceiptItems.isEmpty())
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun submitCheckout_whenBackendSaysPaymentDeficit_mapsToFriendlyMessage() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository(
            createResult = Result.failure(IllegalStateException("deficit amount 2000"))
        )
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)

        viewModel.loadPaymentMethods("token")
        advanceUntilIdle()
        viewModel.onPaidAmountChanged("15000")

        viewModel.submitCheckout("token", sampleCart(), isDailySessionOpen = true)
        advanceUntilIdle()

        assertEquals(
            "Nominal pembayaran kurang. Silakan periksa kembali.",
            viewModel.uiState.value.errorMessage
        )
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun submitCheckout_doubleTap_onlyCreatesOneTransaction() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository(createDelayMs = 200)
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)

        viewModel.loadPaymentMethods("token")
        advanceUntilIdle()
        viewModel.onPaidAmountChanged("12000")

        viewModel.submitCheckout("token", sampleCart(), isDailySessionOpen = true)
        viewModel.submitCheckout("token", sampleCart(), isDailySessionOpen = true)
        advanceUntilIdle()

        assertEquals(1, fakeCheckoutRepo.createCalls)
        assertNotNull(viewModel.uiState.value.checkoutTransactionCode)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun submitCheckout_usesCartSnapshotForRequestAndReceipt() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository(createDelayMs = 200)
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)
        val mutableCart = sampleCart().toMutableList()

        viewModel.loadPaymentMethods("token")
        advanceUntilIdle()
        viewModel.onPaidAmountChanged("50000")

        viewModel.submitCheckout("token", mutableCart, isDailySessionOpen = true)
        mutableCart.clear()
        mutableCart.add(CartItem(variantId = 99L, menuName = "Changed", variantName = "Changed", quantity = 9, unitPrice = 99_000L))
        advanceUntilIdle()

        assertEquals(listOf(CheckoutRequestItem(11L, 1)), fakeCheckoutRepo.lastRequest?.items?.map {
            CheckoutRequestItem(it.variantId, it.qty)
        })
        assertEquals(sampleCart(), viewModel.uiState.value.checkoutReceiptItems)
    }

    @Test
    fun submitCheckout_cancellationResetsSubmittingWithoutCheckoutResult() = runTest {
        val fakeCheckoutRepo = FakeCheckoutRepository(
            createCancellation = CancellationException("cancelled")
        )
        val viewModel = CheckoutViewModel(fakeCheckoutRepo)
        var successCalls = 0

        viewModel.loadPaymentMethods("token")
        advanceUntilIdle()
        viewModel.onPaidAmountChanged("12000")

        viewModel.submitCheckout("token", sampleCart(), isDailySessionOpen = true) {
            successCalls += 1
        }
        advanceUntilIdle()

        assertEquals(1, fakeCheckoutRepo.createCalls)
        assertEquals(0, successCalls)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.checkoutTransactionCode)
        assertTrue(viewModel.uiState.value.checkoutReceiptItems.isEmpty())
    }

    private fun sampleCart(): List<CartItem> {
        return listOf(
            CartItem(
                variantId = 11L,
                menuName = "Burger",
                variantName = "Single",
                quantity = 1,
                unitPrice = 12_000L
            )
        )
    }
}

private data class CheckoutRequestItem(
    val variantId: Long,
    val qty: Int
)

private class FakeCheckoutRepository(
    private val paymentMethodsResult: Result<List<PaymentMethod>> = Result.success(
        listOf(PaymentMethod(id = 1L, name = "Cash"))
    ),
    private val createDelayMs: Long = 0L,
    private val createCancellation: CancellationException? = null,
    private val createResult: Result<CheckoutResult>? = null
) : CheckoutRepository {
    var createCalls: Int = 0
    var lastRequest: CheckoutRequestData? = null

    override suspend fun getPaymentMethods(token: String): Result<List<PaymentMethod>> {
        return paymentMethodsResult
    }

    override suspend fun createTransaction(
        token: String,
        request: CheckoutRequestData
    ): Result<CheckoutResult> {
        createCalls += 1
        lastRequest = request
        if (createDelayMs > 0) {
            delay(createDelayMs)
        }
        createCancellation?.let { throw it }
        return createResult ?: Result.success(
            CheckoutResult(
                transactionId = 1001L,
                transactionCode = "TRX-TEST-1001",
                totalAmount = request.items.sumOf { item ->
                    if (item.variantId == 11L) 12_000L * item.qty else 0L
                },
                paidAmount = request.paidAmount,
                changeAmount = request.paidAmount - request.items.sumOf { item ->
                    if (item.variantId == 11L) 12_000L * item.qty else 0L
                }
            )
        )
    }
}
