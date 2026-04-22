package com.sipos.kebabsk.feature.auth.presentation.forgotpassword

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.ui.theme.*

// Tambahan warna lokal yang dibutuhkan desain baru
val KebabStepInactiveBg = Color(0xFFE7E1DD)
val KebabYellowActive = Color(0xFFFFBF00)
val KebabInputActiveBg = Color(0xFFFFFFFF)

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
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = KebabBg,
        topBar = { TopBarSecurityCheck(onBack = { onBackToLogin() }) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // --- STEPPER ---
                StepperSecurityCheck(currentStep = uiState.step)

                Spacer(modifier = Modifier.height(48.dp))

                when (uiState.step) {
                    ForgotPasswordStep.REQUEST -> {
                        // --- HEADER ICON & TEXT ---
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(KebabCardBg)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(40.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Lupa Kata Sandi?", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = KebabTextDark, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Masukkan email atau username yang terdaftar. Kami akan mengirimkan kode OTP untuk mengatur ulang kata sandi Anda.",
                            fontSize = 14.sp, color = KebabTextGray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp), lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        // --- FORM INPUT ---
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("EMAIL / USERNAME", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KebabTextDark, letterSpacing = 1.sp)
                                Icon(Icons.Default.Info, contentDescription = null, tint = KebabTextGray, modifier = Modifier.size(16.dp))
                            }

                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = onEmailChanged,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("contoh@kebabulos.com", color = KebabTextGray.copy(alpha = 0.5f)) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = KebabTextGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = KebabInputBg,
                                    unfocusedContainerColor = KebabInputBg,
                                    focusedBorderColor = KebabPrimary,
                                    unfocusedBorderColor = KebabDivider,
                                ),
                                singleLine = true,
                                enabled = !uiState.isLoading
                            )
                        }
                    }

                    ForgotPasswordStep.VERIFY -> {
                        Text("Verifikasi OTP", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = KebabTextDark, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Kode telah dikirim ke email anda", fontSize = 14.sp, color = KebabTextGray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(48.dp))

                        OtpInputRow(
                            otpValue = uiState.code,
                            onOtpValueChange = onCodeChanged
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                        Text("Kirim ulang kode (59s)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KebabPrimary, modifier = Modifier.clip(RoundedCornerShape(50)).clickable { }.padding(horizontal = 16.dp, vertical = 8.dp))
                    }

                    ForgotPasswordStep.RESET -> {
                        Text("Reset Kata Sandi", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = KebabTextDark, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Masukkan password baru Anda", fontSize = 14.sp, color = KebabTextGray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(48.dp))

                        OutlinedTextField(
                            value = uiState.newPassword,
                            onValueChange = onNewPasswordChanged,
                            label = { Text("Password Baru") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = KebabInputBg,
                                unfocusedContainerColor = KebabInputBg,
                                focusedBorderColor = KebabPrimary,
                                unfocusedBorderColor = KebabDivider,
                            ),
                            singleLine = true,
                            enabled = !uiState.isLoading
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = uiState.confirmPassword,
                            onValueChange = onConfirmPasswordChanged,
                            label = { Text("Konfirmasi Password Baru") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = KebabInputBg,
                                unfocusedContainerColor = KebabInputBg,
                                focusedBorderColor = KebabPrimary,
                                unfocusedBorderColor = KebabDivider,
                            ),
                            singleLine = true,
                            enabled = !uiState.isLoading
                        )
                    }

                    ForgotPasswordStep.DONE -> {
                        Box(
                            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(24.dp)).background(KebabPrimary.copy(alpha = 0.1f)).padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(40.dp))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Selesai!", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = KebabTextDark, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Password berhasil diubah. Silakan login kembali dengan password baru Anda.", fontSize = 14.sp, color = KebabTextGray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp), lineHeight = 20.sp)
                    }
                }

                if (!uiState.errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = uiState.errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }

                if (!uiState.successMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = uiState.successMessage, color = KebabSuccess, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // --- BOTTOM ACTIONS ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = KebabPrimaryContainer)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (uiState.isLoading) Brush.horizontalGradient(listOf(Color.Gray, Color.LightGray)) else Brush.horizontalGradient(listOf(KebabPrimary, KebabPrimaryContainer)))
                        .clickable(enabled = !uiState.isLoading) {
                            when (uiState.step) {
                                ForgotPasswordStep.REQUEST -> onRequestReset()
                                ForgotPasswordStep.VERIFY -> onVerifyCode()
                                ForgotPasswordStep.RESET -> onResetPassword()
                                ForgotPasswordStep.DONE -> onBackToLogin()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (uiState.step) {
                                    ForgotPasswordStep.REQUEST -> "Kirim Kode OTP"
                                    ForgotPasswordStep.VERIFY -> "Verifikasi"
                                    ForgotPasswordStep.RESET -> "Simpan Kata Sandi"
                                    ForgotPasswordStep.DONE -> "Kembali ke Login"
                                },
                                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (uiState.step != ForgotPasswordStep.DONE) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // Back to Login Button (Hide on DONE)
                if (uiState.step != ForgotPasswordStep.DONE) {
                    TextButton(
                        onClick = onBackToLogin,
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text(text = "Kembali ke Login", color = KebabTextDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TopBarSecurityCheck(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KebabBg)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = KebabPrimary)
        }
        Text(
            text = "Security Check",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = KebabPrimary
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
fun StepperSecurityCheck(currentStep: ForgotPasswordStep) {
    val stepIndex = when (currentStep) {
        ForgotPasswordStep.REQUEST -> 1
        ForgotPasswordStep.VERIFY -> 2
        ForgotPasswordStep.RESET -> 3
        ForgotPasswordStep.DONE -> 4
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Garis latar belakang
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).align(Alignment.Center)) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = if (stepIndex >= 2) KebabPrimary else KebabDivider, thickness = 3.dp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = if (stepIndex >= 3) KebabPrimary else KebabDivider, thickness = 3.dp)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Step 1: Email
            StepIconIndicator(
                number = "1", label = "EMAIL",
                status = if (stepIndex > 1) StepStatus.COMPLETED else StepStatus.ACTIVE
            )
            
            // Step 2: OTP
            StepIconIndicator(
                number = "2", label = "OTP",
                status = if (stepIndex > 2) StepStatus.COMPLETED else if (stepIndex == 2) StepStatus.ACTIVE else StepStatus.INACTIVE
            )
            
            // Step 3: Reset
            StepIconIndicator(
                number = "3", label = "RESET",
                status = if (stepIndex > 3) StepStatus.COMPLETED else if (stepIndex == 3) StepStatus.ACTIVE else StepStatus.INACTIVE
            )
        }
    }
}

enum class StepStatus { INACTIVE, ACTIVE, COMPLETED }

@Composable
fun StepIconIndicator(number: String, label: String, status: StepStatus) {
    val boxBg = when (status) {
        StepStatus.COMPLETED -> KebabPrimary
        StepStatus.ACTIVE -> if (number == "1") KebabPrimary else KebabYellowActive
        StepStatus.INACTIVE -> KebabBg
    }
    
    val textColor = when (status) {
        StepStatus.COMPLETED -> Color.White
        StepStatus.ACTIVE -> if (number == "1") Color.White else KebabTextDark
        StepStatus.INACTIVE -> KebabTextGray.copy(alpha = 0.6f)
    }

    val labelColor = when (status) {
        StepStatus.COMPLETED -> KebabPrimary
        StepStatus.ACTIVE -> KebabTextDark
        StepStatus.INACTIVE -> KebabTextGray.copy(alpha = 0.6f)
    }

    val labelWeight = if (status == StepStatus.INACTIVE) FontWeight.Medium else FontWeight.ExtraBold

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.background(KebabBg)) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(boxBg)
                .border(4.dp, KebabBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (status == StepStatus.COMPLETED) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                if (status == StepStatus.INACTIVE) {
                    Box(modifier = Modifier.matchParentSize().background(KebabDivider))
                    Text(text = number, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                } else {
                    Text(text = number, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = labelWeight, color = labelColor, letterSpacing = 1.sp)
    }
}

@Composable
fun OtpInputRow(otpValue: String, onOtpValueChange: (String) -> Unit) {
    BasicTextField(
        value = otpValue,
        onValueChange = onOtpValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(6) { index ->
                    val char = when {
                        index >= otpValue.length -> ""
                        else -> otpValue[index].toString()
                    }
                    val isFocused = index == otpValue.length
                    
                    OtpBox(char = char, isFocused = isFocused)
                }
            }
        }
    )
}

@Composable
fun OtpBox(char: String, isFocused: Boolean) {
    val bgColor = if (isFocused) KebabInputActiveBg else KebabInputBg
    val borderColor = if (isFocused) KebabPrimary else Color.Transparent

    Box(
        modifier = Modifier
            .width(46.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(if (isFocused) 2.dp else 0.dp, borderColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = KebabTextDark,
            textAlign = TextAlign.Center
        )
        
        if (isFocused && char.isEmpty()) {
            Box(modifier = Modifier.height(24.dp).width(2.dp).background(KebabPrimary))
        }
    }
}