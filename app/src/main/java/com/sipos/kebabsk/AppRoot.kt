package com.sipos.kebabsk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.common.AppTime
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sipos.kebabsk.data.network.NetworkModule
import com.sipos.kebabsk.feature.auth.presentation.forgotpassword.ForgotPasswordScreen
import com.sipos.kebabsk.feature.auth.presentation.forgotpassword.ForgotPasswordUiState
import com.sipos.kebabsk.feature.auth.presentation.login.LoginScreen
import com.sipos.kebabsk.feature.auth.presentation.login.LoginUiState
import com.sipos.kebabsk.feature.auth.presentation.login.SessionSyncState
import com.sipos.kebabsk.feature.dailystock.data.repository.DailyStockRepositoryImpl
import com.sipos.kebabsk.feature.dailystock.presentation.DailyStockViewModel
import com.sipos.kebabsk.feature.dailystock.presentation.DailyStockViewModelFactory
import com.sipos.kebabsk.feature.expense.data.repository.OperationalExpenseRepositoryImpl
import com.sipos.kebabsk.feature.expense.presentation.OperationalExpenseViewModel
import com.sipos.kebabsk.feature.expense.presentation.OperationalExpenseViewModelFactory
import com.sipos.kebabsk.feature.menu.presentation.MenuScreen
import com.sipos.kebabsk.feature.menu.presentation.MenuUiState
import com.sipos.kebabsk.feature.profile.presentation.ChangePasswordScreen
import com.sipos.kebabsk.feature.profile.presentation.CloseStockSessionScreen
import com.sipos.kebabsk.feature.profile.presentation.DailyStockScreen
import com.sipos.kebabsk.feature.profile.presentation.EditProfileScreen
import com.sipos.kebabsk.feature.profile.presentation.BluetoothPrinterScreen
import com.sipos.kebabsk.feature.profile.presentation.OperationalExpenseScreen
import com.sipos.kebabsk.feature.profile.presentation.ProfileScreen
import com.sipos.kebabsk.feature.profile.presentation.RevenueSummaryScreen
import com.sipos.kebabsk.feature.profile.presentation.RevenueViewModel
import com.sipos.kebabsk.feature.profile.presentation.RevenueViewModelFactory
import com.sipos.kebabsk.feature.shift.presentation.ShiftSummaryUiState
import com.sipos.kebabsk.feature.shift.presentation.ShiftSummaryViewModel
import com.sipos.kebabsk.feature.shift.presentation.ShiftSummaryViewModelFactory
import com.sipos.kebabsk.feature.splash.presentation.KebabSkSplashScreen
import com.sipos.kebabsk.feature.transactions.data.repository.TransactionsRepositoryImpl
import com.sipos.kebabsk.feature.transactions.presentation.TransactionsScreen
import com.sipos.kebabsk.feature.transactions.presentation.TransactionsViewModel
import com.sipos.kebabsk.feature.transactions.presentation.TransactionsViewModelFactory
import com.sipos.kebabsk.ui.theme.BrandAmber
import com.sipos.kebabsk.ui.theme.BrandOrange
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabNavActiveBg
import com.sipos.kebabsk.ui.theme.KebabNavInactiveText
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabPrimaryContainer
import com.sipos.kebabsk.ui.theme.KebabSecondaryContainer
import com.sipos.kebabsk.ui.theme.KebabSuccess
import com.sipos.kebabsk.ui.theme.KebabSuccessBg
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray
import com.sipos.kebabsk.ui.theme.SiposKebabSkTheme
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class AuthRoute {
    LOGIN,
    FORGOT_PASSWORD,
    APP
}

private enum class AppTab(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    CASHIER("Kasir", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    TRANSACTIONS("Transaksi", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List),
    PROFILE("Profil", Icons.Filled.Person, Icons.Outlined.Person)
}


private enum class ProfilePage {
    SUMMARY,
    REVENUE_SUMMARY,
    DAILY_STOCK,
    OPERATIONAL_EXPENSE,
    BLUETOOTH_PRINTER,
    CLOSE_STOCK_SESSION,
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
    onDeleteVariant: (variantId: Long) -> Unit,
    onCategorySelected: (category: String?) -> Unit,
    onPaymentMethodSelected: (paymentMethodId: Long) -> Unit,
    onQuickAmountSelected: (amount: Int) -> Unit,
    onPaidAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmitCheckout: (token: String) -> Unit,
    onDismissCheckoutPreview: () -> Unit,
    onClearCheckoutMessage: () -> Unit
) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var route by rememberSaveable { mutableStateOf(AuthRoute.LOGIN) }

    // Splash minimum 700ms — tapi juga menunggu session validation selesai
    LaunchedEffect(Unit) {
        delay(700)
        showSplash = false
    }

    // Tetap tampilkan splash selama validasi sesi masih berjalan
    val isSessionChecking = loginUiState.sessionSyncState == SessionSyncState.CHECKING
    if (showSplash || isSessionChecking) {
        KebabSkSplashScreen()
        return
    }

    LaunchedEffect(loginUiState.session?.token, loginUiState.sessionSyncState) {
        route = when {
            loginUiState.sessionSyncState == SessionSyncState.DESYNCED -> AuthRoute.LOGIN
            loginUiState.session != null -> AuthRoute.APP
            route == AuthRoute.FORGOT_PASSWORD -> AuthRoute.FORGOT_PASSWORD
            else -> AuthRoute.LOGIN
        }
    }

    LaunchedEffect(loginUiState.session?.token, route) {
        val session = loginUiState.session
        if (route == AuthRoute.APP && session != null) {
            onRefreshSession()
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
            onDeleteVariant = onDeleteVariant,
            onCategorySelected = onCategorySelected,
            onPaymentMethodSelected = onPaymentMethodSelected,
            onQuickAmountSelected = onQuickAmountSelected,
            onPaidAmountChanged = onPaidAmountChanged,
            onNoteChanged = onNoteChanged,
            onSubmitCheckout = onSubmitCheckout,
            onDismissCheckoutPreview = onDismissCheckoutPreview,
            onClearCheckoutMessage = onClearCheckoutMessage
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
    onDeleteVariant: (variantId: Long) -> Unit,
    onCategorySelected: (category: String?) -> Unit,
    onPaymentMethodSelected: (paymentMethodId: Long) -> Unit,
    onQuickAmountSelected: (amount: Int) -> Unit,
    onPaidAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmitCheckout: (token: String) -> Unit,
    onDismissCheckoutPreview: () -> Unit,
    onClearCheckoutMessage: () -> Unit
) {
    val session = checkNotNull(loginUiState.session)
    val profileEmail = session.email
    val profileUsername = session.username
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.CASHIER) }
    var profilePage by rememberSaveable { mutableStateOf(ProfilePage.SUMMARY) }
    var cashierTransactionStarted by rememberSaveable { mutableStateOf(false) }

    // Shared DailyStockViewModel agar sessionId bisa diakses oleh Transactions tab (untuk Void)
    val sharedDsRepository = remember { DailyStockRepositoryImpl(NetworkModule.dailyStockApiService) }
    val sharedDsFactory = remember(session.token) { DailyStockViewModelFactory(session.token, sharedDsRepository) }
    val sharedDailyStockViewModel: DailyStockViewModel = viewModel(factory = sharedDsFactory)
    val sharedDailyStockUiState by sharedDailyStockViewModel.uiState.collectAsStateWithLifecycle()

    // Fetch session saat pertama kali masuk AppScaffold
    LaunchedEffect(session.token) {
        sharedDailyStockViewModel.refresh()
    }

    LaunchedEffect(cashierTransactionStarted, session.token) {
        if (cashierTransactionStarted) {
            onLoadMenus(session.token, false)
        }
    }

    LaunchedEffect(loginUiState.successMessage, profilePage) {
        if (!loginUiState.successMessage.isNullOrBlank() && profilePage != ProfilePage.SUMMARY) {
            profilePage = ProfilePage.SUMMARY
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = KebabBg,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth(),
                containerColor = KebabBg,
                tonalElevation = 0.dp
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
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KebabPrimary,
                            selectedTextColor = KebabPrimary,
                            indicatorColor = Color(0xFFFFEDD5),
                            unselectedIconColor = KebabNavInactiveText,
                            unselectedTextColor = KebabNavInactiveText
                        ),
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            AppTab.CASHIER -> {
                val repository = remember { TransactionsRepositoryImpl(NetworkModule.transactionsApiService) }
                val shiftFactory = remember(session.token) { ShiftSummaryViewModelFactory(session.token, repository) }
                val shiftSummaryViewModel: ShiftSummaryViewModel = viewModel(factory = shiftFactory)
                val shiftSummaryUiState by shiftSummaryViewModel.uiState.collectAsStateWithLifecycle()

                if (!cashierTransactionStarted) {
                    CashierDashboardScreen(
                        modifier = Modifier.padding(innerPadding),
                        cashierName = if (menuUiState.cashierName.isBlank()) session.displayName else menuUiState.cashierName,
                        cashierRole = session.role ?: menuUiState.cashierRole ?: "kasir",
                        isDailySessionOpen = menuUiState.isDailySessionOpen,
                        dailySessionLabel = menuUiState.dailySessionStatusLabel,
                        dailyTargetRevenue = menuUiState.dailyTargetRevenue,
                        shiftSummaryUiState = shiftSummaryUiState,
                        onRetryShiftSummary = shiftSummaryViewModel::refresh,
                        onForceLogout = onLogout,
                        onStartTransaction = { cashierTransactionStarted = true },
                        isPendingSync = loginUiState.sessionSyncState == SessionSyncState.PENDING_SYNC
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
                        onDeleteVariant = onDeleteVariant,
                        onPaymentMethodSelected = onPaymentMethodSelected,
                        onQuickAmountSelected = onQuickAmountSelected,
                        onPaidAmountChanged = onPaidAmountChanged,
                        onNoteChanged = onNoteChanged,
                        onSubmitCheckout = { onSubmitCheckout(session.token) },
                        onDismissCheckoutPreview = onDismissCheckoutPreview,
                        onClearCheckoutMessage = onClearCheckoutMessage
                    )
                }
            }

            AppTab.TRANSACTIONS -> {
                val factory = remember(session.token) { TransactionsViewModelFactory(session.token) }
                val transactionsViewModel: TransactionsViewModel = viewModel(factory = factory)
                TransactionsScreen(
                    viewModel = transactionsViewModel, 
                    modifier = Modifier.padding(innerPadding),
                    sessionId = sharedDailyStockUiState.sessionId,
                    onForceLogout = onLogout
                )
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
                            onViewDailyStock = {
                                profilePage = ProfilePage.DAILY_STOCK
                            },
                            onViewOperationalExpense = {
                                profilePage = ProfilePage.OPERATIONAL_EXPENSE
                            },
                            onConnectReceiptPrinter = {
                                profilePage = ProfilePage.BLUETOOTH_PRINTER
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

                    ProfilePage.DAILY_STOCK -> {
                        DailyStockScreen(
                            modifier = Modifier.padding(innerPadding),
                            items = sharedDailyStockUiState.items,
                            sessionId = sharedDailyStockUiState.sessionId,
                            isLoading = sharedDailyStockUiState.isLoading,
                            errorMessage = sharedDailyStockUiState.errorMessage,
                            onBack = {
                                profilePage = ProfilePage.SUMMARY
                            },
                            onRetry = sharedDailyStockViewModel::refresh,
                            onForceLogout = onLogout,
                            onCloseSession = {
                                profilePage = ProfilePage.CLOSE_STOCK_SESSION
                            },
                            isCashReconciliationPending = sharedDailyStockUiState.isCashReconciliationPending,
                            onSessionAlreadyClosed = onLogout
                        )
                    }

                    ProfilePage.CLOSE_STOCK_SESSION -> {
                        val repository = remember { DailyStockRepositoryImpl(NetworkModule.dailyStockApiService) }
                        val factory = remember(session.token) { DailyStockViewModelFactory(session.token, repository) }
                        val dailyStockViewModel: DailyStockViewModel = viewModel(factory = factory)
                        val dailyStockUiState by dailyStockViewModel.uiState.collectAsStateWithLifecycle()

                        LaunchedEffect(Unit) {
                            dailyStockViewModel.refresh()
                        }

                        LaunchedEffect(dailyStockUiState.closeSuccess) {
                            if (dailyStockUiState.closeSuccess) {
                                onLogout()
                            }
                        }

                        CloseStockSessionScreen(
                            modifier = Modifier.padding(innerPadding),
                            items = dailyStockUiState.items,
                            isClosing = dailyStockUiState.isClosing,
                            closeErrorMessage = dailyStockUiState.closeErrorMessage,
                            onBack = {
                                dailyStockViewModel.clearCloseState()
                                profilePage = ProfilePage.DAILY_STOCK
                            },
                            onSubmit = { remaining, notes ->
                                dailyStockViewModel.closeSession(remaining, notes)
                            }
                        )
                    }

                    ProfilePage.OPERATIONAL_EXPENSE -> {
                        val repository = remember { OperationalExpenseRepositoryImpl(NetworkModule.operationalExpenseApiService) }
                        val factory = remember(session.token) { OperationalExpenseViewModelFactory(session.token, repository) }
                        val expenseViewModel: OperationalExpenseViewModel = viewModel(factory = factory)
                        val expenseUiState by expenseViewModel.uiState.collectAsStateWithLifecycle()

                        OperationalExpenseScreen(
                            modifier = Modifier.padding(innerPadding),
                            uiState = expenseUiState,
                            onAmountChanged = expenseViewModel::onAmountChanged,
                            onCategoryChanged = expenseViewModel::onCategoryChanged,
                            onNoteChanged = expenseViewModel::onNoteChanged,
                            onSubmit = expenseViewModel::submit,
                            onBack = {
                                profilePage = ProfilePage.SUMMARY
                            }
                        )
                    }

                    ProfilePage.BLUETOOTH_PRINTER -> {
                        BluetoothPrinterScreen(
                            modifier = Modifier.padding(innerPadding),
                            onBack = { profilePage = ProfilePage.SUMMARY }
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
    isDailySessionOpen: Boolean,
    dailySessionLabel: String?,
    dailyTargetRevenue: Double?,
    shiftSummaryUiState: ShiftSummaryUiState,
    onRetryShiftSummary: () -> Unit,
    onForceLogout: () -> Unit,
    onStartTransaction: () -> Unit,
    isPendingSync: Boolean = false
) {
    val scrollState = rememberScrollState()
    var currentTime by remember { mutableStateOf(AppTime.nowJakartaDateTime()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("id-ID")) }
    val firstName = cashierName.split(" ").firstOrNull().orEmpty().ifBlank { "Kasir" }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = AppTime.nowJakartaDateTime()
            delay(1000)
        }
    }

    // Outer column: fixed header + scrollable body
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ===== FIXED HEADER (tidak ikut scroll) =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Brand row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.RestaurantMenu,
                    contentDescription = null,
                    tint = KebabPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Kebab SK",
                    color = KebabPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }

            // Kasir info card
            UserInfoSection(
                cashierName = firstName,
                cashierRole = cashierRole,
                currentTime = currentTime.format(timeFormatter),
                isDailySessionOpen = isDailySessionOpen,
                dailySessionLabel = dailySessionLabel
            )

            // === SYNC PENDING BANNER ===
            if (isPendingSync) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KebabSecondaryContainer.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = KebabPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Menunggu Sinkronisasi — verifikasi sesi tertunda (offline)",
                        fontSize = 12.sp,
                        color = KebabPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ===== SCROLLABLE BODY =====
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when {
                shiftSummaryUiState.isLoading -> {
                    DashboardMetricCard(
                        title = "TotalTransaksi",
                        value = "...",
                        subValue = "Memuat data shift",
                        icon = Icons.AutoMirrored.Outlined.List
                    )
                    DashboardMetricCard(
                        title = "ItemTerjual",
                        value = "...",
                        subValue = "Memuat data shift",
                        icon = Icons.Outlined.ShoppingCart
                    )
                    DashboardMetricCard(
                        title = "Pendapatan Hari Ini",
                        value = "...",
                        subValue = "Memuat data shift",
                        icon = Icons.AutoMirrored.Outlined.List,
                        isPrimary = true
                    )
                }

                !shiftSummaryUiState.errorMessage.isNullOrBlank() -> {
                    val isSessionExpired = shiftSummaryUiState.errorMessage.contains("Sesi login sudah berakhir", ignoreCase = true)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8E8E8))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = shiftSummaryUiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = if (isSessionExpired) onForceLogout else onRetryShiftSummary,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KebabPrimary)
                            ) {
                                Text(if (isSessionExpired) "Login Ulang" else "Coba Lagi", color = Color.White)
                            }
                        }
                    }
                }

                else -> {
                    DashboardMetricCard(
                        title = "TotalTransaksi",
                        value = shiftSummaryUiState.totalTransactions.toString(),
                        subValue = shiftSummaryUiState.transactionGrowthPercentage?.let { pct ->
                            val sign = if (pct >= 0) "+" else ""
                            "${sign}${pct}% dari kemarin"
                        } ?: if (shiftSummaryUiState.totalTransactions > 0) "Hari ini pertama" else "Belum ada transaksi",
                        icon = Icons.AutoMirrored.Outlined.List
                    )
                    DashboardMetricCard(
                        title = "ItemTerjual",
                        value = shiftSummaryUiState.totalItemsSold.toString(),
                        subValue = shiftSummaryUiState.dominantItemName?.let { "Dominan: $it" }
                            ?: if (shiftSummaryUiState.totalItemsSold > 0) "Data dominan tersedia" else "Belum ada penjualan",
                        icon = Icons.Outlined.ShoppingCart
                    )
                    DashboardMetricCard(
                        title = "Pendapatan Hari Ini",
                        value = formatRupiah(shiftSummaryUiState.totalRevenue),
                        subValue = run {
                            val target = shiftSummaryUiState.dailyTargetRevenue
                                ?.takeIf { it > 0.0 }
                                ?: dailyTargetRevenue?.takeIf { it > 0.0 }
                            val percentage = when {
                                target != null -> (shiftSummaryUiState.totalRevenue / target) * 100.0
                                shiftSummaryUiState.revenueTargetPercentage != null -> shiftSummaryUiState.revenueTargetPercentage
                                else -> 0.0
                            }
                            "Target harian: ${String.format("%.1f", percentage)}% tercapai"
                        },
                        icon = Icons.AutoMirrored.Outlined.List,
                        isPrimary = true
                    )
                }
            }

            MainActionButton(
                enabled = isDailySessionOpen,
                onClick = onStartTransaction
            )

            if (!isDailySessionOpen) {
                Text(
                    text = "Sesi harian belum dibuka admin, transaksi belum dapat dilakukan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}



@Composable
private fun UserInfoSection(
    cashierName: String,
    cashierRole: String,
    currentTime: String,
    isDailySessionOpen: Boolean,
    dailySessionLabel: String?
) {
    val statusLabel = if (isDailySessionOpen) {
        dailySessionLabel ?: "Sesi Harian Aktif"
    } else {
        "Sesi Belum Dibuka"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F1ED)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9DD8D8))
                        .border(2.dp, KebabPrimaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text("Halo, $cashierName", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1E1E1E))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFE9DDCE))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cashierRole.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = KebabPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Filled.Person, contentDescription = null, tint = KebabTextGray, modifier = Modifier.size(14.dp))
                        Text(" $currentTime WIB", color = KebabTextGray, fontSize = 14.sp)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDailySessionOpen) KebabSuccessBg else Color(0xFFF9E8E8))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = statusLabel,
                    color = if (isDailySessionOpen) KebabSuccess else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    isPrimary: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 148.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) Color.White else Color(0xFFF3EDE8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPrimary) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = KebabTextGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(KebabPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(20.dp))
                }
            }

            Column {
                Text(
                    text = value,
                    fontSize = if (isPrimary) 32.sp else 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isPrimary) KebabPrimary else Color.Black
                )
                if (subValue.isNotEmpty()) {
                    Text(subValue, color = KebabTextGray, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun MainActionButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(KebabPrimary, KebabPrimaryContainer)))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Mulai Transaksi Baru", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                        Text("Buka antarmuka kasir utama", fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

private fun formatRupiah(value: Double): String {
    return java.text.NumberFormat
        .getCurrencyInstance(Locale.forLanguageTag("id-ID"))
        .format(value)
}
