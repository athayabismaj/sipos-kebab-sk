package com.sipos.kebabsk.feature.shift.presentation

import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.feature.dailystock.domain.model.DailyStockResult
import com.sipos.kebabsk.feature.dailystock.domain.repository.FakeDailyStockRepository
import com.sipos.kebabsk.feature.shift.domain.repository.FakeCloseShiftRepository
import com.sipos.kebabsk.testutil.MainDispatcherRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CloseShiftViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeCloseShiftRepository = FakeCloseShiftRepository()
    private val fakeDailyStockRepository = FakeDailyStockRepository()

    @Test
    fun readinessSuccessWhenSessionActiveAndNoNullRemaining() = runTest {
        val items = listOf(DailyStockItem(1L, "Kebab", 10.0, 10.0, "pcs"))
        fakeDailyStockRepository.getDailyStockResult = Result.success(DailyStockResult(100L, items))

        val viewModel = CloseShiftViewModel(fakeCloseShiftRepository, fakeDailyStockRepository)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCheckingReadiness)
        assertTrue(state.isReadyToClose)
        assertEquals(100L, state.sessionId)
        assertNull(state.readinessMessage)
    }

    @Test
    fun readinessFailsWhenSessionIdIsNull() = runTest {
        fakeDailyStockRepository.getDailyStockResult = Result.success(DailyStockResult(null, emptyList()))

        val viewModel = CloseShiftViewModel(fakeCloseShiftRepository, fakeDailyStockRepository)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCheckingReadiness)
        assertFalse(state.isReadyToClose)
        assertEquals("Tidak ada sesi stok harian yang aktif.", state.readinessMessage)
    }

    @Test
    fun readinessFailsWhenItemsAreEmpty() = runTest {
        fakeDailyStockRepository.getDailyStockResult = Result.success(DailyStockResult(100L, emptyList()))

        val viewModel = CloseShiftViewModel(fakeCloseShiftRepository, fakeDailyStockRepository)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCheckingReadiness)
        assertFalse(state.isReadyToClose)
        assertEquals("Tidak ada data stok harian. Hubungi admin.", state.readinessMessage)
    }

    @Test
    fun readinessFailsWhenRemainingQuantityIsNull() = runTest {
        val items = listOf(DailyStockItem(1L, "Kebab", 10.0, null, "pcs"))
        fakeDailyStockRepository.getDailyStockResult = Result.success(DailyStockResult(100L, items))

        val viewModel = CloseShiftViewModel(fakeCloseShiftRepository, fakeDailyStockRepository)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCheckingReadiness)
        assertFalse(state.isReadyToClose)
        assertEquals("Harap isi stok sisa bahan baku terlebih dahulu!", state.readinessMessage)
    }

    @Test
    fun submitCloseShiftSuccessSetsReconciliationResult() = runTest {
        val items = listOf(DailyStockItem(1L, "Kebab", 10.0, 10.0, "pcs"))
        fakeDailyStockRepository.getDailyStockResult = Result.success(DailyStockResult(100L, items))

        val viewModel = CloseShiftViewModel(fakeCloseShiftRepository, fakeDailyStockRepository)
        testScheduler.advanceUntilIdle()

        viewModel.submitCloseShift(150000L, "Notes")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertNotNull(state.reconciliationResult)
        assertNull(state.errorMessage)
        assertEquals(1, fakeCloseShiftRepository.closeSessionCalls)
    }

    @Test
    fun submitCloseShiftZeroCashIsValid() = runTest {
        val items = listOf(DailyStockItem(1L, "Kebab", 10.0, 10.0, "pcs"))
        fakeDailyStockRepository.getDailyStockResult = Result.success(DailyStockResult(100L, items))

        val viewModel = CloseShiftViewModel(fakeCloseShiftRepository, fakeDailyStockRepository)
        testScheduler.advanceUntilIdle()

        viewModel.submitCloseShift(0L, "Notes")
        testScheduler.advanceUntilIdle()

        assertEquals(1, fakeCloseShiftRepository.closeSessionCalls)
        assertNotNull(viewModel.uiState.value.reconciliationResult)
    }

    @Test
    fun submitCloseShiftNegativeCashSkipsRepository() = runTest {
        val items = listOf(DailyStockItem(1L, "Kebab", 10.0, 10.0, "pcs"))
        fakeDailyStockRepository.getDailyStockResult = Result.success(DailyStockResult(100L, items))

        val viewModel = CloseShiftViewModel(fakeCloseShiftRepository, fakeDailyStockRepository)
        testScheduler.advanceUntilIdle()

        viewModel.submitCloseShift(-1L, "Notes")
        testScheduler.advanceUntilIdle()

        assertEquals(0, fakeCloseShiftRepository.closeSessionCalls)
        assertEquals("Nominal kas fisik tidak valid.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun submitCloseShiftFailureSetsErrorMessage() = runTest {
        val items = listOf(DailyStockItem(1L, "Kebab", 10.0, 10.0, "pcs"))
        fakeDailyStockRepository.getDailyStockResult = Result.success(DailyStockResult(100L, items))
        fakeCloseShiftRepository.shouldFail = true

        val viewModel = CloseShiftViewModel(fakeCloseShiftRepository, fakeDailyStockRepository)
        testScheduler.advanceUntilIdle()

        viewModel.submitCloseShift(150000L, "Notes")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals("Test failure", state.errorMessage)
    }

    @Test
    fun submitCloseShiftBlocksDoubleSubmit() = runTest {
        val items = listOf(DailyStockItem(1L, "Kebab", 10.0, 10.0, "pcs"))
        fakeDailyStockRepository.getDailyStockResult = Result.success(DailyStockResult(100L, items))
        fakeCloseShiftRepository.delayMs = 200

        val viewModel = CloseShiftViewModel(fakeCloseShiftRepository, fakeDailyStockRepository)
        testScheduler.advanceUntilIdle()

        viewModel.submitCloseShift(150000L, "Notes")
        viewModel.submitCloseShift(150000L, "Notes 2") // should be ignored

        testScheduler.runCurrent()
        assertEquals(1, fakeCloseShiftRepository.closeSessionCalls)

        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun submitCloseShiftCancellationResetsSubmittingWithoutError() = runTest {
        val items = listOf(DailyStockItem(1L, "Kebab", 10.0, 10.0, "pcs"))
        fakeDailyStockRepository.getDailyStockResult = Result.success(DailyStockResult(100L, items))
        fakeCloseShiftRepository.cancellation = CancellationException("cancelled")

        val viewModel = CloseShiftViewModel(fakeCloseShiftRepository, fakeDailyStockRepository)
        testScheduler.advanceUntilIdle()

        viewModel.submitCloseShift(150000L, "Notes")
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, fakeCloseShiftRepository.closeSessionCalls)
        assertFalse(state.isSubmitting)
        assertNull(state.errorMessage)
        assertNull(state.reconciliationResult)
    }
}
