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
import com.sipos.kebabsk.feature.checkout.domain.validation.CheckoutValidationInput
import com.sipos.kebabsk.feature.checkout.domain.validation.CheckoutValidationResult
import com.sipos.kebabsk.feature.checkout.domain.validation.CheckoutValidator
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
    val paymentMethodsLoadCompleted: Boolean = false,
    val selectedPaymentMethodId: Long? = null,
    val paidAmountInput: String = "",
    val noteInput: String = "",
    val checkoutMessage: String? = null,
    val checkoutTransactionCode: String? = null,
    val checkoutBranchAddress: String? = null,
    val checkoutChangeAmount: Long? = null,
    val checkoutTotalAmount: Long? = null,
    val checkoutPaidAmount: Long? = null,
    val checkoutPaymentMethodName: String? = null,
    val qrisTransactionId: Long? = null,
    val qrisPayload: String? = null,
    val qrisMerchantName: String? = null,
    val qrisBranchName: String? = null,
    val qrisAmount: Long? = null,
    val qrisReference: String? = null,
    val qrisGeneratedAt: String? = null,
    val qrisExpiresAt: String? = null,
    val isGeneratingQris: Boolean = false,
    val isConfirmingQris: Boolean = false,
    val isQrisAwaitingConfirmation: Boolean = false,
    val qrisErrorMessage: String? = null,
    val checkoutReceiptItems: List<CartItem> = emptyList()
)

class CheckoutViewModel(
    private val checkoutRepository: CheckoutRepository,
    private val checkoutValidator: CheckoutValidator = CheckoutValidator()
) : ViewModel() {

    private val getPaymentMethodsUseCase = GetPaymentMethodsUseCase(checkoutRepository)
    private val createTransactionUseCase = CreateTransactionUseCase(checkoutRepository)

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val checkoutMutex = Mutex()
    private var latestQrisToken: String? = null

    fun loadPaymentMethods(token: String) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val paymentResult = getPaymentMethodsUseCase(token)
                paymentResult
                    .onSuccess { methods ->
                        val supportedMethods = methods
                            .filter { it.isCashPaymentMethod() || it.isQrisPaymentMethod() }
                            .sortedBy { if (it.isCashPaymentMethod()) 0 else 1 }
                        _uiState.update {
                            it.copy(
                                paymentMethods = supportedMethods,
                                paymentMethodsLoadCompleted = true,
                                selectedPaymentMethodId = supportedMethods.firstOrNull()?.id,
                                errorMessage = if (supportedMethods.isEmpty()) {
                                    "Metode pembayaran Tunai atau QRIS belum tersedia. Hubungi admin."
                                } else null
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                paymentMethods = emptyList(),
                                paymentMethodsLoadCompleted = true,
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
        _uiState.update {
            it.copy(
                selectedPaymentMethodId = paymentMethodId,
                paidAmountInput = "",
                errorMessage = null,
                qrisErrorMessage = null
            )
        }
    }

    fun onQuickAmountSelected(amount: Long) {
        _uiState.update {
            it.copy(
                paidAmountInput = amount.toString(),
                errorMessage = null,
                checkoutMessage = null,
                checkoutTransactionCode = null,
                checkoutBranchAddress = null,
                checkoutChangeAmount = null,
                checkoutTotalAmount = null,
                checkoutPaidAmount = null,
                checkoutPaymentMethodName = null,
                qrisTransactionId = null,
                qrisPayload = null,
                qrisMerchantName = null,
                qrisBranchName = null,
                qrisAmount = null,
                qrisReference = null,
                qrisGeneratedAt = null,
                qrisExpiresAt = null,
                isGeneratingQris = false,
                isConfirmingQris = false,
                isQrisAwaitingConfirmation = false,
                qrisErrorMessage = null,
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
                checkoutBranchAddress = null,
                checkoutChangeAmount = null,
                checkoutTotalAmount = null,
                checkoutPaidAmount = null,
                checkoutPaymentMethodName = null,
                qrisTransactionId = null,
                qrisPayload = null,
                qrisMerchantName = null,
                qrisBranchName = null,
                qrisAmount = null,
                qrisReference = null,
                qrisGeneratedAt = null,
                qrisExpiresAt = null,
                isGeneratingQris = false,
                isConfirmingQris = false,
                isQrisAwaitingConfirmation = false,
                qrisErrorMessage = null,
                checkoutReceiptItems = emptyList()
            )
        }
    }

    fun onNoteChanged(value: String) {
        _uiState.update { it.copy(noteInput = value) }
    }

    fun submitCheckout(
        token: String,
        cartItems: List<CartItem>,
        isDailySessionOpen: Boolean,
        isDailySessionStatusKnown: Boolean = true,
        onSuccess: () -> Unit = {}
    ) {
        if (_uiState.value.isSubmitting) return
        val cartSnapshot = cartItems.map { it.copy() }
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                checkoutMutex.withLock {
                    val state = _uiState.value
                    val selectedMethod = state.paymentMethods
                        .firstOrNull { it.id == state.selectedPaymentMethodId }
                    val isQrisPayment = selectedMethod?.isQrisPaymentMethod() == true
                    val validation = checkoutValidator.validate(
                        CheckoutValidationInput(
                            cartItems = cartSnapshot,
                            isDailySessionOpen = isDailySessionOpen,
                            isDailySessionStatusKnown = isDailySessionStatusKnown,
                            paymentMethodId = state.selectedPaymentMethodId,
                            paidAmountInput = state.paidAmountInput,
                            requiresCashAmount = !isQrisPayment
                        )
                    )
                    if (validation is CheckoutValidationResult.Invalid) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = validation.message
                            )
                        }
                        return@withLock
                    }
                    val validInput = (validation as CheckoutValidationResult.Valid).value

                    _uiState.update {
                        it.copy(
                            isLoading = true,
                            errorMessage = null,
                            checkoutMessage = null,
                            checkoutTransactionCode = null,
                            checkoutBranchAddress = null,
                            checkoutChangeAmount = null,
                            checkoutTotalAmount = null,
                            checkoutPaidAmount = null,
                            checkoutPaymentMethodName = null,
                            qrisTransactionId = null,
                            qrisPayload = null,
                            qrisMerchantName = null,
                            qrisBranchName = null,
                            qrisAmount = null,
                            qrisReference = null,
                            qrisGeneratedAt = null,
                            qrisExpiresAt = null,
                            isGeneratingQris = false,
                            isConfirmingQris = false,
                            isQrisAwaitingConfirmation = false,
                            qrisErrorMessage = null,
                            checkoutReceiptItems = emptyList()
                        )
                    }
                    val request = CheckoutRequestData(
                        paymentMethodId = validInput.paymentMethodId,
                        paidAmount = validInput.paidAmount,
                        items = cartSnapshot.map { CheckoutItemInput(it.variantId, it.quantity) },
                        note = state.noteInput.trim().takeIf { it.isNotBlank() }
                    )

                    createTransactionUseCase(token, request)
                        .onSuccess { result ->
                            val expectedStatus = if (isQrisPayment) "PENDING_PAYMENT" else "SUCCESS"
                            if (!result.status.equals(expectedStatus, ignoreCase = true)) {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = "Status transaksi dari server tidak valid. Muat ulang data sebelum mencoba lagi."
                                    )
                                }
                                return@onSuccess
                            }
                            setCheckoutResult(
                                result = result,
                                paymentMethodName = selectedMethod?.name.orEmpty(),
                                cartSnapshot = cartSnapshot,
                                isQrisPayment = isQrisPayment
                            )

                            if (isQrisPayment) {
                                latestQrisToken = token
                                generateQrisForTransaction(token, result.transactionId)
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
                                    checkoutBranchAddress = null,
                                    checkoutChangeAmount = null,
                                    checkoutTotalAmount = null,
                                    checkoutPaidAmount = null,
                                    checkoutPaymentMethodName = null,
                                    qrisTransactionId = null,
                                    qrisPayload = null,
                                    qrisMerchantName = null,
                                    qrisBranchName = null,
                                    qrisAmount = null,
                                    qrisReference = null,
                                    qrisGeneratedAt = null,
                                    qrisExpiresAt = null,
                                    isGeneratingQris = false,
                                    isConfirmingQris = false,
                                    isQrisAwaitingConfirmation = false,
                                    qrisErrorMessage = null,
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
                checkoutBranchAddress = null,
                checkoutChangeAmount = null,
                checkoutTotalAmount = null,
                checkoutPaidAmount = null,
                checkoutPaymentMethodName = null,
                qrisTransactionId = null,
                qrisPayload = null,
                qrisMerchantName = null,
                qrisBranchName = null,
                qrisAmount = null,
                qrisReference = null,
                qrisGeneratedAt = null,
                qrisExpiresAt = null,
                isGeneratingQris = false,
                isConfirmingQris = false,
                isQrisAwaitingConfirmation = false,
                qrisErrorMessage = null,
                checkoutReceiptItems = emptyList()
            )
        }
    }

    fun clearCheckoutMessage() {
        _uiState.update { it.copy(checkoutMessage = null) }
    }

    fun clear() {
        latestQrisToken = null
        _uiState.value = CheckoutUiState()
    }

    fun retryGenerateQris() {
        val token = latestQrisToken ?: return
        val transactionId = _uiState.value.qrisTransactionId ?: return
        if (_uiState.value.isGeneratingQris) return

        viewModelScope.launch {
            generateQrisForTransaction(token, transactionId)
        }
    }

    fun confirmQrisPayment() {
        val token = latestQrisToken ?: return
        val state = _uiState.value
        val transactionId = state.qrisTransactionId ?: return
        val reference = state.qrisReference?.takeIf { it.isNotBlank() } ?: return
        if (state.qrisPayload.isNullOrBlank() || state.isConfirmingQris) return

        _uiState.update { it.copy(isConfirmingQris = true, qrisErrorMessage = null) }
        viewModelScope.launch {
            checkoutRepository.confirmQris(token, transactionId, reference)
                .onSuccess { confirmation ->
                    val expectedAmount = _uiState.value.checkoutTotalAmount
                    if (!confirmation.status.equals("SUCCESS", ignoreCase = true) ||
                        confirmation.transactionId != transactionId ||
                        confirmation.reference != reference ||
                        confirmation.amount != expectedAmount
                    ) {
                        _uiState.update {
                            it.copy(
                                isConfirmingQris = false,
                                qrisErrorMessage = "Konfirmasi QRIS dari server tidak sesuai dengan transaksi."
                            )
                        }
                        return@onSuccess
                    }
                    _uiState.update {
                        it.copy(
                            isConfirmingQris = false,
                            isQrisAwaitingConfirmation = false,
                            checkoutPaidAmount = confirmation.amount,
                            checkoutChangeAmount = 0,
                            checkoutMessage = "Pembayaran QRIS terkonfirmasi: ",
                            qrisErrorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isConfirmingQris = false,
                            qrisErrorMessage = normalizeQrisError(error.message)
                        )
                    }
                }
        }
    }

    private fun setCheckoutResult(
        result: com.sipos.kebabsk.feature.checkout.domain.model.CheckoutResult,
        paymentMethodName: String,
        cartSnapshot: List<CartItem>,
        isQrisPayment: Boolean
    ) {
        _uiState.update {
            it.copy(
                isLoading = false,
                paidAmountInput = "",
                noteInput = "",
                checkoutMessage = if (isQrisPayment) null else "Pembayaran berhasil: ",
                checkoutTransactionCode = result.transactionCode,
                checkoutBranchAddress = result.branchAddress,
                checkoutChangeAmount = result.changeAmount,
                checkoutTotalAmount = result.totalAmount,
                checkoutPaidAmount = result.paidAmount,
                checkoutPaymentMethodName = paymentMethodName,
                qrisTransactionId = if (isQrisPayment) result.transactionId else null,
                qrisPayload = null,
                qrisMerchantName = null,
                qrisBranchName = null,
                qrisAmount = if (isQrisPayment) result.totalAmount else null,
                qrisReference = null,
                qrisGeneratedAt = null,
                qrisExpiresAt = null,
                isGeneratingQris = isQrisPayment,
                isConfirmingQris = false,
                isQrisAwaitingConfirmation = isQrisPayment,
                qrisErrorMessage = null,
                checkoutReceiptItems = cartSnapshot,
                errorMessage = null
            )
        }
    }

    private suspend fun generateQrisForTransaction(token: String, transactionId: Long) {
        _uiState.update {
            it.copy(
                isGeneratingQris = true,
                qrisPayload = null,
                qrisReference = null,
                qrisGeneratedAt = null,
                qrisExpiresAt = null,
                qrisErrorMessage = null
            )
        }

        checkoutRepository.generateQris(token, transactionId)
            .onSuccess { qris ->
                val expectedAmount = _uiState.value.checkoutTotalAmount
                if (qris.transactionId != transactionId || qris.amount != expectedAmount) {
                    _uiState.update {
                        it.copy(
                            isGeneratingQris = false,
                            qrisErrorMessage = "Nominal QRIS dari server tidak sesuai dengan transaksi."
                        )
                    }
                    return@onSuccess
                }
                _uiState.update {
                    it.copy(
                        qrisPayload = qris.payload,
                        qrisMerchantName = qris.merchantName,
                        qrisBranchName = qris.branchName,
                        qrisAmount = qris.amount,
                        qrisReference = qris.reference,
                        qrisGeneratedAt = qris.generatedAt,
                        qrisExpiresAt = qris.expiresAt,
                        isGeneratingQris = false,
                        qrisErrorMessage = null
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        isGeneratingQris = false,
                        qrisErrorMessage = normalizeQrisError(error.message)
                    )
                }
            }
    }

    private fun normalizeQrisError(rawMessage: String?): String {
        val message = sanitizeUserMessage(rawMessage, "QRIS belum dapat dibuat. Silakan coba lagi.")
        return when {
            message.contains("belum dikonfigurasi", ignoreCase = true) ->
                "QRIS belum dikonfigurasi untuk cabang ini. Hubungi admin."
            message.contains("tidak valid", ignoreCase = true) ->
                "Konfigurasi QRIS cabang tidak valid. Hubungi admin."
            else -> message
        }
    }

    private fun normalizeCheckoutError(rawMessage: String?): String {
        val fallback = "Pembayaran belum berhasil. Silakan coba lagi."
        val message = sanitizeUserMessage(rawMessage, fallback)
        val lower = message.lowercase()

        return when {
            lower.contains("sesi harian") && lower.contains("belum") ->
                "Sesi harian belum dibuka admin. Checkout belum bisa dilakukan."
            lower.contains("bahan") && lower.contains("stok harian") ->
                "Bahan belum masuk stok harian. Hubungi admin untuk input bahan terlebih dahulu."
            lower.contains("stok harian") && (lower.contains("tidak cukup") || lower.contains("kurang")) ->
                "Stok bahan kurang. Kurangi jumlah pesanan atau hubungi admin untuk tambah stok."
            lower.contains("pembayaran kurang") || lower.contains("deficit") ->
                "Nominal pembayaran kurang. Silakan periksa kembali."
            lower.contains("tidak tersedia untuk dijual") || (lower.contains("variant") && lower.contains("tidak tersedia")) ->
                "Stok bahan kurang untuk salah satu menu. Kurangi jumlah pesanan atau hubungi admin untuk menambah stok."
            lower.contains("stok") && (lower.contains("tidak cukup") || lower.contains("kurang") || lower.contains("habis")) ->
                "Stok bahan kurang. Kurangi jumlah pesanan atau hubungi admin untuk tambah stok."
            lower.contains("resep") || lower.contains("recipe") ->
                "Bahan tidak mencukupi resep. Hubungi admin untuk menambah stok bahan."
            else -> message
        }
    }

    private fun PaymentMethod.isCashPaymentMethod(): Boolean {
        return name.equals("Cash", ignoreCase = true) ||
            name.equals("Tunai", ignoreCase = true)
    }

    private fun PaymentMethod.isQrisPaymentMethod(): Boolean {
        return name.equals("QRIS", ignoreCase = true)
    }
}
