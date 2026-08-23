package com.sipos.kebabsk.feature.profile.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabDivider
import com.sipos.kebabsk.ui.theme.KebabErrorText
import com.sipos.kebabsk.ui.theme.KebabErrorIconBg
import com.sipos.kebabsk.ui.theme.KebabInputBg
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabSuccessBannerBg
import com.sipos.kebabsk.ui.theme.KebabSuccessBannerBorder
import com.sipos.kebabsk.ui.theme.KebabSuccessBannerIcon
import com.sipos.kebabsk.ui.theme.KebabSuccessBannerText
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray

@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    initialName: String,
    initialUsername: String,
    initialEmail: String,
    initialRole: String,
    isSaving: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onBack: () -> Unit,
    onSave: (name: String, username: String, email: String) -> Unit
) {
    val scrollState = rememberScrollState()

    var name by remember { mutableStateOf(initialName) }
    var username by remember { mutableStateOf(initialUsername) }
    var email by remember { mutableStateOf(initialEmail) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KebabBg)
    ) {
        // === TOP BAR ===
        EditProfilTopBar(onBack = onBack)

        // === SCROLLABLE CONTENT ===
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- STATUS BANNERS ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!errorMessage.isNullOrBlank()) {
                    StatusBanner(
                        icon = Icons.Default.Error,
                        text = errorMessage,
                        bgColor = KebabErrorIconBg,
                        borderColor = KebabErrorText.copy(alpha = 0.3f),
                        contentColor = KebabErrorText,
                        iconColor = KebabErrorText
                    )
                }
                if (!successMessage.isNullOrBlank()) {
                    StatusBanner(
                        icon = Icons.Default.CheckCircle,
                        text = successMessage,
                        bgColor = KebabSuccessBannerBg,
                        borderColor = KebabSuccessBannerBorder,
                        contentColor = KebabSuccessBannerText,
                        iconColor = KebabSuccessBannerIcon
                    )
                }
            }

            // --- AVATAR SECTION ---
            ProfileAvatarSection(
                displayName = name,
                role = initialRole
            )

            // --- FORM CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, KebabDivider),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    EditInputField(
                        label = "Nama Lengkap",
                        value = name,
                        onValueChange = { name = it },
                        icon = Icons.Default.Person
                    )

                    EditInputField(
                        label = "Username",
                        value = username,
                        onValueChange = { username = it },
                        icon = Icons.Default.Badge
                    )

                    EditInputField(
                        label = "Alamat Email",
                        value = email,
                        onValueChange = { email = it },
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email
                    )
                }
            }

            // --- SAVE BUTTON ---
            SaveProfileButton(
                isSaving = isSaving,
                onClick = { onSave(name.trim(), username.trim(), email.trim()) }
            )

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

// === TOP BAR ===
@Composable
private fun EditProfilTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KebabBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
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

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Edit Profil",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = KebabTextDark
            )
            Text(
                text = "Perbarui informasi akun",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = KebabTextGray
            )
        }

        Spacer(modifier = Modifier.width(48.dp))
    }
}

// === STATUS BANNER ===
@Composable
private fun StatusBanner(
    icon: ImageVector,
    text: String,
    bgColor: Color,
    borderColor: Color,
    contentColor: Color,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

// === AVATAR ===
@Composable
private fun ProfileAvatarSection(displayName: String, role: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KebabDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(KebabInputBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = KebabPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName.ifBlank { "Pengguna" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KebabTextDark,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(KebabPrimary.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = role.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = KebabPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Identitas akun kasir",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = KebabTextGray
                    )
                }
            }
        }
    }
}

// === INPUT FIELD ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = KebabTextDark
            )
        }

        // Input
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            leadingIcon = {
                Icon(icon, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(20.dp))
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = KebabInputBg,
                focusedContainerColor = KebabInputBg,
                unfocusedIndicatorColor = KebabDivider,
                focusedIndicatorColor = KebabPrimary,
                errorIndicatorColor = KebabErrorText,
                errorContainerColor = KebabInputBg,
                focusedTextColor = KebabTextDark,
                unfocusedTextColor = KebabTextDark,
                cursorColor = KebabPrimary
            ),
            singleLine = true
        )
    }
}

// === SAVE BUTTON ===
@Composable
private fun SaveProfileButton(isSaving: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(KebabPrimary.copy(alpha = if (isSaving) 0.58f else 1f))
            .then(if (isSaving) Modifier else Modifier.clickable { onClick() }),
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
                    text = "Simpan Profil",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
