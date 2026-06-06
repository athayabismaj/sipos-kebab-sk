package com.sipos.kebabsk.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Egg
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.RiceBowl
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabCardBg
import com.sipos.kebabsk.ui.theme.KebabDivider
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabPrimaryContainer
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

val KebabSecondary = Color(0xFF795900)
val KebabTertiary = Color(0xFF00658F)

@Composable
fun DailyStockScreen(
    modifier: Modifier = Modifier,
    items: List<DailyStockItem>,
    sessionId: Long?,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onForceLogout: () -> Unit = {},
    onCloseSession: () -> Unit,
    isCashReconciliationPending: Boolean = false,
    onSessionAlreadyClosed: () -> Unit = {}
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale.forLanguageTag("id-ID"))
    val hariIni = AppTime.todayJakarta().format(dateFormatter)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KebabBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // === TOP BAR ===
            StokTopBar(onBack = onBack, onRetry = onRetry, isLoading = isLoading)

            // === MAIN SCROLLABLE CONTENT ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Tanggal / Sesi
                Text(
                    text = "Sesi Aktif",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = KebabTextGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hariIni,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KebabTextDark
                )

                Spacer(modifier = Modifier.height(24.dp))

                when {
                    isLoading -> {
                        DailyStockSkeleton(modifier = Modifier.weight(1f))
                    }

                    isCashReconciliationPending -> {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFF856404),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Formulir Stok Terkunci.\nAnda belum menyelesaikan rekonsiliasi kas harian.",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF856404),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            Spacer(modifier = Modifier.height(24.dp))
                            androidx.compose.material3.Button(
                                onClick = onSessionAlreadyClosed,
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFE0A800)),
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Sesi Sudah Ditutup\nKembali ke Login",
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold, 
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    !errorMessage.isNullOrBlank() -> {
                        val isSessionExpired = errorMessage.contains("Sesi login sudah berakhir", ignoreCase = true)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                OutlinedButton(onClick = if (isSessionExpired) onForceLogout else onRetry) {
                                    Text(if (isSessionExpired) "Login Ulang" else "Coba Lagi")
                                }
                            }
                        }
                    }

                    items.isEmpty() -> {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    tint = KebabPrimary
                                )
                                Text(
                                    text = "Belum ada data stok bahan harian yang dibawa.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    else -> {
                        // Daftar Stok Bahan
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(items) { index, item ->
                                // Rotasi icon/warna biar visually appealing & sesuai mockup
                                val (icon, color) = getIconAndColorForIndex(index)
                                
                                StokItemCard(
                                    title = item.name,
                                    stokAwal = formatQty(item.qty),
                                    sisaAktual = formatQty(item.remainingQty ?: item.qty),
                                    unit = item.unit ?: "Unit",
                                    icon = icon,
                                    iconColor = color
                                )
                            }
                            
                            // Spacer akhir list scrollable
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }
            }
        }

        // --- FLOATING ACTION BUTTON (Bawah) ---
        if (sessionId != null && items.isNotEmpty() && !isLoading && errorMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, KebabBg, KebabBg)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = KebabPrimaryContainer)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(KebabPrimary, KebabPrimaryContainer)))
                        .clickable { onCloseSession() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Input Sisa & Tutup Sesi",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StokTopBar(onBack: () -> Unit, onRetry: () -> Unit, isLoading: Boolean) {
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
            text = "Stok Bahan",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = KebabPrimary
        )

        IconButton(
            onClick = onRetry,
            enabled = !isLoading
        ) {
            Icon(
                Icons.Default.Refresh, 
                contentDescription = "Refresh", 
                tint = if (isLoading) KebabPrimary.copy(alpha = 0.5f) else KebabPrimary
            )
        }
    }
}

@Composable
private fun StokItemCard(
    title: String,
    stokAwal: String,
    sisaAktual: String,
    unit: String,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KebabCardBg),
        border = BorderStroke(1.dp, KebabDivider),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lingkaran Ikon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Kolom Tengah (Judul & Sisa)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = KebabTextDark,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sisa: ", 
                        fontSize = 13.sp, 
                        color = KebabTextGray,
                        maxLines = 1
                    )
                    Text(
                        text = "$sisaAktual $unit", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold,
                        color = KebabTextDark,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Info Stok Awal (Kanan)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "STOK AWAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KebabTextGray,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = stokAwal,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KebabPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = KebabTextGray,
                        modifier = Modifier.padding(bottom = 2.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun getIconAndColorForIndex(index: Int): Pair<ImageVector, Color> {
    val icons = listOf(
        Icons.Outlined.Restaurant to KebabPrimary,
        Icons.Outlined.RiceBowl to KebabSecondary,
        Icons.Outlined.LocalPizza to KebabTertiary,
        Icons.Outlined.Egg to KebabPrimary
    )
    return icons[index % icons.size]
}

private fun formatQty(qty: Double): String {
    return if ((qty % 1.0).absoluteValue < 0.000001) {
        qty.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.2f", qty)
    }
}
