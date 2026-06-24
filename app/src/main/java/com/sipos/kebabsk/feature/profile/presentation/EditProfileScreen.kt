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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.sipos.kebabsk.ui.theme.KebabPrimaryContainer
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- STATUS BANNERS ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                displayName = initialName,
                role = initialRole
            )

            // --- FORM CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
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

            Spacer(modifier = Modifier.height(116.dp))
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
            .padding(horizontal = 20.dp, vertical = 14.dp),
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
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = KebabPrimary
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Edit Profil",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KebabTextDark
            )
            Text(
                text = "Perbarui informasi akun",
                fontSize = 12.sp,
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

// === AVATAR ===
@Composable
private fun ProfileAvatarSection(displayName: String, role: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color.White, Color(0xFFFFF8F2))))
            .border(1.dp, Color.White, RoundedCornerShape(28.dp))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .background(Brush.linearGradient(listOf(KebabPrimary, KebabPrimaryContainer))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .shadow(4.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.06f))
                    .clip(CircleShape)
                    .background(KebabPrimary)
                    .clickable { /* Ubah Foto */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Foto",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KebabTextDark,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(KebabPrimary.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = role.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KebabPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Data ini digunakan untuk identitas kasir.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = KebabTextGray,
                lineHeight = 16.sp
            )
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
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
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
                .height(62.dp),
            leadingIcon = {
                Icon(icon, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(22.dp))
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(18.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = KebabInputBg,
                focusedContainerColor = KebabInputBg,
                unfocusedIndicatorColor = KebabDivider.copy(alpha = 0.55f),
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
            .height(58.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = Color.Black.copy(alpha = 0.06f))
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(KebabPrimary, KebabPrimaryContainer)))
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
                    fontSize = 17.sp
                )
            }
        }
    }
}
