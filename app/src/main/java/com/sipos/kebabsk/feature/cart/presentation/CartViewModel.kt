package com.sipos.kebabsk.feature.cart.presentation

import androidx.lifecycle.ViewModel
import com.sipos.kebabsk.feature.cart.domain.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartUiState(
    val cartItems: List<CartItem> = emptyList()
) {
    val totalAmount: Long
        get() = cartItems.sumOf { it.subtotal }
}

class CartViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    fun addVariantToCart(menuName: String, variantId: Long, variantName: String, price: Long) {
        _uiState.update { state ->
            val existing = state.cartItems.firstOrNull { it.variantId == variantId }
            val updated = if (existing == null) {
                state.cartItems + CartItem(
                    variantId = variantId,
                    menuName = menuName,
                    variantName = variantName,
                    quantity = 1,
                    unitPrice = price
                )
            } else {
                state.cartItems.map {
                    if (it.variantId == variantId) it.copy(quantity = it.quantity + 1) else it
                }
            }
            state.copy(cartItems = updated)
        }
    }

    fun removeFromCart(variantId: Long) {
        _uiState.update { state ->
            val updated = state.cartItems.mapNotNull {
                if (it.variantId == variantId) {
                    val newQty = it.quantity - 1
                    if (newQty <= 0) null else it.copy(quantity = newQty)
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

    fun clearCart() {
        _uiState.update { it.copy(cartItems = emptyList()) }
    }
}
