package com.sipos.kebabsk.feature.menu.presentation

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.feature.auth.domain.model.AuthSession
import com.sipos.kebabsk.ui.theme.*

// Custom crossed fork-and-spoon icon (sendok garpu menyilang)
private val CrossedUtensils: ImageVector
    get() = ImageVector.Builder(
        name = "CrossedUtensils",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Fork (left-to-right diagonal)
        path(fill = SolidColor(Color.Black)) {
            // Handle
            moveTo(3f, 21f)
            lineTo(4.5f, 21f)
            lineTo(12.5f, 13f)
            lineTo(11f, 11.5f)
            close()
            // Prongs
            moveTo(9.5f, 3f)
            lineTo(8f, 3f)
            lineTo(8f, 8f)
            lineTo(9f, 9f)
            lineTo(11f, 9f)
            lineTo(12f, 8f)
            lineTo(12f, 3f)
            lineTo(10.5f, 3f)
            lineTo(10.5f, 7f)
            lineTo(9.5f, 7f)
            close()
        }
        // Knife (right-to-left diagonal)
        path(fill = SolidColor(Color.Black)) {
            // Blade
            moveTo(21f, 3f)
            lineTo(19.5f, 3f)
            lineTo(14f, 8.5f)
            lineTo(15.5f, 10f)
            lineTo(21f, 4.5f)
            close()
            // Handle
            moveTo(14f, 10.5f)
            lineTo(13f, 11.5f)
            lineTo(19.5f, 21f)
            lineTo(21f, 21f)
            lineTo(21f, 19.5f)
            lineTo(15.5f, 12f)
            lineTo(14.5f, 11f)
            close()
        }
    }.build()

enum class CashierPage {
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
    onDeleteVariant: (variantId: Long) -> Unit,
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
    val isCashier = remember(session.role) { session.role.equals("kasir", ignoreCase = true) }

    val quickAmounts = remember(totalAmount) { buildQuickAmounts(totalAmount) }
    val rawMenuItems = remember(uiState.menus) { buildMenuVariantItems(uiState.menus) }
    val menuItems = remember(rawMenuItems, isCashier) {
        if (isCashier) {
            // Sembunyikan semua variant yang tidak bisa dipesan:
            // - isAvailable = false (admin nonaktifkan)
            // - insufficientStock = true (bahan kurang dari resep)
            rawMenuItems.filter { it.isAvailable && !it.insufficientStock }
        } else {
            rawMenuItems
        }
    }
    val categories = remember(menuItems) { buildMenuCategories(menuItems) }
    val filteredMenuItems = remember(menuItems, uiState.selectedCategory) {
        filterMenuItems(menuItems, uiState.selectedCategory)
    }
    val emptyStateMessage = "Belum ada menu yang tersedia untuk dijual saat ini."

    var cashierPage by rememberSaveable { mutableStateOf(CashierPage.MENU) }

    if (cashierPage != CashierPage.MENU) {
        BackHandler { cashierPage = CashierPage.MENU }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KebabBg,
        topBar = {
            MenuTopBar(
                cashierPage = cashierPage,
                onSearch = { /* TODO: Search */ },
                onClose = { cashierPage = CashierPage.MENU }
            )
        },
        floatingActionButton = {
            if (cashierPage == CashierPage.MENU) {
                CartFloatingActionButton(
                    itemCount = uiState.cartItems.sumOf { it.qty },
                    onClick = { cashierPage = CashierPage.CART }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 0.dp) // padding content
        ) {

            // Removed CircularProgressIndicator, loading is handled below

            if (!uiState.errorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
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

            Box(modifier = Modifier.weight(1f)) {
                when (cashierPage) {
                    CashierPage.MENU -> {
                        if (uiState.isLoading) {
                            MenuListSkeletonTab()
                        } else {
                            MenuListTab(
                                menuItems = filteredMenuItems,
                                categories = categories,
                                selectedCategory = uiState.selectedCategory,
                                cartItems = uiState.cartItems,
                                emptyStateMessage = emptyStateMessage,
                                onCategorySelected = onCategorySelected,
                                onRefresh = onRefresh,
                                onAddVariant = onAddVariant,
                                onRemoveVariant = onRemoveVariant
                            )
                        }
                    }

                    CashierPage.CART -> {
                        Box(
                            modifier = Modifier.padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = 16.dp,
                                bottom = 0.dp
                            )
                        ) {
                            CartTab(
                                cartItems = uiState.cartItems,
                                totalAmount = totalAmount,
                                onAddVariant = onAddVariant,
                                onRemoveVariant = onRemoveVariant,
                                onDeleteVariant = onDeleteVariant,
                                onNavigateToPayment = { cashierPage = CashierPage.PAYMENT },
                                onBackToMenu = { cashierPage = CashierPage.MENU }
                            )
                        }
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
                            },
                            onBackToCart = { cashierPage = CashierPage.CART }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuTopBar(cashierPage: CashierPage, onSearch: () -> Unit, onClose: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (cashierPage == CashierPage.MENU) {
                Icon(Icons.Default.Menu, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Kebab SK",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KebabPrimary
                )
            } else {
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp).padding(0.dp)) {
                    Icon(CrossedUtensils, contentDescription = "Kebab SK", tint = KebabPrimary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Kebab SK",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    color = KebabPrimaryContainer
                )
            }
        }
        // Search icon dihilangkan sesuai permintaan
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartFloatingActionButton(itemCount: Int, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = KebabPrimary,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier.padding(end = 12.dp, bottom = 4.dp)
    ) {
        BadgedBox(
            badge = {
                if (itemCount > 0) {
                    Badge(
                        containerColor = KebabSecondaryContainer, // Kuning
                        contentColor = KebabTextDark,
                        modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                    ) {
                        Text(text = itemCount.toString(), fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Keranjang",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
