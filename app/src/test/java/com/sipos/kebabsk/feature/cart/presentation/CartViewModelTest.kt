package com.sipos.kebabsk.feature.cart.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CartViewModelTest {

    @Test
    fun addVariantToCart_firstItem_addsItem() {
        val viewModel = CartViewModel()

        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)

        val item = viewModel.uiState.value.cartItems.single()
        assertEquals(1L, item.variantId)
        assertEquals("Kebab", item.menuName)
        assertEquals("Mini", item.variantName)
        assertEquals(1, item.quantity)
        assertEquals(10_000L, item.unitPrice)
    }

    @Test
    fun addVariantToCart_sameVariant_incrementsQuantity() {
        val viewModel = CartViewModel()

        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)
        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)

        assertEquals(1, viewModel.uiState.value.cartItems.size)
        assertEquals(2, viewModel.uiState.value.cartItems.single().quantity)
    }

    @Test
    fun addVariantToCart_differentVariant_keepsSeparateItems() {
        val viewModel = CartViewModel()

        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)
        viewModel.addVariantToCart("Kebab", 2L, "Jumbo", 15_000L)

        assertEquals(listOf(1L, 2L), viewModel.uiState.value.cartItems.map { it.variantId })
    }

    @Test
    fun addVariantToCart_sameNameDifferentVariantId_doesNotMergeItems() {
        val viewModel = CartViewModel()

        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)
        viewModel.addVariantToCart("Kebab", 99L, "Mini", 10_000L)

        assertEquals(2, viewModel.uiState.value.cartItems.size)
        assertEquals(listOf(1, 1), viewModel.uiState.value.cartItems.map { it.quantity })
    }

    @Test
    fun removeFromCart_decrementsQuantity() {
        val viewModel = CartViewModel()

        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)
        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)
        viewModel.removeFromCart(1L)

        assertEquals(1, viewModel.uiState.value.cartItems.single().quantity)
    }

    @Test
    fun removeFromCart_whenQuantityOne_removesItem() {
        val viewModel = CartViewModel()

        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)
        viewModel.removeFromCart(1L)

        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
    }

    @Test
    fun deleteFromCart_removesItemImmediately() {
        val viewModel = CartViewModel()

        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)
        viewModel.addVariantToCart("Burger", 2L, "Single", 12_000L)
        viewModel.deleteFromCart(1L)

        assertEquals(listOf(2L), viewModel.uiState.value.cartItems.map { it.variantId })
    }

    @Test
    fun clearCart_removesAllItems() {
        val viewModel = CartViewModel()

        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)
        viewModel.addVariantToCart("Burger", 2L, "Single", 12_000L)
        viewModel.clearCart()

        assertTrue(viewModel.uiState.value.cartItems.isEmpty())
    }

    @Test
    fun subtotal_usesLongAndCorrectValue() {
        val viewModel = CartViewModel()

        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)
        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)

        val item = viewModel.uiState.value.cartItems.single()
        val subtotal: Long = item.subtotal
        assertEquals(20_000L, subtotal)
    }

    @Test
    fun totalAmount_usesLongAndSumsSubtotals() {
        val viewModel = CartViewModel()

        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)
        viewModel.addVariantToCart("Burger", 2L, "Single", 12_000L)
        viewModel.addVariantToCart("Burger", 2L, "Single", 12_000L)

        val totalAmount: Long = viewModel.uiState.value.totalAmount
        assertEquals(34_000L, totalAmount)
    }

    @Test
    fun itemOrder_isStableByInsertionOrder() {
        val viewModel = CartViewModel()

        viewModel.addVariantToCart("Kebab", 1L, "Mini", 10_000L)
        viewModel.addVariantToCart("Burger", 2L, "Single", 12_000L)
        viewModel.addVariantToCart("Add on", 3L, "Sosis", 2_500L)

        assertEquals(listOf(1L, 2L, 3L), viewModel.uiState.value.cartItems.map { it.variantId })
    }
}
