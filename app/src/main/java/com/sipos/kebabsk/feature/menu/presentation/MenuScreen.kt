package com.sipos.kebabsk.feature.menu.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sipos.kebabsk.feature.auth.domain.model.AuthSession
import com.sipos.kebabsk.ui.theme.BrandAmber
import com.sipos.kebabsk.ui.theme.BrandOrange

private enum class CashierPage {
    MENU,
    CART,
    PAYMENT
}

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    session: AuthSession,
    uiState: MenuUiState,
    onRefresh: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onAddVariant: (menuName: String, variantId: Long, variantName: String, price: Double) -> Unit,
    onRemoveVariant: (variantId: Long) -> Unit,
    onPaymentMethodSelected: (paymentMethodId: Long) -> Unit,
    onQuickAmountSelected: (amount: Int) -> Unit,
    onPaidAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmitCheckout: () -> Unit,
    onDismissCheckoutPreview: () -> Unit
) {
    val totalAmount = remember(uiState.cartItems) {
        uiState.cartItems.sumOf { it.price * it.qty }
    }
    val exactAmount = totalAmount.toInt()

    val quickAmounts = remember(totalAmount) { buildQuickAmounts(totalAmount) }
    val menuItems = remember(uiState.menus) { buildMenuVariantItems(uiState.menus) }
    val categories = remember(uiState.menus) { buildMenuCategories(uiState.menus) }
    val filteredMenuItems = remember(menuItems, uiState.selectedCategory) {
        filterMenuItems(menuItems, uiState.selectedCategory)
    }

    var cashierPage by rememberSaveable { mutableStateOf(CashierPage.MENU) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        CashierInfoHeader(
            displayName = if (uiState.cashierName.isBlank()) session.displayName else uiState.cashierName,
            role = uiState.cashierRole ?: "kasir"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab row
        CashierTopTabs(
            selectedPage = cashierPage,
            cartItemCount = uiState.cartItems.sumOf { it.qty },
            onTabSelected = { cashierPage = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandOrange, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!uiState.errorMessage.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        if (!uiState.checkoutMessage.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.checkoutMessage,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        when (cashierPage) {
            CashierPage.MENU -> {
                MenuListTab(
                    menuItems = filteredMenuItems,
                    categories = categories,
                    selectedCategory = uiState.selectedCategory,
                    cartItems = uiState.cartItems,
                    onCategorySelected = onCategorySelected,
                    onRefresh = onRefresh,
                    onAddVariant = onAddVariant,
                    onRemoveVariant = onRemoveVariant
                )
            }

            CashierPage.CART -> {
                CartTab(
                    cartItems = uiState.cartItems,
                    totalAmount = totalAmount,
                    onRemoveVariant = onRemoveVariant,
                    onNavigateToPayment = { cashierPage = CashierPage.PAYMENT }
                )
            }

            CashierPage.PAYMENT -> {
                PaymentTab(
                    uiState = uiState,
                    totalAmount = totalAmount,
                    exactAmount = exactAmount,
                    quickAmounts = quickAmounts,
                    onPaymentMethodSelected = onPaymentMethodSelected,
                    onQuickAmountSelected = onQuickAmountSelected,
                    onPaidAmountChanged = onPaidAmountChanged,
                    onNoteChanged = onNoteChanged,
                    onSubmitCheckout = onSubmitCheckout,
                    onDismissCheckoutPreview = {
                        onDismissCheckoutPreview()
                        cashierPage = CashierPage.MENU
                    }
                )
            }
        }
    }
}

@Composable
private fun CashierInfoHeader(displayName: String, role: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BrandOrange.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = BrandOrange,
                modifier = Modifier.size(22.dp)
            )
        }
        Column {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = role.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CashierTopTabs(
    selectedPage: CashierPage,
    cartItemCount: Int,
    onTabSelected: (CashierPage) -> Unit
) {
    val tabs = listOf(CashierPage.MENU to "Menu", CashierPage.CART to "Keranjang")

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            tabs.forEach { (page, label) ->
                val isSelected = selectedPage == page
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) BrandOrange else Color.Transparent,
                    label = "tabBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "tabText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (page == CashierPage.CART && cartItemCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = BrandAmber,
                                    contentColor = Color(0xFF3E2723),
                                    modifier = Modifier.offset(x = 8.dp, y = (-4).dp)
                                ) {
                                    Text(cartItemCount.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = textColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = textColor)
                            }
                        }
                    } else {
                        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = textColor)
                    }

                    // Invisible click surface
                    Surface(
                        onClick = { onTabSelected(page) },
                        modifier = Modifier.matchParentSize(),
                        color = Color.Transparent
                    ) {}
                }
            }
        }
    }
}
