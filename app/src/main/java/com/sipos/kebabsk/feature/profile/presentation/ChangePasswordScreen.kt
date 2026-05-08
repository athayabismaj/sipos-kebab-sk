package com.sipos.kebabsk.feature.profile.presentation

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabCardBg
import com.sipos.kebabsk.ui.theme.KebabDivider
import com.sipos.kebabsk.ui.theme.KebabErrorText
import com.sipos.kebabsk.ui.theme.KebabInputBg
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabPrimaryContainer
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray

@Composable
fun ChangePasswordScreen(
    modifier: Modifier = Modifier,
    isSaving: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onBack: () -> Unit,
    onSave: (currentPassword: String, newPassword: String, confirmPassword: String) -> Unit
) {
    val scrollState = rememberScrollState()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isCurrentPwdVisible by remember { mutableStateOf(false) }
    var isNewPwdVisible by remember { mutableStateOf(false) }
    var isConfirmPwdVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KebabBg)
    ) {
        // === TOP BAR ===
        SecurityTopBar(onBack = onBack)

        // === SCROLLABLE CONTENT ===
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- HEADER & BANNERS ---
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Success Banner
                if (!successMessage.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KebabPrimary.copy(alpha = 0.05f))
                            .border(1.dp, KebabPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = KebabPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = successMessage,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KebabTextDark
                        )
                    }
                }

                // Error Banner
                if (!errorMessage.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KebabErrorText.copy(alpha = 0.05f))
                            .border(1.dp, KebabErrorText.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = KebabErrorText,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = KebabErrorText
                        )
                    }
                }

                Text(
                    text = "Ubah Sandi",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = KebabTextDark
                )
                Text(
                    text = "Pastikan akun Kebab SK Anda tetap aman dengan memperbarui kata sandi secara berkala.",
                    fontSize = 14.sp,
                    color = KebabTextGray,
                    lineHeight = 20.sp
                )
            }

            // --- FORM CARD ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, KebabDivider.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KebabCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Current Password
                    PasswordInputField(
                        label = "KATA SANDI SAAT INI",
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        placeholder = "Masukkan sandi saat ini",
                        isVisible = isCurrentPwdVisible,
                        onVisibilityChange = { isCurrentPwdVisible = !isCurrentPwdVisible }
                    )

                    HorizontalDivider(
                        color = KebabDivider.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // New Password
                    PasswordInputField(
                        label = "KATA SANDI BARU",
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        placeholder = "Masukkan sandi baru",
                        isVisible = isNewPwdVisible,
                        onVisibilityChange = { isNewPwdVisible = !isNewPwdVisible }
                    )

                    // Confirm Password
                    PasswordInputField(
                        label = "KONFIRMASI KATA SANDI BARU",
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Ketik ulang sandi baru",
                        isVisible = isConfirmPwdVisible,
                        onVisibilityChange = { isConfirmPwdVisible = !isConfirmPwdVisible }
                    )

                    // Info Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(KebabDivider.copy(alpha = 0.2f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = KebabPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Kata sandi baru harus terdiri dari minimal 8 karakter, mengandung setidaknya satu huruf besar, satu huruf kecil, dan satu angka.",
                            fontSize = 12.sp,
                            color = KebabTextGray,
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                8.dp,
                                RoundedCornerShape(12.dp),
                                spotColor = Color.Black.copy(alpha = 0.06f)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(KebabPrimary, KebabPrimaryContainer)
                                )
                            )
                            .then(
                                if (isSaving) Modifier
                                else Modifier.clickable {
                                    onSave(currentPassword, newPassword, confirmPassword)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Save,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Simpan Perubahan",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// === TOP BAR ===
@Composable
private fun SecurityTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KebabBg)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = KebabPrimary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Security",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = KebabPrimary
            )
            Icon(
                Icons.Default.Info,
                contentDescription = "Info",
                tint = KebabPrimary.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(48.dp))
    }
}

// === PASSWORD FIELD ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isVisible: Boolean,
    onVisibilityChange: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Label with help icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = KebabTextGray,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = "Info",
                tint = KebabPrimary.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
        }

        // Input Field
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    placeholder,
                    color = KebabTextGray.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            },
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onVisibilityChange) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password Visibility",
                        tint = KebabTextGray
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = KebabInputBg,
                focusedContainerColor = KebabInputBg,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = KebabPrimary,
                errorIndicatorColor = KebabErrorText,
                errorContainerColor = KebabInputBg
            ),
            singleLine = true
        )
    }
}