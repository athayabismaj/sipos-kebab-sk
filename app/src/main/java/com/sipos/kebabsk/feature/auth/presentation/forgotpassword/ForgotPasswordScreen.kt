package com.sipos.kebabsk.feature.auth.presentation.forgotpassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    uiState: ForgotPasswordUiState,
    onEmailChanged: (String) -> Unit,
    onCodeChanged: (String) -> Unit,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRequestReset: () -> Unit,
    onVerifyCode: () -> Unit,
    onResetPassword: () -> Unit,
    onBackToLogin: () -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Lupa Sandi",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (uiState.step) {

            // ================= REQUEST EMAIL =================
            ForgotPasswordStep.REQUEST -> {

                Text(
                    text = "Masukkan email akun untuk menerima kode OTP.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = onEmailChanged,
                    label = { Text("Email") },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ================= OTP =================
            ForgotPasswordStep.VERIFY -> {

                Text(
                    text = "Masukkan kode OTP yang dikirim ke email.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = uiState.email,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(20.dp))

                OtpInput(
                    otp = uiState.code,
                    onOtpChange = onCodeChanged
                )
            }

            // ================= RESET PASSWORD =================
            ForgotPasswordStep.RESET -> {

                Text(
                    text = "Masukkan password baru untuk akun kamu.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = uiState.newPassword,
                    onValueChange = onNewPasswordChanged,
                    label = { Text("Password Baru") },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.confirmPassword,
                    onValueChange = onConfirmPasswordChanged,
                    label = { Text("Konfirmasi Password") },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ================= DONE =================
            ForgotPasswordStep.DONE -> {

                Text(
                    text = "Password berhasil diubah. Silakan login kembali.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // ================= ERROR =================
        if (!uiState.errorMessage.isNullOrBlank()) {

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // ================= SUCCESS =================
        if (!uiState.successMessage.isNullOrBlank()) {

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = uiState.successMessage,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                when (uiState.step) {
                    ForgotPasswordStep.REQUEST -> onRequestReset()
                    ForgotPasswordStep.VERIFY -> onVerifyCode()
                    ForgotPasswordStep.RESET -> onResetPassword()
                    ForgotPasswordStep.DONE -> onBackToLogin()
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {

            if (uiState.isLoading) {

                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp
                )

            } else {

                Text(
                    when (uiState.step) {
                        ForgotPasswordStep.REQUEST -> "Kirim Kode OTP"
                        ForgotPasswordStep.VERIFY -> "Verifikasi Kode"
                        ForgotPasswordStep.RESET -> "Simpan Password Baru"
                        ForgotPasswordStep.DONE -> "Kembali ke Login"
                    }
                )

            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onBackToLogin,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kembali ke Login")
        }
    }
}


// ================= OTP INPUT =================

@Composable
fun OtpInput(
    otp: String,
    onOtpChange: (String) -> Unit
) {

    val focusRequesters = remember {
        List(6) { FocusRequester() }
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {

        repeat(6) { index ->

            OutlinedTextField(

                value = otp.getOrNull(index)?.toString() ?: "",

                onValueChange = { value ->

                    if (value.length <= 1 && value.all { it.isDigit() }) {

                        val otpChars = otp.padEnd(6, ' ')
                            .toCharArray()

                        otpChars[index] = value.firstOrNull() ?: ' '

                        val result = otpChars.joinToString("").trim()

                        onOtpChange(result)

                        if (value.isNotEmpty() && index < 5) {
                            focusRequesters[index + 1].requestFocus()
                        }

                    }

                },

                singleLine = true,

                textStyle = MaterialTheme.typography.titleLarge,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),

                modifier = Modifier
                    .width(48.dp)
                    .focusRequester(focusRequesters[index])
                    .onKeyEvent { event ->

                        if (
                            event.type == KeyEventType.KeyDown &&
                            event.key == Key.Backspace &&
                            otp.getOrNull(index) == null &&
                            index > 0
                        ) {

                            focusRequesters[index - 1].requestFocus()
                            true

                        } else false

                    }

            )
        }
    }
}