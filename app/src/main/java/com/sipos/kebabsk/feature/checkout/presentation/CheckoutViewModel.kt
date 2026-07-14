package com.sipos.kebabsk.feature.checkout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.common.MoneyUtils
import com.sipos.kebabsk.feature.cart.domain.model.CartItem
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutItemInput
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutRequestData
import com.sipos.kebabsk.feature.checkout.domain.model.PaymentMethod
import com.sipos.kebabsk.feature.checkout.domain.repository.CheckoutRepository
import com.sipos.kebabsk.feature.checkout.domain.usecase.CreateTransactionUseCase
import com.sipos.kebabsk.feature.checkout.domain.usecase.GetPaymentMethodsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class CheckoutUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethodId: Long? = null,
    val paidAmountInput: String = "",
    val noteInput: String = "",
    val checkoutMessage: String? = null,
    val checkoutTransactionCode: String? = null,
    val checkoutChangeAmount: Long? = null,
    val checkoutTotalAmount: Long? = null,
    val checkoutPaidAmount: Long? = null,
    val checkoutReceiptItems: List<CartItem> = emptyList()
)

class CheckoutViewModel(
    private val checkoutRepository: CheckoutRepository
) : ViewModel() {

    private val getPaymentMethodsUseCase = GetPaymentMethodsUseCase(checkoutRepository)
    private val createTransactionUseCase = CreateTransactionUseCase(checkoutRepository)

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val checkoutMutex = Mutex()
    private var paymentMethodsLoaded = false

    fun loadPaymentMethods(token: String) {
        if (paymentMethodsLoaded || _uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val paymentResult = getPaymentMethodsUseCase(token)
                paymentResult
                    .onSuccess { methods ->
                        paymentMethodsLoaded = true
                        val cashMethods = methods.filter { it.isCashPaymentMethod() }
                        _uiState.update {
                            it.copy(
                                paymentMethods = cashMethods,
                                selectedPaymentMethodId = cashMethods.firstOrNull()?.id,
                                errorMessage = if (cashMethods.isEmpty()) {
                                    "Metode pembayaran tunai belum tersedia. Hubungi admin untuk pengecekan."
                                } else null
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                paymentMethods = emptyList(),
                                selectedPaymentMethodId = null,
                                errorMessage = sanitizeUserMessage(
                                    error.message,
                                    "Metode pembayaran belum tersedia. Hubungi admin untuk pengecekan."
                                )
                            )
                        }
                    }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onPaymentMethodSelected(paymentMethodId: Long) {
        _uiState.update { it.copy(selectedPaymentMethodId = paymentMethodId) }
    }

    fun onQuickAmountSelected(amount: Long) {
        _uiState.update {
            it.copy(
                paidAmountInput = amount.toString(),
                errorMessage = null,
                checkoutMessage = null,
                checkoutTransactionCode = null,
                checkoutChangeAmount = null,
                checkoutTotalAmount = null,
                checkoutPaidAmount = null,
                checkoutReceiptItems = emptyList()
            )
        }
    }

    fun onPaidAmountChanged(value: String) {
        _uiState.update {
            it.copy(
                paidAmountInput = MoneyUtils.sanitizeMoneyInput(value),
                errorMessage = null,
                checkoutMessage = null,
                checkoutTransactionCode = null,
                checkoutChangeAmount = null,
                checkoutTotalAmount = null,
                checkoutPaidAmount = null,
                checkoutReceiptItems = emptyList()
            )
        }
    }

    fun onNoteChanged(value: String) {
        _uiState.update { it.copy(noteInput = value) }
    }

    fun submitCheckout(token: String, cartItems: List<CartItem>, isDailySessionOpen: Boolean, onSuccess: () -> Unit = {}) {
        if (_uiState.value.isSubmitting) return
        val cartSnapshot = cartItems.map { it.copy() }
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                checkoutMutex.withLock {
                    val state = _uiState.value
                    if (cartSnapshot.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Keranjang masih kosong"
                            )
                        }
                        return@withLock
                    }

                    if (!isDailySessionOpen) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Sesi harian belum dibuka admin. Checkout belum bisa dilakukan."
                            )
                        }
                        return@withLock
                    }

                    val paymentMethodId = state.selectedPaymentMethodId
                    if (paymentMethodId == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Metode pembayaran tunai belum tersedia"
                            )
                        }
                        return@withLock
                    }

                    val paidAmount = MoneyUtils.parseRupiahInput(state.paidAmountInput) ?: 0L
                    if (paidAmount < cartSnapshot.sumOf { it.subtotal }) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Nominal pembayaran kurang. Silakan periksa kembali."
                            )
                        }
                        return@withLock
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = true,
                            errorMessage = null,
                            checkoutMessage = null,
                            checkoutTransactionCode = null,
                            checkoutChangeAmount = null,
                            checkoutTotalAmount = null,
                            checkoutPaidAmount = null,
                            checkoutReceiptItems = emptyList()
                        )
                    }
                    val request = CheckoutRequestData(
                        paymentMethodId = paymentMethodId,
                        paidAmount = paidAmount,
                        items = cartSnapshot.map { CheckoutItemInput(it.variantId, it.quantity) },
                        note = state.noteInput.takeIf { it.isNotBlank() }
                    )

                    createTransactionUseCase(token, request)
                        .onSuccess { result ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    paidAmountInput = "",
                                    noteInput = "",
                                    checkoutMessage = "Pembayaran berhasil: ",
                                    checkoutTransactionCode = result.transactionCode,
                                    checkoutChangeAmount = result.changeAmount,
                                    checkoutTotalAmount = result.totalAmount,
                                    checkoutPaidAmount = result.paidAmount,
                                    checkoutReceiptItems = cartSnapshot,
                                    errorMessage = null
                                )
                            }
                            onSuccess()
                        }
                        .onFailure { error ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = normalizeCheckoutError(error.message),
                                    checkoutMessage = null,
                                    checkoutTransactionCode = null,
                                    checkoutChangeAmount = null,
                                    checkoutTotalAmount = null,
                                    checkoutPaidAmount = null,
                                    checkoutReceiptItems = emptyList()
                                )
                            }
                        }
                }
            } finally {
                _uiState.update { it.copy(isSubmitting = false, isLoading = false) }
            }
        }
    }

    fun dismissCheckoutPreview() {
        _uiState.update {
            it.copy(
                checkoutTransactionCode = null,
                checkoutChangeAmount = null,
                checkoutTotalAmount = null,
                checkoutPaidAmount = null,
                checkoutReceiptItems = emptyList()
            )
        }
    }

    fun clearCheckoutMessage() {
        _uiState.update { it.copy(checkoutMessage = null) }
    }

    fun clear() {
        paymentMethodsLoaded = false
        _uiState.value = CheckoutUiState()
    }

    private fun normalizeCheckoutError(rawMessage: String?): String {
        val fallback = "Pembayaran belum berhasil. Silakan coba lagi."
        val message = sanitizeUserMessage(rawMessage, fallback)
        val lower = message.lowercase()

        return when {
            lower.contains("sesi harian") && lower.contains("belum") ->
                "Sesi harian belum dibuka admin. Checkout belum bisa dilakukan."
            lower.contains("bahan") && lower.contains("stok harian") ->
                "?? Bahan belum masuk stok harian. Hubungi admin untuk input bahan terlebih dahulu."
            lower.contains("stok harian") && (lower.contains("tidak cukup") || lower.contains("kurang")) ->
                "?? Stok bahan kurang. Kurangi jumlah pesanan atau hubungi admin untuk tambah stok."
            lower.contains("pembayaran kurang") || lower.contains("deficit") ->
                "Nominal pembayaran kurang. Silakan periksa kembali."
            lower.contains("tidak tersedia untuk dijual") || (lower.contains("variant") && lower.contains("tidak tersedia")) ->
                "?? Stok bahan kurang untuk salah satu menu. Kurangi jumlah pesanan atau hubungi admin untuk menambah stok."
            lower.contains("stok") && (lower.contains("tidak cukup") || lower.contains("kurang") || lower.contains("habis")) ->
                "?? Stok bahan kurang. Kurangi jumlah pesanan atau hubungi admin untuk tambah stok."
            lower.contains("resep") || lower.contains("recipe") ->
                "?? Bahan tidak mencukupi resep. Hubungi admin untuk menambah stok bahan."
            else -> message
        }
    }

    private fun PaymentMethod.isCashPaymentMethod(): Boolean {
        return name.equals("Cash", ignoreCase = true) ||
            name.equals("Tunai", ignoreCase = true)
    }
}
