package com.sipos.kebabsk.feature.transactions.domain.repository

import com.sipos.kebabsk.feature.transactions.domain.model.TransactionHistoryItem
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionPageData
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionReceipt
import com.sipos.kebabsk.feature.transactions.domain.model.RevenueSummaryResult
import java.time.LocalDate

interface TransactionsRepository {
    suspend fun getTransactions(token: String, date: LocalDate, page: Int = 1): Result<TransactionPageData>
    suspend fun getTransactionReceipt(token: String, transactionId: Long, transactionCode: String? = null): Result<TransactionReceipt>
    suspend fun getRevenueSummary(token: String, date: LocalDate): Result<RevenueSummaryResult>
    suspend fun getRevenueTrend(token: String, date: LocalDate): Result<List<Pair<String, Long>>>
    suspend fun voidTransaction(token: String, transactionId: Long, reason: String, sessionId: Long): Result<String>
}
