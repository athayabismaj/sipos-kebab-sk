package com.sipos.kebabsk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipos.kebabsk.feature.auth.presentation.forgotpassword.ForgotPasswordViewModel
import com.sipos.kebabsk.feature.auth.presentation.login.LoginViewModel
import com.sipos.kebabsk.feature.menu.presentation.MenuViewModel
import com.sipos.kebabsk.ui.theme.SiposKebabSkTheme

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModels()
    private val menuViewModel: MenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen BEFORE super.onCreate() so the theme
        // transition is seamless. The splash dismisses automatically once
        // the first Compose frame is drawn.
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SiposKebabSkTheme {
                val loginUiState = loginViewModel.uiState.collectAsStateWithLifecycle().value
                val forgotUiState = forgotPasswordViewModel.uiState.collectAsStateWithLifecycle().value
                val menuUiState = menuViewModel.uiState.collectAsStateWithLifecycle().value

                AuthRoot(
                    loginUiState = loginUiState,
                    forgotPasswordUiState = forgotUiState,
                    menuUiState = menuUiState,
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
                    onAddVariant = menuViewModel::addVariantToCart,
                    onRemoveVariant = menuViewModel::removeFromCart,
                    onCategorySelected = menuViewModel::onCategorySelected,
                    onPaymentMethodSelected = menuViewModel::onPaymentMethodSelected,
                    onQuickAmountSelected = menuViewModel::onQuickAmountSelected,
                    onPaidAmountChanged = menuViewModel::onPaidAmountChanged,
                    onNoteChanged = menuViewModel::onNoteChanged,
                    onSubmitCheckout = { token -> menuViewModel.submitCheckout(token) },
                    onDismissCheckoutPreview = menuViewModel::dismissCheckoutPreview
                )
            }
        }
    }
}
