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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.VisualTransformation
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
        containerColor = Color(0xFFFFF9F4),
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        topBar = { TopBarSecurityCheck(onBack = { onBackToLogin() }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
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
                Spacer(modifier = Modifier.height(18.dp))

                StepperSecurityCheck(currentStep = uiState.step)

                Spacer(modifier = Modifier.height(22.dp))

                when (uiState.step) {
                    ForgotPasswordStep.REQUEST -> {
                        RecoveryContentCard {
                            ForgotStepHeader(
                                icon = Icons.Default.Email,
                                title = "Temukan akun",
                                description = "Masukkan alamat email yang terdaftar untuk menerima kode OTP."
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Alamat email",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KebabTextDark,
                                    modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
                                )

                                OutlinedTextField(
                                    value = uiState.email,
                                    onValueChange = onEmailChanged,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(58.dp),
                                    placeholder = {
                                        Text(
                                            "contoh: kasir@skkebab.my.id",
                                            color = KebabTextGray.copy(alpha = 0.58f)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Email,
                                            contentDescription = null,
                                            tint = KebabPrimary
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = recoveryFieldColors(),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = KebabTextDark,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    singleLine = true,
                                    enabled = !uiState.isLoading
                                )
                            }
                        }
                    }

                    ForgotPasswordStep.VERIFY -> {
                        RecoveryContentCard {
                            ForgotStepHeader(
                                icon = Icons.Default.Email,
                                title = "Masukkan kode OTP",
                                description = "Kode 6 digit telah dikirim untuk akun ${uiState.email.ifBlank { "Anda" }}."
                            )

                            Spacer(modifier = Modifier.height(26.dp))

                            Text(
                                text = "Kode verifikasi",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = KebabTextDark,
                                modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
                            )
                            OtpInputRow(
                                otpValue = uiState.code,
                                onOtpValueChange = onCodeChanged
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 18.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(KebabPrimary.copy(alpha = 0.07f))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = KebabPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Kode hanya dapat digunakan satu kali.",
                                    color = KebabTextGray,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    ForgotPasswordStep.RESET -> {
                        RecoveryContentCard {
                            ForgotStepHeader(
                                icon = Icons.Default.Lock,
                                title = "Buat kata sandi baru",
                                description = "Gunakan kata sandi yang mudah Anda ingat dan sulit ditebak."
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            ForgotPasswordInput(
                                value = uiState.newPassword,
                                onValueChange = onNewPasswordChanged,
                                label = "Kata sandi baru",
                                placeholder = "Minimal 6 karakter",
                                enabled = !uiState.isLoading
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ForgotPasswordInput(
                                value = uiState.confirmPassword,
                                onValueChange = onConfirmPasswordChanged,
                                label = "Konfirmasi kata sandi",
                                placeholder = "Ketik ulang kata sandi",
                                enabled = !uiState.isLoading
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 18.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF6F0EA))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = KebabPrimary,
                                    modifier = Modifier
                                        .padding(top = 1.dp)
                                        .size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Gunakan minimal 6 karakter dan hindari kata sandi yang mudah ditebak.",
                                    color = KebabTextGray,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    ForgotPasswordStep.DONE -> {
                        RecoveryContentCard {
                            ForgotStepHeader(
                                icon = Icons.Default.Check,
                                title = "Kata sandi diperbarui",
                                description = "Silakan masuk kembali menggunakan kata sandi baru Anda.",
                                success = true
                            )
                        }
                    }
                }

                if (!uiState.errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    StatusMessageBox(
                        text = uiState.errorMessage,
                        containerColor = Color(0xFFFFEDEA),
                        textColor = MaterialTheme.colorScheme.error
                    )
                }

                if (!uiState.successMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    StatusMessageBox(
                        text = uiState.successMessage,
                        containerColor = KebabSuccessBg,
                        textColor = KebabSuccess
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, KebabBg, KebabBg)
                        )
                    )
                    .padding(top = 10.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            6.dp,
                            RoundedCornerShape(16.dp),
                            spotColor = KebabPrimaryContainer.copy(alpha = 0.45f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (uiState.isLoading) Brush.horizontalGradient(listOf(KebabPrimary.copy(alpha=0.6f), KebabPrimaryContainer.copy(alpha=0.6f))) else Brush.horizontalGradient(listOf(KebabPrimary, KebabPrimaryContainer)))
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            text = when (uiState.step) {
                                ForgotPasswordStep.REQUEST -> if (uiState.isLoading) "Mengirim OTP..." else "Kirim Kode OTP"
                                ForgotPasswordStep.VERIFY -> if (uiState.isLoading) "Memverifikasi..." else "Verifikasi Kode"
                                ForgotPasswordStep.RESET -> if (uiState.isLoading) "Menyimpan..." else "Perbarui Kata Sandi"
                                ForgotPasswordStep.DONE -> "Kembali ke Login"
                            },
                            color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                        )
                        if (!uiState.isLoading) {
                            Spacer(modifier = Modifier.width(8.dp))
                            if (uiState.step != ForgotPasswordStep.DONE) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                if (uiState.step != ForgotPasswordStep.DONE) {
                    TextButton(
                        onClick = onBackToLogin,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Kembali ke login",
                            color = KebabTextGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
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
            .background(Color(0xFFFFF9F4))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, KebabPrimary.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = KebabPrimary)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Pemulihan Akun",
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KebabTextDark
            )
            Text(
                text = "Verifikasi keamanan",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = KebabTextGray
            )
        }
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun ForgotStepHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    success: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (success) KebabSuccessBg else KebabPrimary.copy(alpha = 0.10f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (success) KebabSuccess else KebabPrimary,
                modifier = Modifier.size(27.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KebabTextDark,
                lineHeight = 25.sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                description,
                fontSize = 13.sp,
                color = KebabTextGray,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun RecoveryContentCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = KebabPrimary.copy(alpha = 0.06f),
                spotColor = KebabPrimary.copy(alpha = 0.10f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFF1E5DA),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 20.dp, vertical = 22.dp),
        content = content
    )
}

@Composable
private fun ForgotPasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    enabled: Boolean
) {
    var passwordVisible by remember(value.isEmpty()) { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = KebabTextDark,
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = KebabTextGray.copy(alpha = 0.62f)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = KebabPrimary) },
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (passwordVisible) {
                            "Sembunyikan kata sandi"
                        } else {
                            "Tampilkan kata sandi"
                        },
                        tint = KebabTextGray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(16.dp),
            colors = recoveryFieldColors(),
            textStyle = androidx.compose.ui.text.TextStyle(color = KebabTextDark, fontSize = 16.sp, fontWeight = FontWeight.Medium),
            singleLine = true,
            enabled = enabled
        )
    }
}

@Composable
private fun recoveryFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color(0xFFFAF6F2),
    disabledContainerColor = Color(0xFFF5F0EC),
    focusedBorderColor = KebabPrimary,
    unfocusedBorderColor = Color(0xFFE8D8CB),
    cursorColor = KebabPrimary
)

@Composable
private fun StatusMessageBox(
    text: String,
    containerColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(1.dp, Color(0xFFF1E5DA), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Garis latar belakang
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).align(Alignment.Center)) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = if (stepIndex >= 2) KebabPrimary else KebabDivider, thickness = 2.dp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = if (stepIndex >= 3) KebabPrimary else KebabDivider, thickness = 2.dp)
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background(Color(0xFFFFFCFA))
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(boxBg)
                .border(4.dp, Color(0xFFFFFCFA), CircleShape),
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
        onValueChange = { newValue ->
            onOtpValueChange(newValue.filter { it.isDigit() }.take(6))
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(6) { index ->
                    val char = when {
                        index >= otpValue.length -> ""
                        else -> otpValue[index].toString()
                    }
                    val isFocused = index == otpValue.length
                    
                    OtpBox(
                        char = char,
                        isFocused = isFocused,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}

@Composable
fun OtpBox(
    char: String,
    isFocused: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isFocused) KebabInputActiveBg else KebabInputBg
    val borderColor = if (isFocused) KebabPrimary else KebabDivider.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(if (isFocused) 2.dp else 1.dp, borderColor, RoundedCornerShape(14.dp)),
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
