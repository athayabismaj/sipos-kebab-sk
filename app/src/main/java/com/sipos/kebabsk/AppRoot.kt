package com.sipos.kebabsk

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import com.sipos.kebabsk.common.presentation.AppBottomNavigation
import com.sipos.kebabsk.feature.auth.presentation.forgotpassword.ForgotPasswordScreen
import com.sipos.kebabsk.feature.auth.presentation.forgotpassword.ForgotPasswordUiState
import com.sipos.kebabsk.feature.auth.presentation.login.LoginScreen
import com.sipos.kebabsk.feature.auth.presentation.login.LoginUiState
import com.sipos.kebabsk.feature.auth.presentation.login.SessionSyncState
import com.sipos.kebabsk.feature.dashboard.presentation.DashboardScreen
import com.sipos.kebabsk.feature.dailystock.presentation.DailyStockViewModel
import com.sipos.kebabsk.feature.expense.presentation.OperationalExpenseViewModel
import com.sipos.kebabsk.feature.menu.presentation.MenuScreen
import com.sipos.kebabsk.feature.menu.presentation.MenuUiState
import com.sipos.kebabsk.feature.cart.presentation.CartUiState
import com.sipos.kebabsk.feature.checkout.presentation.CheckoutUiState
import com.sipos.kebabsk.feature.cart.domain.model.CartItem
import com.sipos.kebabsk.feature.profile.presentation.ChangePasswordScreen
import com.sipos.kebabsk.feature.profile.presentation.CloseStockSessionScreen
import com.sipos.kebabsk.feature.profile.presentation.DailyStockScreen
import com.sipos.kebabsk.feature.profile.presentation.EditProfileScreen
import com.sipos.kebabsk.feature.profile.presentation.BluetoothPrinterScreen
import com.sipos.kebabsk.feature.profile.presentation.OperationalExpenseScreen
import com.sipos.kebabsk.feature.profile.presentation.ProfileScreen
import com.sipos.kebabsk.feature.shift.presentation.ShiftSummaryUiState
import com.sipos.kebabsk.feature.shift.presentation.ShiftSummaryViewModel
import com.sipos.kebabsk.feature.splash.presentation.KebabSkSplashScreen
import com.sipos.kebabsk.feature.transactions.presentation.TransactionsScreen
import com.sipos.kebabsk.feature.transactions.presentation.TransactionsViewModel
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.SiposKebabSkTheme
import kotlinx.coroutines.delay

private enum class AuthRoute {
    LOGIN,
    FORGOT_PASSWORD,
    APP
}

enum class AppTab(@param:StringRes val labelRes: Int, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    CASHIER(R.string.nav_cashier, Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    TRANSACTIONS(R.string.nav_transactions, Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List),
    PROFILE(R.string.nav_profile, Icons.Filled.Person, Icons.Outlined.Person)
}


private enum class ProfilePage {
    SUMMARY,
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
    cartUiState: CartUiState,
    checkoutUiState: CheckoutUiState,
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
    onAddVariant: (menuName: String, variantId: Long, variantName: String, price: Long) -> Unit,
    onRemoveVariant: (variantId: Long) -> Unit,
    onDeleteVariant: (variantId: Long) -> Unit,
    onCategorySelected: (categoryId: Long?) -> Unit,
    onLoadMoreMenus: () -> Unit,
    onRetryLoadMoreMenus: () -> Unit,
    onLoadPaymentMethods: (token: String) -> Unit,
    onPaymentMethodSelected: (paymentMethodId: Long) -> Unit,
    onQuickAmountSelected: (amount: Long) -> Unit,
    onPaidAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmitCheckout: (token: String, cartItems: List<CartItem>, isDailySessionOpen: Boolean, isDailySessionStatusKnown: Boolean) -> Unit,
    onRetryQris: () -> Unit,
    onConfirmQrisPayment: () -> Unit,
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
            cartUiState = cartUiState,
            checkoutUiState = checkoutUiState,
            onLoadMenus = onLoadMenus,
            onRefreshSession = onRefreshSession,
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
            onLoadMoreMenus = onLoadMoreMenus,
            onRetryLoadMoreMenus = onRetryLoadMoreMenus,
            onLoadPaymentMethods = onLoadPaymentMethods,
            onPaymentMethodSelected = onPaymentMethodSelected,
            onQuickAmountSelected = onQuickAmountSelected,
            onPaidAmountChanged = onPaidAmountChanged,
            onNoteChanged = onNoteChanged,
            onSubmitCheckout = onSubmitCheckout,
            onRetryQris = onRetryQris,
            onConfirmQrisPayment = onConfirmQrisPayment,
            onDismissCheckoutPreview = onDismissCheckoutPreview,
            onClearCheckoutMessage = onClearCheckoutMessage
        )
    }
}

@Composable
private fun AppScaffold(
    loginUiState: LoginUiState,
    menuUiState: MenuUiState,
    cartUiState: CartUiState,
    checkoutUiState: CheckoutUiState,
    onLoadMenus: (token: String, forceRefresh: Boolean) -> Unit,
    onRefreshSession: () -> Unit,
    onLogout: () -> Unit,
    onUpdateProfile: (name: String, username: String, email: String) -> Unit,
    onChangePassword: (currentPassword: String, newPassword: String, confirmPassword: String) -> Unit,
    onClearProfileMessage: () -> Unit,
    onAddVariant: (menuName: String, variantId: Long, variantName: String, price: Long) -> Unit,
    onRemoveVariant: (variantId: Long) -> Unit,
    onDeleteVariant: (variantId: Long) -> Unit,
    onCategorySelected: (categoryId: Long?) -> Unit,
    onLoadMoreMenus: () -> Unit,
    onRetryLoadMoreMenus: () -> Unit,
    onLoadPaymentMethods: (token: String) -> Unit,
    onPaymentMethodSelected: (paymentMethodId: Long) -> Unit,
    onQuickAmountSelected: (amount: Long) -> Unit,
    onPaidAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmitCheckout: (token: String, cartItems: List<CartItem>, isDailySessionOpen: Boolean, isDailySessionStatusKnown: Boolean) -> Unit,
    onRetryQris: () -> Unit,
    onConfirmQrisPayment: () -> Unit,
    onDismissCheckoutPreview: () -> Unit,
    onClearCheckoutMessage: () -> Unit
) {
    val session = checkNotNull(loginUiState.session)
    val profileEmail = session.email
    val profileUsername = session.username
    val sessionStateKey = remember(session.token) { session.token.hashCode().toString() }
    var selectedTab by rememberSaveable(sessionStateKey) { mutableStateOf(AppTab.CASHIER) }
    var profilePage by rememberSaveable(sessionStateKey) { mutableStateOf(ProfilePage.SUMMARY) }
    var cashierTransactionStarted by rememberSaveable(sessionStateKey) { mutableStateOf(false) }

    // Shared DailyStockViewModel agar sessionId bisa diakses oleh Transactions tab (untuk Void)
    val sharedDailyStockViewModel: DailyStockViewModel = koinViewModel(key = "daily-stock-$sessionStateKey")
    val sharedDailyStockUiState by sharedDailyStockViewModel.uiState.collectAsStateWithLifecycle()
    val isDashboardDailySessionStatusKnown = !sharedDailyStockUiState.isLoading &&
        (sharedDailyStockUiState.isSessionOpen != null || menuUiState.isDailySessionStatusKnown)
    val isDashboardDailySessionOpen = sharedDailyStockUiState.isSessionOpen ?: menuUiState.isDailySessionOpen
    val dashboardDailySessionLabel = sharedDailyStockUiState.sessionStatusLabel ?: menuUiState.dailySessionStatusLabel

    LaunchedEffect(selectedTab, profilePage, session.token) {
        if (selectedTab == AppTab.PROFILE && profilePage == ProfilePage.DAILY_STOCK) {
            sharedDailyStockViewModel.refresh()
        }
    }

    // Verify the session gate before the dashboard enables a new transaction.
    LaunchedEffect(session.token) {
        onLoadMenus(session.token, false)
    }

    LaunchedEffect(cashierTransactionStarted, session.token) {
        if (cashierTransactionStarted) {
            sharedDailyStockViewModel.refresh()
            onLoadMenus(session.token, false)
        }
    }

    LaunchedEffect(loginUiState.successMessage, profilePage) {
        if (!loginUiState.successMessage.isNullOrBlank() && profilePage != ProfilePage.SUMMARY) {
            profilePage = ProfilePage.SUMMARY
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = KebabBg
        ) { innerPadding ->
            when (selectedTab) {
                AppTab.CASHIER -> {
                    val shiftSummaryViewModel: ShiftSummaryViewModel = koinViewModel(key = "shift-summary-$sessionStateKey")
                    val shiftSummaryUiState by shiftSummaryViewModel.uiState.collectAsStateWithLifecycle()

                    if (!cashierTransactionStarted) {
                        DashboardScreen(
                            modifier = Modifier.padding(innerPadding),
                            cashierName = if (menuUiState.cashierName.isBlank()) session.displayName else menuUiState.cashierName,
                            cashierRole = session.role ?: menuUiState.cashierRole ?: "kasir",
                            isDailySessionOpen = isDashboardDailySessionOpen,
                            isDailySessionStatusKnown = isDashboardDailySessionStatusKnown,
                            dailySessionLabel = dashboardDailySessionLabel,
                            shiftSummaryUiState = shiftSummaryUiState,
                            isRefreshing = shiftSummaryUiState.isLoading ||
                                sharedDailyStockUiState.isLoading || menuUiState.isLoading,
                            onRefresh = {
                                shiftSummaryViewModel.refresh()
                                sharedDailyStockViewModel.refresh()
                                onLoadMenus(session.token, true)
                            },
                            onRetryShiftSummary = shiftSummaryViewModel::refresh,
                            onForceLogout = onLogout,
                            onStartTransaction = { cashierTransactionStarted = true },
                            isPendingSync = loginUiState.sessionSyncState == SessionSyncState.PENDING_SYNC
                        )
                    } else {
                        MenuScreen(
                            modifier = Modifier.padding(innerPadding),
                            session = session,
                            menuUiState = menuUiState,
                            cartUiState = cartUiState,
                            checkoutUiState = checkoutUiState,
                            isDailySessionOpen = isDashboardDailySessionOpen,
                            isDailySessionStatusKnown = isDashboardDailySessionStatusKnown,
                            onRefresh = { onLoadMenus(session.token, true) },
                            onRefreshSessionStatus = sharedDailyStockViewModel::refresh,
                            onCategorySelected = onCategorySelected,
                            onLoadMore = onLoadMoreMenus,
                            onRetryLoadMore = onRetryLoadMoreMenus,
                            onLoadPaymentMethods = { onLoadPaymentMethods(session.token) },
                            onAddVariant = onAddVariant,
                            onRemoveVariant = onRemoveVariant,
                            onDeleteVariant = onDeleteVariant,
                            onPaymentMethodSelected = onPaymentMethodSelected,
                            onQuickAmountSelected = onQuickAmountSelected,
                            onPaidAmountChanged = onPaidAmountChanged,
                            onNoteChanged = onNoteChanged,
                            onSubmitCheckout = {
                                onSubmitCheckout(
                                    session.token,
                                    cartUiState.cartItems,
                                    isDashboardDailySessionOpen,
                                    isDashboardDailySessionStatusKnown
                                )
                            },
                            onRetryQris = onRetryQris,
                            onConfirmQrisPayment = onConfirmQrisPayment,
                            onDismissCheckoutPreview = onDismissCheckoutPreview,
                            onClearCheckoutMessage = onClearCheckoutMessage
                        )
                    }
                }

                AppTab.TRANSACTIONS -> {
                    val transactionsViewModel: TransactionsViewModel = koinViewModel(key = "transactions-$sessionStateKey")
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
                                isRefreshing = loginUiState.isRefreshingSession || menuUiState.isLoading,
                                onRefresh = {
                                    onRefreshSession()
                                    onLoadMenus(session.token, true)
                                },
                                onEditProfile = {
                                    onClearProfileMessage()
                                    profilePage = ProfilePage.EDIT
                                },
                                onChangePassword = {
                                    onClearProfileMessage()
                                    profilePage = ProfilePage.CHANGE_PASSWORD
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
                        val dailyStockViewModel = sharedDailyStockViewModel
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
                            closingPresets = dailyStockUiState.closingPresets,
                            closingGroups = dailyStockUiState.closingGroups,
                            isPreviewingClosing = dailyStockUiState.isPreviewingClosing,
                            closingPreview = dailyStockUiState.closingPreview,
                            closingPreviewError = dailyStockUiState.closingPreviewError,
                            onBack = {
                                dailyStockViewModel.clearCloseState()
                                profilePage = ProfilePage.DAILY_STOCK
                            },
                            onPreview = dailyStockViewModel::previewClosing,
                            onClearPreview = dailyStockViewModel::clearClosingPreview,
                            onSubmit = { remaining, anchors, notes ->
                                if (anchors.isEmpty()) {
                                    dailyStockViewModel.closeSession(remaining, notes)
                                } else {
                                    dailyStockViewModel.closeSessionWithRecipe(remaining, anchors, notes)
                                }
                            }
                        )
                    }

                    ProfilePage.OPERATIONAL_EXPENSE -> {
                        val expenseViewModel: OperationalExpenseViewModel =
                            koinViewModel(key = "operational-expense-$sessionStateKey")
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

        AppBottomNavigation(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedDestination = selectedTab,
            onDestinationSelected = { tab ->
                selectedTab = tab
                if (tab != AppTab.PROFILE) profilePage = ProfilePage.SUMMARY
            }
        )
    }
}
