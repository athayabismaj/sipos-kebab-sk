package com.sipos.kebabsk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipos.kebabsk.data.network.NetworkModule
import com.sipos.kebabsk.feature.auth.presentation.forgotpassword.ForgotPasswordScreen
import com.sipos.kebabsk.feature.auth.presentation.forgotpassword.ForgotPasswordUiState
import com.sipos.kebabsk.feature.auth.presentation.login.LoginScreen
import com.sipos.kebabsk.feature.auth.presentation.login.LoginUiState
import com.sipos.kebabsk.feature.menu.presentation.MenuScreen
import com.sipos.kebabsk.feature.menu.presentation.MenuUiState
import com.sipos.kebabsk.feature.profile.presentation.ChangePasswordScreen
import com.sipos.kebabsk.feature.profile.presentation.EditProfileScreen
import com.sipos.kebabsk.feature.profile.presentation.ProfileScreen
import com.sipos.kebabsk.feature.profile.presentation.RevenueSummaryScreen
import com.sipos.kebabsk.feature.profile.presentation.RevenueViewModel
import com.sipos.kebabsk.feature.profile.presentation.RevenueViewModelFactory
import com.sipos.kebabsk.feature.transactions.data.repository.TransactionsRepositoryImpl
import com.sipos.kebabsk.feature.transactions.presentation.TransactionsScreen
import com.sipos.kebabsk.feature.transactions.presentation.TransactionsViewModel
import com.sipos.kebabsk.feature.transactions.presentation.TransactionsViewModelFactory
import com.sipos.kebabsk.ui.theme.BrandAmber
import com.sipos.kebabsk.ui.theme.BrandOrange
import com.sipos.kebabsk.ui.theme.SiposKebabSkTheme
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class AuthRoute {
    LOGIN,
    FORGOT_PASSWORD,
    APP
}

private enum class AppTab(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    CASHIER("Kasir", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    TRANSACTIONS("Transaksi", Icons.Filled.List, Icons.Outlined.List),
    PROFILE("Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

private enum class ProfilePage {
    SUMMARY,
    REVENUE_SUMMARY,
    EDIT,
    CHANGE_PASSWORD
}

@Composable
fun AuthRoot(
    loginUiState: LoginUiState,
    forgotPasswordUiState: ForgotPasswordUiState,
    menuUiState: MenuUiState,
    onIdentifierChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onRefreshSession: () -> Unit,
    onUpdateProfile: (name: String, username: String, email: String) -> Unit,
    onChangePassword: (currentPassword: String, newPassword: String, confirmPassword: String) -> Unit,
    onClearProfileMessage: () -> Unit,
    onLogout: () -> Unit,
    onForgotEmailChanged: (String) -> Unit,
    onForgotCodeChanged: (String) -> Unit,
    onForgotNewPasswordChanged: (String) -> Unit,
    onForgotConfirmPasswordChanged: (String) -> Unit,
    onForgotSubmitRequest: () -> Unit,
    onForgotSubmitVerification: () -> Unit,
    onForgotSubmitResetPassword: () -> Unit,
    onForgotReset: () -> Unit,
    onLoadMenus: (token: String, forceRefresh: Boolean) -> Unit,
    onAddVariant: (menuName: String, variantId: Long, variantName: String, price: Double) -> Unit,
    onRemoveVariant: (variantId: Long) -> Unit,
    onCategorySelected: (category: String?) -> Unit,
    onPaymentMethodSelected: (paymentMethodId: Long) -> Unit,
    onQuickAmountSelected: (amount: Int) -> Unit,
    onPaidAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmitCheckout: (token: String) -> Unit,
    onDismissCheckoutPreview: () -> Unit
) {
    var route by remember(loginUiState.session) {
        mutableStateOf(if (loginUiState.session == null) AuthRoute.LOGIN else AuthRoute.APP)
    }

    if (loginUiState.session != null) {
        route = AuthRoute.APP
    }

    LaunchedEffect(loginUiState.session?.token, route) {
        val session = loginUiState.session
        if (route == AuthRoute.APP && session != null) {
            onRefreshSession()
            onLoadMenus(session.token, false)
        }
    }

    when (route) {
        AuthRoute.LOGIN -> LoginScreen(
            modifier = Modifier.fillMaxSize(),
            uiState = loginUiState,
            onIdentifierChanged = onIdentifierChanged,
            onPasswordChanged = onPasswordChanged,
            onLogin = onLogin,
            onForgotPassword = {
                onForgotReset()
                route = AuthRoute.FORGOT_PASSWORD
            }
        )

        AuthRoute.FORGOT_PASSWORD -> ForgotPasswordScreen(
            modifier = Modifier.fillMaxSize(),
            uiState = forgotPasswordUiState,
            onEmailChanged = onForgotEmailChanged,
            onCodeChanged = onForgotCodeChanged,
            onNewPasswordChanged = onForgotNewPasswordChanged,
            onConfirmPasswordChanged = onForgotConfirmPasswordChanged,
            onRequestReset = onForgotSubmitRequest,
            onVerifyCode = onForgotSubmitVerification,
            onResetPassword = onForgotSubmitResetPassword,
            onBackToLogin = {
                onForgotReset()
                route = AuthRoute.LOGIN
            }
        )

        AuthRoute.APP -> AppScaffold(
            loginUiState = loginUiState,
            menuUiState = menuUiState,
            onLoadMenus = onLoadMenus,
            onLogout = {
                onLogout()
                route = AuthRoute.LOGIN
            },
            onUpdateProfile = onUpdateProfile,
            onChangePassword = onChangePassword,
            onClearProfileMessage = onClearProfileMessage,
            onAddVariant = onAddVariant,
            onRemoveVariant = onRemoveVariant,
            onCategorySelected = onCategorySelected,
            onPaymentMethodSelected = onPaymentMethodSelected,
            onQuickAmountSelected = onQuickAmountSelected,
            onPaidAmountChanged = onPaidAmountChanged,
            onNoteChanged = onNoteChanged,
            onSubmitCheckout = onSubmitCheckout,
            onDismissCheckoutPreview = onDismissCheckoutPreview
        )
    }
}

@Composable
private fun AppScaffold(
    loginUiState: LoginUiState,
    menuUiState: MenuUiState,
    onLoadMenus: (token: String, forceRefresh: Boolean) -> Unit,
    onLogout: () -> Unit,
    onUpdateProfile: (name: String, username: String, email: String) -> Unit,
    onChangePassword: (currentPassword: String, newPassword: String, confirmPassword: String) -> Unit,
    onClearProfileMessage: () -> Unit,
    onAddVariant: (menuName: String, variantId: Long, variantName: String, price: Double) -> Unit,
    onRemoveVariant: (variantId: Long) -> Unit,
    onCategorySelected: (category: String?) -> Unit,
    onPaymentMethodSelected: (paymentMethodId: Long) -> Unit,
    onQuickAmountSelected: (amount: Int) -> Unit,
    onPaidAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmitCheckout: (token: String) -> Unit,
    onDismissCheckoutPreview: () -> Unit
) {
    val session = checkNotNull(loginUiState.session)
    val profileEmail = session.email
    val profileUsername = session.username
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.CASHIER) }
    var profilePage by rememberSaveable { mutableStateOf(ProfilePage.SUMMARY) }
    var cashierTransactionStarted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(loginUiState.successMessage, profilePage) {
        if (!loginUiState.successMessage.isNullOrBlank() && profilePage != ProfilePage.SUMMARY) {
            profilePage = ProfilePage.SUMMARY
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                AppTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            selectedTab = tab
                            if (tab != AppTab.PROFILE) profilePage = ProfilePage.SUMMARY
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            AppTab.CASHIER -> {
                if (!cashierTransactionStarted) {
                    CashierDashboardScreen(
                        modifier = Modifier.padding(innerPadding),
                        cashierName = if (menuUiState.cashierName.isBlank()) session.displayName else menuUiState.cashierName,
                        cashierRole = session.role ?: menuUiState.cashierRole ?: "kasir",
                        onStartTransaction = { cashierTransactionStarted = true }
                    )
                } else {
                    MenuScreen(
                        modifier = Modifier.padding(innerPadding),
                        session = session,
                        uiState = menuUiState,
                        onRefresh = { onLoadMenus(session.token, true) },
                        onCategorySelected = onCategorySelected,
                        onAddVariant = onAddVariant,
                        onRemoveVariant = onRemoveVariant,
                        onPaymentMethodSelected = onPaymentMethodSelected,
                        onQuickAmountSelected = onQuickAmountSelected,
                        onPaidAmountChanged = onPaidAmountChanged,
                        onNoteChanged = onNoteChanged,
                        onSubmitCheckout = { onSubmitCheckout(session.token) },
                        onDismissCheckoutPreview = onDismissCheckoutPreview
                    )
                }
            }

            AppTab.TRANSACTIONS -> {
                val factory = remember(session.token) { TransactionsViewModelFactory(session.token) }
                val transactionsViewModel: TransactionsViewModel = viewModel(factory = factory)
                TransactionsScreen(viewModel = transactionsViewModel, modifier = Modifier.padding(innerPadding))
            }

            AppTab.PROFILE -> {
                when (profilePage) {
                    ProfilePage.SUMMARY -> {
                        ProfileScreen(
                            modifier = Modifier.padding(innerPadding),
                            displayName = if (menuUiState.cashierName.isBlank()) session.displayName else menuUiState.cashierName,
                            email = profileEmail,
                            username = profileUsername,
                            role = session.role ?: menuUiState.cashierRole,
                            onEditProfile = {
                                onClearProfileMessage()
                                profilePage = ProfilePage.EDIT
                            },
                            onChangePassword = {
                                onClearProfileMessage()
                                profilePage = ProfilePage.CHANGE_PASSWORD
                            },
                            onViewRevenue = {
                                profilePage = ProfilePage.REVENUE_SUMMARY
                            },
                            onLogout = onLogout
                        )
                    }

                    ProfilePage.EDIT -> {
                        EditProfileScreen(
                            modifier = Modifier.padding(innerPadding),
                            initialName = if (menuUiState.cashierName.isBlank()) session.displayName else menuUiState.cashierName,
                            initialUsername = profileUsername,
                            initialEmail = profileEmail,
                            initialRole = session.role ?: menuUiState.cashierRole ?: "kasir",
                            isSaving = loginUiState.isLoading,
                            errorMessage = loginUiState.errorMessage,
                            successMessage = loginUiState.successMessage,
                            onBack = {
                                onClearProfileMessage()
                                profilePage = ProfilePage.SUMMARY
                            },
                            onSave = { name, username, email ->
                                onUpdateProfile(name, username, email)
                            }
                        )
                    }

                    ProfilePage.CHANGE_PASSWORD -> {
                        ChangePasswordScreen(
                            modifier = Modifier.padding(innerPadding),
                            isSaving = loginUiState.isLoading,
                            errorMessage = loginUiState.errorMessage,
                            successMessage = loginUiState.successMessage,
                            onBack = {
                                onClearProfileMessage()
                                profilePage = ProfilePage.SUMMARY
                            },
                            onSave = { currentPassword, newPassword, confirmPassword ->
                                onChangePassword(currentPassword, newPassword, confirmPassword)
                            }
                        )
                    }

                    ProfilePage.REVENUE_SUMMARY -> {
                        val repository = remember { TransactionsRepositoryImpl(NetworkModule.transactionsApiService) }
                        val factory = remember(session.token) { RevenueViewModelFactory(session.token, repository) }
                        val revenueViewModel: RevenueViewModel = viewModel(factory = factory)
                        val revenueUiState by revenueViewModel.uiState.collectAsStateWithLifecycle()

                        RevenueSummaryScreen(
                            modifier = Modifier.padding(innerPadding),
                            uiState = revenueUiState,
                            onDateChanged = revenueViewModel::setDate,
                            onRefresh = revenueViewModel::refresh,
                            onBack = {
                                profilePage = ProfilePage.SUMMARY
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CashierDashboardScreen(
    modifier: Modifier = Modifier,
    cashierName: String,
    cashierRole: String,
    onStartTransaction: () -> Unit
) {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.forLanguageTag("id-ID")) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss", Locale.forLanguageTag("id-ID")) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(1000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting header
        Text(
            text = "Selamat Datang,",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = cashierName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Hero Card – gradient background
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(BrandOrange, BrandAmber)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Role badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cashierRole.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Time
                    Text(
                        text = currentTime.format(timeFormatter),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )

                    // Date
                    Text(
                        text = currentTime.format(dateFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Info row – quick stats placeholder
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoChip(
                modifier = Modifier.weight(1f),
                label = "Sesi",
                value = "Aktif"
            )
            InfoChip(
                modifier = Modifier.weight(1f),
                label = "Kasir",
                value = cashierName.split(" ").first()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // CTA Button
        Button(
            onClick = onStartTransaction,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandOrange,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Mulai Transaksi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun InfoChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
