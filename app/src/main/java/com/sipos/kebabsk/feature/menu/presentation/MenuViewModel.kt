package com.sipos.kebabsk.feature.menu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sipos.kebabsk.common.sanitizeUserMessage
import com.sipos.kebabsk.data.network.NetworkModule
import com.sipos.kebabsk.feature.checkout.data.repository.CheckoutRepositoryImpl
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutCartItem
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutItemInput
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutRequestData
import com.sipos.kebabsk.feature.checkout.domain.model.PaymentMethod
import com.sipos.kebabsk.feature.checkout.domain.usecase.CreateTransactionUseCase
import com.sipos.kebabsk.feature.checkout.domain.usecase.GetPaymentMethodsUseCase
import com.sipos.kebabsk.feature.menu.data.repository.MenuRepositoryImpl
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuItem
import com.sipos.kebabsk.feature.menu.domain.usecase.GetMenusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class MenuUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cashierName: String = "",
    val cashierRole: String? = null,
    val isDailySessionOpen: Boolean = false,
    val dailySessionStatusLabel: String? = null,
    val dailyTargetRevenue: Double? = null,
    val dailyStockItems: List<DailyStockItem> = emptyList(),
    val menus: List<MenuItem> = emptyList(),
    val selectedCategory: String? = null,
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethodId: Long? = null,
    val paidAmountInput: String = "",
    val noteInput: String = "",
    val cartItems: List<CheckoutCartItem> = emptyList(),
    val checkoutMessage: String? = null,
    val checkoutTransactionCode: String? = null,
    val checkoutChangeAmount: Double? = null,
    val checkoutTotalAmount: Double? = null,
    val checkoutPaidAmount: Double? = null,
    val checkoutReceiptItems: List<CheckoutCartItem> = emptyList()
)

class MenuViewModel(
    private val getMenusUseCase: GetMenusUseCase = GetMenusUseCase(MenuRepositoryImpl(NetworkModule.menuApiService)),
    private val getPaymentMethodsUseCase: GetPaymentMethodsUseCase = GetPaymentMethodsUseCase(CheckoutRepositoryImpl(NetworkModule.checkoutApiService)),
    private val createTransactionUseCase: CreateTransactionUseCase = CreateTransactionUseCase(CheckoutRepositoryImpl(NetworkModule.checkoutApiService))
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private var loadedToken: String? = null
    private val menuLoadMutex = Mutex()
    private val checkoutMutex = Mutex()

    fun loadMenus(token: String, forceRefresh: Boolean = false) {
        if (_uiState.value.isLoading) return
        if (!forceRefresh && loadedToken == token && _uiState.value.menus.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            menuLoadMutex.withLock {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null
                    )
                }

                val menuResult = getMenusUseCase(token)

                var menuLoaded = false
                menuResult
                    .onSuccess { payload ->
                        loadedToken = token
                        menuLoaded = true
                        _uiState.update {
                            it.copy(
                                menus = payload.menus,
                                cashierName = payload.user.name,
                                cashierRole = payload.user.role,
                                isDailySessionOpen = payload.dailySession.isOpen,
                                dailySessionStatusLabel = payload.dailySession.label,
                                dailyTargetRevenue = payload.dailySession.targetRevenue,
                                dailyStockItems = payload.dailyStockItems
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update {
                            it.copy(
                                errorMessage = sanitizeUserMessage(
                                    error.message,
                                    "Menu belum bisa dimuat. Silakan coba lagi."
                                )
                            )
                        }
                    }

                if (menuLoaded) {
                    val paymentResult = getPaymentMethodsUseCase(token)
                    paymentResult
                        .onSuccess { methods ->
                            val cashMethods = methods.filter { it.isCashPaymentMethod() }
                            _uiState.update {
                                it.copy(
                                    paymentMethods = cashMethods,
                                    selectedPaymentMethodId = cashMethods.firstOrNull()?.id,
                                    errorMessage = if (cashMethods.isEmpty()) {
                                        "Metode pembayaran tunai belum tersedia. Hubungi admin untuk pengecekan."
                                    } else {
                                        it.errorMessage
                                    }
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
                }

                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    fun prefetchMenusIfNeeded(token: String) {
        if (_uiState.value.menus.isNotEmpty() || _uiState.value.isLoading) return
        loadMenus(token, forceRefresh = false)
    }

    fun forceRefreshMenus(token: String) {
        loadMenus(token, forceRefresh = true)
    }

    fun addVariantToCart(menuName: String, variantId: Long, variantName: String, price: Double) {
        _uiState.update { state ->
            val existing = state.cartItems.firstOrNull { it.variantId == variantId }
            val updated = if (existing == null) {
                state.cartItems + CheckoutCartItem(
                    variantId = variantId,
                    menuName = menuName,
                    variantName = variantName,
                    qty = 1,
                    price = price
                )
            } else {
                state.cartItems.map {
                    if (it.variantId == variantId) it.copy(qty = it.qty + 1) else it
                }
            }
            state.copy(
                cartItems = updated,
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

    fun removeFromCart(variantId: Long) {
        _uiState.update { state ->
            val updated = state.cartItems.mapNotNull {
                if (it.variantId == variantId) {
                    val newQty = it.qty - 1
                    if (newQty <= 0) null else it.copy(qty = newQty)
                } else it
            }
            state.copy(cartItems = updated)
        }
    }

    fun deleteFromCart(variantId: Long) {
        _uiState.update { state ->
            state.copy(cartItems = state.cartItems.filter { it.variantId != variantId })
        }
    }

    fun onCategorySelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onPaymentMethodSelected(paymentMethodId: Long) {
        _uiState.update { it.copy(selectedPaymentMethodId = paymentMethodId) }
    }

    fun onQuickAmountSelected(amount: Int) {
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
                paidAmountInput = sanitizeMoneyInput(value),
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

    fun submitCheckout(token: String) {
        viewModelScope.launch {
            checkoutMutex.withLock {
                if (_uiState.value.isLoading) {
                    // Double guard: avoid duplicate submit while request is in flight.
                    return@withLock
                }

                val state = _uiState.value
                if (state.cartItems.isEmpty()) {
                    _uiState.update { it.copy(errorMessage = "Keranjang masih kosong") }
                    return@withLock
                }

                if (!state.isDailySessionOpen) {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Sesi harian belum dibuka admin. Checkout belum bisa dilakukan."
                        )
                    }
                    return@withLock
                }

                val paymentMethodId = state.selectedPaymentMethodId
                if (paymentMethodId == null) {
                    _uiState.update { it.copy(errorMessage = "Metode pembayaran tunai belum tersedia") }
                    return@withLock
                }

                val paidAmount = sanitizeMoneyInput(state.paidAmountInput).toDoubleOrNull()
                if (paidAmount == null || paidAmount <= 0.0) {
                    _uiState.update { it.copy(errorMessage = "Nominal bayar tidak valid") }
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
                    items = state.cartItems.map { CheckoutItemInput(it.variantId, it.qty) },
                    note = state.noteInput.takeIf { it.isNotBlank() }
                )

                createTransactionUseCase(token, request)
                    .onSuccess { result ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                cartItems = emptyList(),
                                paidAmountInput = "",
                                noteInput = "",
                                checkoutMessage = "Pembayaran berhasil: ${result.transactionCode}",
                                checkoutTransactionCode = result.transactionCode,
                                checkoutChangeAmount = result.changeAmount,
                                checkoutTotalAmount = result.totalAmount,
                                checkoutPaidAmount = result.paidAmount,
                                checkoutReceiptItems = state.cartItems,
                                errorMessage = null
                            )
                        }
                        // REFRESH DATA (Tarik stok terbaru setelah checkout berhasil)
                        loadMenus(token, forceRefresh = true)
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
        loadedToken = null
        _uiState.value = MenuUiState()
    }

    private fun normalizeCheckoutError(rawMessage: String?): String {
        val fallback = "Pembayaran belum berhasil. Silakan coba lagi."
        val message = sanitizeUserMessage(rawMessage, fallback)
        val lower = message.lowercase()

        return when {
            lower.contains("sesi harian") && lower.contains("belum") ->
                "Sesi harian belum dibuka admin. Checkout belum bisa dilakukan."
            lower.contains("bahan") && lower.contains("stok harian") ->
                "⚠️ Bahan belum masuk stok harian. Hubungi admin untuk input bahan terlebih dahulu."
            lower.contains("stok harian") && (lower.contains("tidak cukup") || lower.contains("kurang")) ->
                "⚠️ Stok bahan kurang. Kurangi jumlah pesanan atau hubungi admin untuk tambah stok."
            lower.contains("pembayaran kurang") || lower.contains("deficit") ->
                "Nominal pembayaran kurang. Silakan periksa kembali."
            // Tangkap pesan backend: "Variant 'xxx' tidak tersedia untuk dijual"
            // artinya stok bahan tidak cukup untuk resep varian tersebut
            lower.contains("tidak tersedia untuk dijual") || lower.contains("variant") && lower.contains("tidak tersedia") ->
                "⚠️ Stok bahan kurang untuk salah satu menu. Kurangi jumlah pesanan atau hubungi admin untuk menambah stok."
            // Tangkap pola lain dari backend yang berkaitan dengan stok/resep
            lower.contains("stok") && (lower.contains("tidak cukup") || lower.contains("kurang") || lower.contains("habis")) ->
                "⚠️ Stok bahan kurang. Kurangi jumlah pesanan atau hubungi admin untuk tambah stok."
            lower.contains("resep") || lower.contains("recipe") ->
                "⚠️ Bahan tidak mencukupi resep. Hubungi admin untuk menambah stok bahan."
            else -> message
        }
    }

    private fun sanitizeMoneyInput(value: String): String {
        return value.filter { it.isDigit() }
    }

    private fun PaymentMethod.isCashPaymentMethod(): Boolean {
        return name.equals("Cash", ignoreCase = true) ||
            name.equals("Tunai", ignoreCase = true)
    }
}
