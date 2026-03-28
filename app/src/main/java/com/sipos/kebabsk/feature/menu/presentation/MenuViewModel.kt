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
import com.sipos.kebabsk.feature.menu.domain.model.MenuItem
import com.sipos.kebabsk.feature.menu.domain.usecase.GetMenusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MenuUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cashierName: String = "",
    val cashierRole: String? = null,
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
    val checkoutPaidAmount: Double? = null
)

class MenuViewModel : ViewModel() {
    private val getMenusUseCase = GetMenusUseCase(MenuRepositoryImpl(NetworkModule.menuApiService))
    private val getPaymentMethodsUseCase = GetPaymentMethodsUseCase(CheckoutRepositoryImpl(NetworkModule.checkoutApiService))
    private val createTransactionUseCase = CreateTransactionUseCase(CheckoutRepositoryImpl(NetworkModule.checkoutApiService))

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private var loadedToken: String? = null

    fun loadMenus(token: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && loadedToken == token && _uiState.value.menus.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    checkoutMessage = null,
                    checkoutTransactionCode = null,
                    checkoutChangeAmount = null,
                    checkoutTotalAmount = null,
                    checkoutPaidAmount = null
                )
            }

            val menuResult = getMenusUseCase(token)
            val paymentResult = getPaymentMethodsUseCase(token)

            menuResult
                .onSuccess { payload ->
                    loadedToken = token
                    _uiState.update {
                        it.copy(
                            menus = payload.menus,
                            cashierName = payload.user.name,
                            cashierRole = payload.user.role
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

            paymentResult
                .onSuccess { methods ->
                    val finalMethods = if (methods.isEmpty()) fallbackPaymentMethods() else methods
                    _uiState.update {
                        it.copy(
                            paymentMethods = finalMethods,
                            selectedPaymentMethodId = it.selectedPaymentMethodId ?: finalMethods.firstOrNull()?.id,
                            errorMessage = if (methods.isEmpty()) {
                                "Metode pembayaran sementara menggunakan Cash."
                            } else {
                                it.errorMessage
                            }
                        )
                    }
                }
                .onFailure { error ->
                    val finalMethods = fallbackPaymentMethods()
                    _uiState.update {
                        it.copy(
                            paymentMethods = finalMethods,
                            selectedPaymentMethodId = it.selectedPaymentMethodId ?: finalMethods.firstOrNull()?.id,
                            errorMessage = sanitizeUserMessage(
                                error.message,
                                "Metode pembayaran belum tersedia. Sementara menggunakan Cash."
                            )
                        )
                    }
                }

            _uiState.update { it.copy(isLoading = false) }
        }
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
                checkoutPaidAmount = null
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
                checkoutPaidAmount = null
            )
        }
    }

    fun onPaidAmountChanged(value: String) {
        _uiState.update {
            it.copy(
                paidAmountInput = value.filter { char -> char.isDigit() || char == '.' },
                errorMessage = null,
                checkoutMessage = null,
                checkoutTransactionCode = null,
                checkoutChangeAmount = null,
                checkoutTotalAmount = null,
                checkoutPaidAmount = null
            )
        }
    }

    fun onNoteChanged(value: String) {
        _uiState.update { it.copy(noteInput = value) }
    }

    fun submitCheckout(token: String) {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Keranjang masih kosong") }
            return
        }

        val paymentMethodId = state.selectedPaymentMethodId
        if (paymentMethodId == null) {
            _uiState.update { it.copy(errorMessage = "Pilih metode pembayaran") }
            return
        }

        val paidAmount = state.paidAmountInput.toDoubleOrNull()
        if (paidAmount == null || paidAmount <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Nominal bayar tidak valid") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    checkoutMessage = null,
                    checkoutTransactionCode = null,
                    checkoutChangeAmount = null,
                    checkoutTotalAmount = null,
                    checkoutPaidAmount = null
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
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = sanitizeUserMessage(
                                error.message,
                                "Pembayaran belum berhasil. Silakan coba lagi."
                            ),
                            checkoutMessage = null,
                            checkoutTransactionCode = null,
                            checkoutChangeAmount = null,
                            checkoutTotalAmount = null,
                            checkoutPaidAmount = null
                        )
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
                checkoutPaidAmount = null
            )
        }
    }

    fun clear() {
        loadedToken = null
        _uiState.value = MenuUiState()
    }

    private fun fallbackPaymentMethods(): List<PaymentMethod> {
        return listOf(PaymentMethod(id = 1L, name = "Cash"))
    }
}
