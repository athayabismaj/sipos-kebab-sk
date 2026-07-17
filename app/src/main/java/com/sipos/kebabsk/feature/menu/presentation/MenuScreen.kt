package com.sipos.kebabsk.feature.menu.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.R
import com.sipos.kebabsk.feature.auth.domain.model.AuthSession
import com.sipos.kebabsk.ui.theme.*
import com.sipos.kebabsk.feature.cart.presentation.CartUiState
import com.sipos.kebabsk.feature.checkout.presentation.CheckoutUiState

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
    menuUiState: MenuUiState,
    cartUiState: CartUiState,
    checkoutUiState: CheckoutUiState,
    isDailySessionOpen: Boolean,
    isDailySessionStatusKnown: Boolean,
    onRefresh: () -> Unit,
    onRefreshSessionStatus: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onLoadPaymentMethods: () -> Unit,
    onAddVariant: (menuName: String, variantId: Long, variantName: String, price: Long) -> Unit,
    onRemoveVariant: (variantId: Long) -> Unit,
    onDeleteVariant: (variantId: Long) -> Unit,
    onPaymentMethodSelected: (paymentMethodId: Long) -> Unit,
    onQuickAmountSelected: (amount: Long) -> Unit,
    onPaidAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmitCheckout: () -> Unit,
    onDismissCheckoutPreview: () -> Unit,
    onClearCheckoutMessage: () -> Unit
) {
    val totalAmount = cartUiState.totalAmount
    val exactAmount = totalAmount
    val isCashier = remember(session.role) { session.role.equals("kasir", ignoreCase = true) }

    val quickAmounts = remember(totalAmount) { buildQuickAmounts(totalAmount) }
    val rawMenuItems = remember(menuUiState.menus) { buildMenuVariantItems(menuUiState.menus) }
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
    val filteredMenuItems = remember(menuItems, menuUiState.selectedCategory) {
        filterMenuItems(menuItems, menuUiState.selectedCategory)
    }
    val emptyStateMessage = "Belum ada menu yang tersedia untuk dijual saat ini."

    var cashierPage by rememberSaveable { mutableStateOf(CashierPage.MENU) }

    if (cashierPage != CashierPage.MENU) {
        BackHandler { cashierPage = CashierPage.MENU }
    }

    LaunchedEffect(checkoutUiState.checkoutMessage) {
        if (!checkoutUiState.checkoutMessage.isNullOrBlank()) {
            delay(3000)
            onClearCheckoutMessage()
        }
    }

    LaunchedEffect(cashierPage) {
        when (cashierPage) {
            CashierPage.MENU -> onRefresh()
            CashierPage.PAYMENT -> {
                // Status sesi dapat berubah ketika admin membuka atau menutup sesi.
                // Segarkan sebelum checkout agar tombol Bayar tidak memakai state lama.
                onRefreshSessionStatus()
                onRefresh()
                onLoadPaymentMethods()
            }
            CashierPage.CART -> Unit
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KebabBg,
        topBar = {
            MenuTopBar(
                cashierPage = cashierPage,
                itemCount = cartUiState.cartItems.sumOf { it.qty },
                onBack = {
                    cashierPage = if (cashierPage == CashierPage.PAYMENT) {
                        CashierPage.CART
                    } else {
                        CashierPage.MENU
                    }
                }
            )
        },
        floatingActionButton = {
            if (cashierPage == CashierPage.MENU) {
                CartFloatingActionButton(
                    itemCount = cartUiState.cartItems.sumOf { it.qty },
                    onClick = { cashierPage = CashierPage.CART }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        val cartInteractionEnabled = !checkoutUiState.isSubmitting

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 0.dp) // padding content
        ) {

            Box(modifier = Modifier.weight(1f)) {
                when (cashierPage) {
                    CashierPage.MENU -> {
                        if (menuUiState.isLoading) {
                            MenuListSkeletonTab()
                        } else {
                            MenuListTab(
                                menuItems = filteredMenuItems,
                                categories = categories,
                                selectedCategory = menuUiState.selectedCategory,
                                cartItems = cartUiState.cartItems,
                                cartInteractionEnabled = cartInteractionEnabled,
                                emptyStateMessage = emptyStateMessage,
                                onCategorySelected = onCategorySelected,
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
                                cartItems = cartUiState.cartItems,
                                totalAmount = totalAmount,
                                onAddVariant = onAddVariant,
                                onRemoveVariant = onRemoveVariant,
                                onDeleteVariant = onDeleteVariant,
                                cartInteractionEnabled = cartInteractionEnabled,
                                onNavigateToPayment = { cashierPage = CashierPage.PAYMENT },
                                onBackToMenu = { cashierPage = CashierPage.MENU }
                            )
                        }
                    }

                    CashierPage.PAYMENT -> {
                        PaymentTab(
                            checkoutUiState = checkoutUiState,
                            cartItems = cartUiState.cartItems,
                            isDailySessionOpen = isDailySessionOpen,
                            isDailySessionStatusKnown = isDailySessionStatusKnown,
                            isLoading = menuUiState.isLoading,
                            totalAmount = totalAmount,
                            exactAmount = exactAmount,
                            quickAmounts = quickAmounts,
                            cartInteractionEnabled = cartInteractionEnabled,
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

                // Banners Overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .align(Alignment.TopCenter),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedVisibility(
                        visible = !menuUiState.errorMessage.isNullOrBlank(),
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth(),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = menuUiState.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = !checkoutUiState.checkoutMessage.isNullOrBlank(),
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = KebabSuccessBg,
                            modifier = Modifier.fillMaxWidth(),
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = checkoutUiState.checkoutMessage ?: "",
                                color = KebabSuccess,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuTopBar(
    cashierPage: CashierPage,
    itemCount: Int,
    onBack: () -> Unit
) {
    val title = when (cashierPage) {
        CashierPage.MENU -> stringResource(R.string.app_name)
        CashierPage.CART -> stringResource(R.string.menu_top_cart_title)
        CashierPage.PAYMENT -> stringResource(R.string.menu_top_payment_title)
    }
    val subtitle = when (cashierPage) {
        CashierPage.MENU -> stringResource(R.string.menu_top_menu_subtitle)
        CashierPage.CART -> stringResource(R.string.menu_top_cart_subtitle)
        CashierPage.PAYMENT -> stringResource(R.string.menu_top_payment_subtitle)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KebabBg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cashierPage == CashierPage.MENU) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.kebab_sk_logo),
                                contentDescription = stringResource(R.string.cd_app_logo),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(KebabPrimary.copy(alpha = 0.10f))
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = KebabPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KebabPrimary
                        )
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = KebabTextGray
                        )
                    }
                }

                val badgeText = when (cashierPage) {
                    CashierPage.MENU -> if (itemCount > 0) {
                        pluralStringResource(R.plurals.item_count, itemCount, itemCount)
                    } else {
                        stringResource(R.string.menu_badge_cashier)
                    }
                    CashierPage.CART -> pluralStringResource(R.plurals.item_count, itemCount, itemCount)
                    CashierPage.PAYMENT -> stringResource(R.string.checkout_cash_label)
                }
                Text(
                    text = badgeText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = KebabPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(KebabPrimary.copy(alpha = 0.10f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartFloatingActionButton(itemCount: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = KebabPrimary,
        contentColor = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier
            .padding(end = 12.dp, bottom = 92.dp)
            .size(56.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
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
                    contentDescription = stringResource(R.string.cd_cart),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
