package com.sipos.kebabsk.feature.expense.presentation

import com.sipos.kebabsk.feature.expense.domain.repository.FakeOperationalExpenseRepository
import com.sipos.kebabsk.testutil.MainDispatcherRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OperationalExpenseViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = FakeOperationalExpenseRepository()
    private val viewModel = OperationalExpenseViewModel(fakeRepository)

    @Test
    fun submitExpenseWithValidNominalCallsRepository() = runTest {
        viewModel.onAmountChanged("150000")
        viewModel.onCategoryChanged("  Kas Besar  ")
        viewModel.onNoteChanged("  Test note  ")
        
        viewModel.submit()
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertEquals("Sukses", state.successMessage)
        assertNull(state.errorMessage)
        assertEquals(1, fakeRepository.submitCalls)
        assertEquals(150000L, fakeRepository.lastAmount)
        assertEquals("Kas Besar", fakeRepository.lastSource)
        assertEquals("Test note", fakeRepository.lastNote)
    }

    @Test
    fun submitExpenseWithEmptyNominalIsRejected() = runTest {
        viewModel.onAmountChanged("")
        
        viewModel.submit()
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertNull(state.successMessage)
        assertEquals("Nominal pengeluaran tidak valid.", state.errorMessage)
        assertEquals(0, fakeRepository.submitCalls)
    }

    @Test
    fun submitExpenseWithZeroNominalIsRejected() = runTest {
        viewModel.onAmountChanged("0")
        
        viewModel.submit()
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertNull(state.successMessage)
        assertEquals("Nominal pengeluaran tidak valid.", state.errorMessage)
        assertEquals(0, fakeRepository.submitCalls)
    }

    @Test
    fun submitExpenseWithLettersNominalIsRejected() = runTest {
        viewModel.onAmountChanged("abc")
        viewModel.onCategoryChanged("Kas Besar")

        viewModel.submit()
        testScheduler.advanceUntilIdle()

        assertEquals("Nominal pengeluaran tidak valid.", viewModel.uiState.value.errorMessage)
        assertEquals(0, fakeRepository.submitCalls)
    }

    @Test
    fun submitExpenseWithOverflowNominalIsRejected() = runTest {
        viewModel.onAmountChanged("999999999999999999999")
        viewModel.onCategoryChanged("Kas Besar")

        viewModel.submit()
        testScheduler.advanceUntilIdle()

        assertEquals("Nominal pengeluaran tidak valid.", viewModel.uiState.value.errorMessage)
        assertEquals(0, fakeRepository.submitCalls)
    }

    @Test
    fun submitExpenseWithBlankCategoryIsRejected() = runTest {
        viewModel.onAmountChanged("150000")
        viewModel.onCategoryChanged("   ")

        viewModel.submit()
        testScheduler.advanceUntilIdle()

        assertEquals("Kategori pengeluaran wajib diisi.", viewModel.uiState.value.errorMessage)
        assertEquals(0, fakeRepository.submitCalls)
    }

    @Test
    fun submitExpenseBlankNoteBecomesNull() = runTest {
        viewModel.onAmountChanged("150000")
        viewModel.onCategoryChanged("Kas Besar")
        viewModel.onNoteChanged("   ")

        viewModel.submit()
        testScheduler.advanceUntilIdle()

        assertEquals(1, fakeRepository.submitCalls)
        assertNull(fakeRepository.lastNote)
    }

    @Test
    fun submitExpenseRepositoryFailureShowsError() = runTest {
        fakeRepository.shouldFail = true
        
        viewModel.onAmountChanged("150000")
        viewModel.onCategoryChanged("Kas Besar")
        
        viewModel.submit()
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertNull(state.successMessage)
        assertEquals("Test failure", state.errorMessage)
    }

    @Test
    fun submitExpenseDoubleTapOnlyCallsRepositoryOnce() = runTest {
        fakeRepository.delayMs = 200

        viewModel.onAmountChanged("150000")
        viewModel.onCategoryChanged("Kas Besar")

        viewModel.submit()
        viewModel.submit()
        testScheduler.runCurrent()

        assertEquals(1, fakeRepository.submitCalls)

        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun submitExpenseCancellationResetsSavingWithoutError() = runTest {
        fakeRepository.cancellation = CancellationException("cancelled")

        viewModel.onAmountChanged("150000")
        viewModel.onCategoryChanged("Kas Besar")

        viewModel.submit()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, fakeRepository.submitCalls)
        assertFalse(state.isSaving)
        assertNull(state.successMessage)
        assertNull(state.errorMessage)
    }
}

