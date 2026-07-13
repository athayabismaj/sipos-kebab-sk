package com.sipos.kebabsk.feature.transactions.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.AppSessionStore
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.feature.transactions.domain.repository.TransactionsRepository
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionHistoryItem
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionReceipt
import com.sipos.kebabsk.feature.transactions.domain.usecase.GetTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class VoidReason { RESTOCK, WASTE }

data class TransactionsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentDate: LocalDate = AppTime.todayJakarta(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalRevenue: Long = 0L,
    val totalTransactionsCount: Int = 0,
    val allTransactions: List<TransactionHistoryItem> = emptyList(),
    val paginatedTransactions: List<TransactionHistoryItem> = emptyList(),
    val isVoiding: Boolean = false,
    val voidSuccess: Boolean = false,
    val voidMessage: String? = null,
    val voidErrorMessage: String? = null,
    val receiptTransactionId: Long? = null,
    val isLoadingReceipt: Boolean = false,
    val receipt: TransactionReceipt? = null,
    val receiptErrorMessage: String? = null
)

class TransactionsViewModel(
    private val repository: TransactionsRepository
) : ViewModel() {
    
    private val getTransactionsUseCase = GetTransactionsUseCase(repository)

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()
    private var fetchJob: Job? = null

    init {
        fetchTransactions()
    }

    companion object {
    }

    fun fetchTransactions() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            val token = AppSessionStore.loadSession()?.token ?: ""
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val summaryResult = repository.getRevenueSummary(token, _uiState.value.currentDate)
            val summary = summaryResult.getOrNull()

            val currentPage = _uiState.value.currentPage
            val result = getTransactionsUseCase(token, _uiState.value.currentDate, currentPage)
            result.onSuccess { pageData ->
                val newItems = pageData.items
                val totalPages = pageData.totalPages
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalRevenue = summary?.totalRevenue ?: 0L,
                        totalTransactionsCount = summary?.totalCount ?: newItems.size,
                        allTransactions = newItems,
                        totalPages = totalPages,
                        currentPage = currentPage,
                        paginatedTransactions = newItems
                    )
                }
            }.onFailure { error ->
                if (error is kotlinx.coroutines.CancellationException || error.message?.contains("cancelled", ignoreCase = true) == true) {
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = sanitizeUserMessage(error.message, "Riwayat transaksi belum bisa dimuat. Silakan coba lagi.")
                    )
                }
            }
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.currentPage >= state.totalPages) return
        _uiState.update { it.copy(currentPage = state.currentPage + 1) }
        fetchTransactions()
    }

    fun loadPreviousPage() {
        val state = _uiState.value
        if (state.isLoading || state.currentPage <= 1) return
        _uiState.update { it.copy(currentPage = state.currentPage - 1) }
        fetchTransactions()
    }

    fun setDate(newDate: LocalDate) {
        if (_uiState.value.isLoading && _uiState.value.currentDate == newDate) return
        _uiState.update { it.copy(currentDate = newDate, currentPage = 1) }
        fetchTransactions()
    }

    fun nextDay() {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(currentDate = it.currentDate.plusDays(1), currentPage = 1)
        }
        fetchTransactions()
    }

    fun previousDay() {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(currentDate = it.currentDate.minusDays(1), currentPage = 1)
        }
        fetchTransactions()
    }

    fun voidTransaction(transactionId: Long, reason: VoidReason, sessionId: Long) {
        if (_uiState.value.isVoiding) return
        if (_uiState.value.currentDate != AppTime.todayJakarta()) {
            _uiState.update {
                it.copy(
                    voidSuccess = false,
                    voidErrorMessage = "Transaksi tanggal sebelumnya hanya dapat dicetak ulang, tidak dapat dibatalkan."
                )
            }
            return
        }
        _uiState.update { it.copy(isVoiding = true, voidSuccess = false, voidErrorMessage = null, voidMessage = null) }
        val reasonString = reason.name.lowercase()
        val token = AppSessionStore.loadSession()?.token ?: ""
        viewModelScope.launch {
            val result = repository.voidTransaction(token, transactionId, reasonString, sessionId)
            result.onSuccess { message ->
                _uiState.update {
                    it.copy(
                        isVoiding = false,
                        voidSuccess = true,
                        voidMessage = message,
                        voidErrorMessage = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isVoiding = false,
                        voidSuccess = false,
                        voidErrorMessage = sanitizeUserMessage(error.message, "Gagal membatalkan transaksi. Silakan coba lagi.")
                    )
                }
            }
        }
    }

    fun clearVoidState() {
        _uiState.update { it.copy(voidSuccess = false, voidMessage = null, voidErrorMessage = null) }
    }

    fun openReceipt(transaction: TransactionHistoryItem) {
        openReceipt(transaction.id, transaction)
    }

    fun openReceipt(transactionId: Long) {
        val transaction = _uiState.value.allTransactions.firstOrNull { it.id == transactionId }
        openReceipt(transactionId, transaction)
    }

    private fun openReceipt(transactionId: Long, transaction: TransactionHistoryItem?) {
        if (_uiState.value.isLoadingReceipt) return
        val fallbackReceipt = transaction?.toFallbackReceipt(_uiState.value.currentDate)
        _uiState.update {
            it.copy(
                receiptTransactionId = transactionId,
                isLoadingReceipt = true,
                receipt = fallbackReceipt,
                receiptErrorMessage = null
            )
        }

        val token = AppSessionStore.loadSession()?.token ?: ""
        viewModelScope.launch {
            repository.getTransactionReceipt(token, transactionId, transaction?.code)
                .onSuccess { receipt ->
                    val resolvedReceipt = receipt
                        .withResolvedCashierName()
                    _uiState.update {
                        it.copy(
                            isLoadingReceipt = false,
                            receipt = resolvedReceipt,
                            receiptErrorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingReceipt = false,
                            receipt = fallbackReceipt,
                            receiptErrorMessage = if (fallbackReceipt == null) {
                                sanitizeUserMessage(
                                    error.message,
                                    "Detail struk belum bisa dimuat. Silakan coba lagi."
                                )
                            } else {
                                null
                            }
                        )
                    }
                }
        }
    }

    fun dismissReceipt() {
        _uiState.update {
            it.copy(
                receiptTransactionId = null,
                isLoadingReceipt = false,
                receipt = null,
                receiptErrorMessage = null
            )
        }
    }

    private fun TransactionHistoryItem.toFallbackReceipt(selectedDate: LocalDate): TransactionReceipt {
        return TransactionReceipt(
            id = id,
            code = code,
            createdAtLabel = formatReceiptDateLabel(originalDate, selectedDate, time),
            paymentMethod = "Tunai",
            totalAmount = total,
            paidAmount = total,
            changeAmount = 0L,
            status = status,
            items = emptyList(),
            cashierName = resolvedCashierName(),
            isDetailed = false
        )
    }

    private fun TransactionReceipt.withResolvedCashierName(): TransactionReceipt {
        return if (cashierName.isValidCashierName()) {
            this
        } else {
            copy(cashierName = resolvedCashierName())
        }
    }

    private fun resolvedCashierName(): String {
        val name = AppSessionStore.loadSession()?.displayName ?: "Kasir"
        return name.trim().takeIf { it.isNotBlank() } ?: "Kasir"
    }

    private fun String.isValidCashierName(): Boolean {
        val normalized = trim()
        return normalized.isNotBlank() && !normalized.equals("Kebab SK POS", ignoreCase = true)
    }

    private fun formatReceiptDateLabel(originalDate: String, selectedDate: LocalDate, time: String): String {
        if (originalDate.isNotBlank()) {
            val parsed = runCatching {
                val normalized = originalDate
                    .replace("T", " ")
                    .substringBefore(".")
                    .substringBefore("+")
                    .removeSuffix("Z")
                    .trim()
                LocalDateTime.parse(
                    normalized.take(19),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
            }.getOrNull()

            if (parsed != null) {
                return parsed.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")))
            }
        }

        val safeTime = time.takeIf { it.isNotBlank() } ?: "00:00"
        return "${selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID")))}, $safeTime"
    }
}

