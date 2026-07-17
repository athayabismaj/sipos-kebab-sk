package com.sipos.kebabsk.feature.transactions.data.repository

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.sipos.kebabsk.feature.transactions.data.remote.RevenueSummaryResponse
import com.sipos.kebabsk.feature.transactions.data.remote.TransactionsApiService
import com.sipos.kebabsk.feature.transactions.data.remote.TransactionsResponse
import com.sipos.kebabsk.feature.transactions.data.remote.VoidTransactionRequest
import com.sipos.kebabsk.feature.transactions.data.remote.VoidTransactionResponse
import com.sipos.kebabsk.testutil.ContractFixtureLoader
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.time.LocalDate

class TransactionsContractFixtureTest {
    private val gson = Gson()

    @Test
    fun paginatedHistoryKeepsPageStatusOrderAndLongAmounts() = runTest {
        val repository = TransactionsRepositoryImpl(
            FixtureTransactionsApiService(historyFixture = "transaction_history_page_1.json")
        )

        val page = repository.getTransactions("fixture-token", LocalDate.of(2026, 7, 15), 1)
            .getOrThrow()

        assertEquals(2, page.totalPages)
        assertEquals(listOf(1002L, 1001L), page.items.map { it.id })
        assertEquals(listOf("Sukses", "Void"), page.items.map { it.status })
        assertEquals(listOf(20_000L, 10_000L), page.items.map { it.total })
        assertEquals(listOf("11:00", "10:00"), page.items.map { it.time })
    }

    @Test
    fun emptyHistoryKeepsSuccessfulEmptyPageContract() = runTest {
        val repository = TransactionsRepositoryImpl(
            FixtureTransactionsApiService(historyFixture = "transaction_history_empty.json")
        )

        val page = repository.getTransactions("token", LocalDate.of(2026, 7, 15), 1).getOrThrow()

        assertTrue(page.items.isEmpty())
        assertEquals(1, page.totalPages)
    }

    @Test
    fun transactionDetailMapsReceiptItemsPaymentAndLargeAmounts() = runTest {
        val repository = TransactionsRepositoryImpl(
            FixtureTransactionsApiService(
                historyFixture = "transaction_history_empty.json",
                detailFixture = "receipt.json"
            )
        )

        val receipt = repository.getTransactionReceipt(
            token = "token",
            transactionId = 1003L,
            transactionCode = "TRX-FIX-20260715-003"
        ).getOrThrow()

        assertEquals("TRX-FIX-20260715-003", receipt.code)
        assertEquals("Cash", receipt.paymentMethod)
        assertEquals(1_250_000L, receipt.totalAmount)
        assertEquals(1_300_000L, receipt.paidAmount)
        assertEquals(50_000L, receipt.changeAmount)
        assertEquals("Jl. Kampus UMK, Kudus", receipt.branchAddress)
        assertEquals(1, receipt.items.size)
        assertEquals("Kebab Spesial Keju Mozzarella Panjang", receipt.items.single().name)
        assertEquals(1_250_000L, receipt.items.single().subtotal)
        assertTrue(receipt.isDetailed)
    }

    private fun response(name: String): TransactionsResponse {
        return gson.fromJson(ContractFixtureLoader.jsonObject(name), TransactionsResponse::class.java)
    }

    private inner class FixtureTransactionsApiService(
        private val historyFixture: String,
        private val detailFixture: String = "transaction_detail.json"
    ) : TransactionsApiService {
        override suspend fun getTransactions(
            authorization: String,
            date: String?,
            page: Int?
        ): Response<TransactionsResponse> = Response.success(response(historyFixture))

        override suspend fun getTransactionDetail(
            authorization: String,
            reference: String
        ): Response<JsonElement> = Response.success(ContractFixtureLoader.jsonObject(detailFixture))

        override suspend fun getTransactionReceiptDetail(
            authorization: String,
            reference: String
        ): Response<JsonElement> = Response.success(ContractFixtureLoader.jsonObject(detailFixture))

        override suspend fun getRevenueSummary(
            authorization: String,
            date: String?
        ): Response<RevenueSummaryResponse> = error("Not used in transaction fixture test")

        override suspend fun voidTransaction(
            authorization: String,
            id: Long,
            request: VoidTransactionRequest
        ): Response<VoidTransactionResponse> = error("Not used in transaction fixture test")
    }
}
