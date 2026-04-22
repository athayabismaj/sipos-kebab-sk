package com.sipos.kebabsk.feature.profile.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabCardBg
import com.sipos.kebabsk.ui.theme.KebabErrorBg
import com.sipos.kebabsk.ui.theme.KebabErrorIconBg
import com.sipos.kebabsk.ui.theme.KebabErrorText
import com.sipos.kebabsk.ui.theme.KebabIconBg
import com.sipos.kebabsk.ui.theme.KebabItemBg
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    displayName: String,
    email: String,
    username: String,
    role: String?,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onViewRevenue: () -> Unit = {},
    onViewDailyStock: () -> Unit = {},
    onViewOperationalExpense: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KebabBg)
    ) {
        // === TOP APP BAR ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(KebabBg)
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onEditProfile) {
                Icon(
                    Icons.Outlined.AccountCircle,
                    contentDescription = "Akun",
                    tint = KebabPrimary
                )
            }

            Text(
                text = "Profil Kasir",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KebabPrimary
            )

            IconButton(onClick = onLogout) {
                Icon(
                    Icons.Outlined.Logout,
                    contentDescription = "Keluar",
                    tint = KebabErrorText
                )
            }
        }

        // === SCROLLABLE CONTENT ===
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // === KARTU PROFIL ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color.Black.copy(alpha = 0.06f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(KebabCardBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(Color.White, CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00658F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = displayName,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = KebabTextDark
                    )
                    Text(
                        text = role?.replaceFirstChar { it.uppercase() } ?: "Kasir",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = KebabTextGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // === MENU OPERASIONAL ===
            MenuSection(title = "Operasional") {
                ProfilMenuItem(
                    title = "Ringkasan Penjualan",
                    icon = Icons.Default.TrendingUp,
                    onClick = onViewRevenue
                )
                ProfilMenuItem(
                    title = "Stok Bahan Harian",
                    icon = Icons.Default.Inventory,
                    onClick = onViewDailyStock
                )
                ProfilMenuItem(
                    title = "Pengeluaran Operasional",
                    icon = Icons.Default.Payments,
                    onClick = onViewOperationalExpense
                )
            }

            // === MENU AKUN ===
            MenuSection(title = "Akun") {
                ProfilMenuItem(
                    title = "Ubah Sandi",
                    icon = Icons.Default.LockReset,
                    iconTint = KebabTextGray,
                    onClick = onChangePassword
                )
                ProfilMenuItem(
                    title = "Keluar / Logout",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    iconTint = KebabErrorText,
                    iconBgColor = KebabErrorIconBg,
                    textColor = KebabErrorText,
                    containerColor = KebabErrorBg,
                    showChevron = false,
                    onClick = onLogout
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MenuSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = KebabTextGray,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        content()
    }
}

@Composable
private fun ProfilMenuItem(
    title: String,
    icon: ImageVector,
    iconTint: Color = KebabPrimary,
    iconBgColor: Color = KebabIconBg,
    textColor: Color = KebabTextDark,
    containerColor: Color = KebabItemBg,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ikon dalam lingkaran
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFDDC1AE)
            )
        }
    }
}
