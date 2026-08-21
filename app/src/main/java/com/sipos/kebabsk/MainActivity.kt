package com.sipos.kebabsk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.sipos.kebabsk.common.AuthSessionEvents
import com.sipos.kebabsk.common.perf.JankMonitor
import com.sipos.kebabsk.feature.auth.presentation.forgotpassword.ForgotPasswordViewModel
import com.sipos.kebabsk.feature.auth.presentation.login.LoginViewModel
import com.sipos.kebabsk.feature.menu.presentation.MenuViewModel
import com.sipos.kebabsk.feature.cart.presentation.CartViewModel
import com.sipos.kebabsk.feature.checkout.presentation.CheckoutViewModel
import com.sipos.kebabsk.ui.theme.SiposKebabSkTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModel()
    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModel()
    private val menuViewModel: MenuViewModel by viewModel()
    private val cartViewModel: CartViewModel by viewModel()
    private val checkoutViewModel: CheckoutViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { false }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (BuildConfig.DEBUG) {
            JankMonitor.attach(window)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AuthSessionEvents.forceLogout.collect {
                    loginViewModel.logout()
                    menuViewModel.clear()
                    cartViewModel.clearCart()
                    checkoutViewModel.clear()
                }
            }
        }

        setContent {
            SiposKebabSkTheme {
                val loginUiState = loginViewModel.uiState.collectAsStateWithLifecycle().value
                val forgotUiState = forgotPasswordViewModel.uiState.collectAsStateWithLifecycle().value
                val menuUiState = menuViewModel.uiState.collectAsStateWithLifecycle().value
                val cartUiState = cartViewModel.uiState.collectAsStateWithLifecycle().value
                val checkoutUiState = checkoutViewModel.uiState.collectAsStateWithLifecycle().value

                AuthRoot(
                    loginUiState = loginUiState,
                    forgotPasswordUiState = forgotUiState,
                    menuUiState = menuUiState,
                    cartUiState = cartUiState,
                    checkoutUiState = checkoutUiState,
                    onIdentifierChanged = loginViewModel::onIdentifierChanged,
                    onPasswordChanged = loginViewModel::onPasswordChanged,
                    onLogin = loginViewModel::login,
                    onRefreshSession = loginViewModel::refreshSession,
                    onUpdateProfile = loginViewModel::updateProfile,
                    onChangePassword = loginViewModel::changePassword,
                    onClearProfileMessage = loginViewModel::clearProfileMessage,
                    onLogout = {
                        loginViewModel.logout()
                        menuViewModel.clear()
                        cartViewModel.clearCart()
                        checkoutViewModel.clear()
                    },
                    onForgotEmailChanged = forgotPasswordViewModel::onEmailChanged,
                    onForgotCodeChanged = forgotPasswordViewModel::onCodeChanged,
                    onForgotNewPasswordChanged = forgotPasswordViewModel::onNewPasswordChanged,
                    onForgotConfirmPasswordChanged = forgotPasswordViewModel::onConfirmPasswordChanged,
                    onForgotSubmitRequest = forgotPasswordViewModel::submitForgotPassword,
                    onForgotSubmitVerification = forgotPasswordViewModel::submitCodeVerification,
                    onForgotSubmitResetPassword = forgotPasswordViewModel::submitResetPassword,
                    onForgotReset = forgotPasswordViewModel::resetState,
                    onLoadMenus = { token, forceRefresh -> menuViewModel.loadMenus(token, forceRefresh) },
                    onAddVariant = cartViewModel::addVariantToCart,
                    onRemoveVariant = cartViewModel::removeFromCart,
                    onDeleteVariant = cartViewModel::deleteFromCart,
                    onCategorySelected = menuViewModel::onCategorySelected,
                    onLoadMoreMenus = menuViewModel::loadNextPage,
                    onRetryLoadMoreMenus = menuViewModel::retryLoadMore,
                    onLoadPaymentMethods = checkoutViewModel::loadPaymentMethods,
                    onPaymentMethodSelected = checkoutViewModel::onPaymentMethodSelected,
                    onQuickAmountSelected = checkoutViewModel::onQuickAmountSelected,
                    onPaidAmountChanged = checkoutViewModel::onPaidAmountChanged,
                    onNoteChanged = checkoutViewModel::onNoteChanged,
                    onSubmitCheckout = { token, cartItems, isDailySessionOpen, isDailySessionStatusKnown ->
                        checkoutViewModel.submitCheckout(
                            token,
                            cartItems,
                            isDailySessionOpen,
                            isDailySessionStatusKnown
                        ) {
                            cartViewModel.clearCart()
                            menuViewModel.loadMenus(token, forceRefresh = true)
                        }
                    },
                    onRetryQris = checkoutViewModel::retryGenerateQris,
                    onConfirmQrisPayment = checkoutViewModel::confirmQrisPayment,
                    onDismissCheckoutPreview = checkoutViewModel::dismissCheckoutPreview,
                    onClearCheckoutMessage = checkoutViewModel::clearCheckoutMessage
                )
            }
        }
    }
}
