package com.sipos.kebabsk.feature.transactions.presentation

import com.sipos.kebabsk.feature.transactions.domain.model.TransactionHistoryItem
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionPageData
import com.sipos.kebabsk.feature.transactions.domain.model.RevenueSummaryResult
import com.sipos.kebabsk.feature.transactions.domain.repository.TransactionsRepository
import com.sipos.kebabsk.feature.transactions.domain.usecase.GetTransactionsUseCase
import com.sipos.kebabsk.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun setDate_sameDateWhileLoading_doesNotCreateDuplicateRequest() = runTest {
        val fakeRepo = FakeTransactionsRepository(delayMs = 300)
        val viewModel = TransactionsViewModel(
            getTransactionsUseCase = GetTransactionsUseCase(fakeRepo),
            repository = fakeRepo,
            token = "token"
        )

        runCurrent()
        val sameDate = viewModel.uiState.value.currentDate
        viewModel.setDate(sameDate)
        runCurrent()

        assertEquals(1, fakeRepo.getTransactionsCalls)
        advanceUntilIdle()
    }

    @Test
    fun loadNextPage_whileLoading_ignoredByGuard() = runTest {
        val fakeRepo = FakeTransactionsRepository(delayMs = 0)
        val viewModel = TransactionsViewModel(
            getTransactionsUseCase = GetTransactionsUseCase(fakeRepo),
            repository = fakeRepo,
            token = "token"
        )

        advanceUntilIdle() // initial load finished, totalPages becomes 3
        assertEquals(1, viewModel.uiState.value.currentPage)

        fakeRepo.delayMs = 400
        viewModel.fetchTransactions() // set loading true
        runCurrent()
        viewModel.loadNextPage()
        runCurrent()

        assertEquals(2, fakeRepo.getTransactionsCalls)
        assertEquals(1, viewModel.uiState.value.currentPage)
        advanceUntilIdle()
    }

    @Test
    fun fetchTransactions_whenTechnicalError_usesFriendlyFallbackMessage() = runTest {
        val fakeRepo = FakeTransactionsRepository(
            delayMs = 0,
            shouldFail = true,
            failureMessage = "HTTP Error 500 from /api/transactions"
        )
        val viewModel = TransactionsViewModel(
            getTransactionsUseCase = GetTransactionsUseCase(fakeRepo),
            repository = fakeRepo,
            token = "token"
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage?.contains("Riwayat transaksi") == true)
    }
}

private class FakeTransactionsRepository(
    var delayMs: Long,
    private val shouldFail: Boolean = false,
    private val failureMessage: String = "Riwayat transaksi gagal"
) : TransactionsRepository {
    var getTransactionsCalls: Int = 0

    override suspend fun getTransactions(
        token: String,
        date: LocalDate,
        page: Int
    ): Result<TransactionPageData> {
        getTransactionsCalls += 1
        if (delayMs > 0) delay(delayMs)
        if (shouldFail) {
            return Result.failure(IllegalStateException(failureMessage))
        }
        return Result.success(
            TransactionPageData(
                items = listOf(
                    TransactionHistoryItem(
                        id = 1L,
                        code = "TRX-0001",
                        time = "10:00",
                        itemCount = 1,
                        total = 12000.0,
                        status = "Selesai",
                        originalDate = date.toString()
                    )
                ),
                totalPages = 3
            )
        )
    }

    override suspend fun getRevenueSummary(token: String, date: LocalDate): Result<RevenueSummaryResult> {
        return Result.success(
            RevenueSummaryResult(
                totalRevenue = 0.0,
                totalCount = 0,
                transactionGrowthPercentage = null,
                dominantItemName = null,
                revenueTargetPercentage = null,
                dailyTargetRevenue = null
            )
        )
    }

    override suspend fun getRevenueTrend(token: String, date: LocalDate): Result<List<Pair<String, Double>>> {
        return Result.success(emptyList())
    }

    override suspend fun voidTransaction(token: String, transactionId: Long, reason: String, sessionId: Long): Result<String> {
        return Result.success("Success")
    }
}
