package com.sipos.kebabsk.feature.transactions.domain.repository

import com.sipos.kebabsk.feature.transactions.domain.model.TransactionHistoryItem
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionPageData
import com.sipos.kebabsk.feature.transactions.domain.model.RevenueSummaryResult
import java.time.LocalDate

interface TransactionsRepository {
    suspend fun getTransactions(token: String, date: LocalDate, page: Int = 1): Result<TransactionPageData>
    suspend fun getRevenueSummary(token: String, date: LocalDate): Result<RevenueSummaryResult>
    suspend fun getRevenueTrend(token: String, date: LocalDate): Result<List<Pair<String, Double>>>
}
