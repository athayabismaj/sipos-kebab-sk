package com.sipos.kebabsk.feature.dashboard.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.R
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.common.MoneyUtils
import com.sipos.kebabsk.feature.shift.presentation.ShiftSummaryUiState
import com.sipos.kebabsk.ui.theme.BrandOrange
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabPrimaryContainer
import com.sipos.kebabsk.ui.theme.KebabSecondaryContainer
import com.sipos.kebabsk.ui.theme.KebabSuccess
import com.sipos.kebabsk.ui.theme.KebabSuccessBg
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    cashierName: String,
    cashierRole: String,
    isDailySessionOpen: Boolean,
    dailySessionLabel: String?,
    dailyTargetRevenue: Long?,
    shiftSummaryUiState: ShiftSummaryUiState,
    onRetryShiftSummary: () -> Unit,
    onForceLogout: () -> Unit,
    onStartTransaction: () -> Unit,
    isPendingSync: Boolean = false
) {
    val scrollState = rememberScrollState()
    var currentTime by remember { mutableStateOf(AppTime.nowJakartaDateTime()) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("id-ID")) }
    val firstName = cashierName.split(" ").firstOrNull().orEmpty().ifBlank { "Kasir" }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = AppTime.nowJakartaDateTime()
            delay(1000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.kebab_sk_logo),
                            contentDescription = "Logo Kebab SK",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Kebab SK",
                            color = KebabPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Dashboard kasir",
                            color = KebabTextGray,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White)
                        .border(1.dp, KebabPrimary.copy(alpha = 0.08f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Hari ini",
                        color = KebabPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            UserInfoSection(
                cashierName = firstName,
                cashierRole = cashierRole,
                currentTime = currentTime.format(timeFormatter),
                isDailySessionOpen = isDailySessionOpen,
                dailySessionLabel = dailySessionLabel
            )

            if (isPendingSync) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KebabSecondaryContainer.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = KebabPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Menunggu Sinkronisasi â€” verifikasi sesi tertunda (offline)",
                        fontSize = 12.sp,
                        color = KebabPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when {
                shiftSummaryUiState.isLoading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Transaksi",
                            value = "...",
                            subValue = "Memuat",
                            icon = Icons.AutoMirrored.Outlined.List,
                            compact = true
                        )
                        DashboardMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Item Terjual",
                            value = "...",
                            subValue = "Memuat",
                            icon = Icons.Outlined.ShoppingCart,
                            compact = true
                        )
                    }
                    DashboardMetricCard(
                        title = "Pendapatan Hari Ini",
                        value = "...",
                        subValue = "Memuat data shift",
                        icon = Icons.AutoMirrored.Outlined.List,
                        isPrimary = true
                    )
                }

                !shiftSummaryUiState.errorMessage.isNullOrBlank() -> {
                    val isSessionExpired = shiftSummaryUiState.errorMessage.contains("Sesi login sudah berakhir", ignoreCase = true)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8E8E8))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = shiftSummaryUiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = if (isSessionExpired) onForceLogout else onRetryShiftSummary,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KebabPrimary)
                            ) {
                                Text(if (isSessionExpired) "Login Ulang" else "Coba Lagi", color = Color.White)
                            }
                        }
                    }
                }

                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Transaksi",
                            value = shiftSummaryUiState.totalTransactions.toString(),
                            subValue = shiftSummaryUiState.transactionGrowthPercentage?.let { pct ->
                                val sign = if (pct >= 0) "+" else ""
                                "${sign}${pct}%"
                            } ?: if (shiftSummaryUiState.totalTransactions > 0) "Transaksi hari ini" else "Belum ada",
                            icon = Icons.AutoMirrored.Outlined.List,
                            compact = true
                        )
                        DashboardMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Item Terjual",
                            value = shiftSummaryUiState.totalItemsSold.toString(),
                            subValue = shiftSummaryUiState.dominantItemName?.let { "Top: $it" }
                                ?: if (shiftSummaryUiState.totalItemsSold > 0) "Item terjual" else "Belum ada",
                            icon = Icons.Outlined.ShoppingCart,
                            compact = true
                        )
                    }
                    DashboardMetricCard(
                        title = "Pendapatan Hari Ini",
                        value = MoneyUtils.formatRupiah(shiftSummaryUiState.totalRevenue),
                        subValue = run {
                            val target = shiftSummaryUiState.dailyTargetRevenue
                                ?.takeIf { it > 0.0 }
                                ?: dailyTargetRevenue?.takeIf { it > 0.0 }
                            val percentage = when {
                                target != null -> (shiftSummaryUiState.totalRevenue / target) * 100.0
                                shiftSummaryUiState.revenueTargetPercentage != null -> shiftSummaryUiState.revenueTargetPercentage
                                else -> 0.0
                            }
                            "Target harian: ${String.format("%.1f", percentage)}% tercapai"
                        },
                        icon = Icons.AutoMirrored.Outlined.List,
                        isPrimary = true
                    )
                }
            }

            MainActionButton(
                enabled = isDailySessionOpen,
                onClick = onStartTransaction
            )

            if (!isDailySessionOpen) {
                ClosedSessionNotice(dailySessionLabel = dailySessionLabel)
            }

            Spacer(modifier = Modifier.height(104.dp))
        }
    }
}

@Composable
private fun ClosedSessionNotice(
    dailySessionLabel: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7EA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BrandOrange.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BrandOrange.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = KebabPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = dailySessionLabel?.takeIf { it.isNotBlank() } ?: "Sesi Harian Belum Dibuka",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = KebabTextDark
                )
                Text(
                    text = "Transaksi akan aktif setelah admin membuka sesi harian.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KebabTextGray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun UserInfoSection(
    cashierName: String,
    cashierRole: String,
    currentTime: String,
    isDailySessionOpen: Boolean,
    dailySessionLabel: String?
) {
    val statusLabel = when {
        isDailySessionOpen -> dailySessionLabel ?: "Sesi Harian Aktif"
        !dailySessionLabel.isNullOrBlank() -> dailySessionLabel
        else -> "Sesi Harian Belum Dibuka"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFFF9800), Color(0xFFFFD08A))))
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Halo, $cashierName", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = Color(0xFF1E1E1E))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFE9DDCE))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cashierRole.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = KebabPrimary
                            )
                        }
                        Text("$currentTime WIB", color = KebabTextGray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDailySessionOpen) KebabSuccessBg else Color(0xFFF9E8E8))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = statusLabel,
                    color = if (isDailySessionOpen) KebabSuccess else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subValue: String,
    icon: ImageVector,
    isPrimary: Boolean = false,
    compact: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) 132.dp else 148.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) Color.White else Color(0xFFF3EDE8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPrimary) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(if (compact) 16.dp else 20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    title,
                    color = KebabTextDark.copy(alpha = 0.78f),
                    fontSize = if (compact) 13.sp else 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 17.sp,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(if (compact) 34.dp else 36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KebabPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(20.dp))
                }
            }

            Column {
                Text(
                    text = value,
                    fontSize = when {
                        isPrimary -> 32.sp
                        compact -> 34.sp
                        else -> 40.sp
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isPrimary) KebabPrimary else Color.Black
                )
                if (subValue.isNotEmpty()) {
                    Text(
                        subValue,
                        color = KebabTextGray,
                        fontSize = if (compact) 12.sp else 13.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MainActionButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(KebabPrimary, KebabPrimaryContainer)))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Mulai Transaksi Baru", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                        Text("Buka kasir dan pilih menu", fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
