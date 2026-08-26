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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabDivider
import com.sipos.kebabsk.ui.theme.KebabErrorBg
import com.sipos.kebabsk.ui.theme.KebabErrorIconBg
import com.sipos.kebabsk.ui.theme.KebabErrorText
import com.sipos.kebabsk.ui.theme.KebabIconBg
import com.sipos.kebabsk.ui.theme.KebabInputBg
import com.sipos.kebabsk.ui.theme.KebabItemBg
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    displayName: String,
    email: String,
    username: String,
    role: String?,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onLogout: () -> Unit,
    onEditProfile: () -> Unit = {},
    onChangePassword: () -> Unit = {},
    onViewDailyStock: () -> Unit = {},
    onViewOperationalExpense: () -> Unit = {},
    onConnectReceiptPrinter: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        indicator = {},
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(KebabBg)
        ) {
            // === TOP APP BAR ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Profil",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KebabTextDark
                    )
                    Text(
                        text = "Akun dan operasional kasir",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = KebabTextGray
                    )
                }

                IconButton(onClick = onEditProfile) {
                    Icon(
                        Icons.Outlined.AccountCircle,
                        contentDescription = "Edit profil",
                        tint = KebabPrimary
                    )
                }
            }

            // === SCROLLABLE CONTENT ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // === KARTU PROFIL ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, KebabDivider, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = KebabPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KebabTextDark,
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(KebabInputBg)
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = role?.uppercase() ?: "KASIR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KebabPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = username.ifBlank { email },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = KebabTextGray
                        )
                    }
                }
            }

            // === MENU OPERASIONAL ===
            MenuSection(title = "Operasional") {
                ProfilMenuItem(
                    title = "Stok Bahan Harian",
                    subtitle = "Pantau bahan dan tutup sesi stok",
                    icon = Icons.Default.Inventory,
                    onClick = onViewDailyStock
                )
                ProfilMenuItem(
                    title = "Pengeluaran Operasional",
                    subtitle = "Catat dan cek biaya outlet",
                    icon = Icons.Default.Payments,
                    onClick = onViewOperationalExpense
                )
            }



            // === MENU AKUN ===
            MenuSection(title = "Akun") {
                ProfilMenuItem(
                    title = "Printer Bluetooth",
                    subtitle = "Hubungkan printer struk",
                    icon = Icons.Default.Print,
                    iconTint = KebabTextGray,
                    onClick = onConnectReceiptPrinter
                )
                ProfilMenuItem(
                    title = "Ubah Sandi",
                    subtitle = "Perbarui password akun kasir",
                    icon = Icons.Default.LockReset,
                    iconTint = KebabTextGray,
                    onClick = onChangePassword
                )
                ProfilMenuItem(
                    title = "Keluar",
                    subtitle = "Akhiri akses dari perangkat ini",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    iconTint = KebabErrorText,
                    iconBgColor = KebabErrorIconBg,
                    textColor = KebabErrorText,
                    containerColor = KebabErrorBg,
                    showChevron = false,
                    onClick = onLogout
                )
            }

                Spacer(modifier = Modifier.height(116.dp))
            }
        }
    }
}

@Composable
private fun MenuSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = KebabTextGray,
            letterSpacing = 0.4.sp,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        content()
    }
}

@Composable
private fun ProfilMenuItem(
    title: String,
    subtitle: String? = null,
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
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .border(
                width = 1.dp,
                color = if (textColor == KebabErrorText) {
                    KebabErrorText.copy(alpha = 0.16f)
                } else {
                    KebabDivider
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ikon dalam lingkaran
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (textColor == KebabErrorText) KebabErrorText.copy(alpha = 0.72f) else KebabTextGray,
                    lineHeight = 16.sp
                )
            }
        }

        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = KebabTextGray.copy(alpha = 0.55f)
            )
        }
    }
}
