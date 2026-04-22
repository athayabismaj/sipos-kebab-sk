package com.sipos.kebabsk.feature.transactions.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionHistoryItem
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabCardBg
import com.sipos.kebabsk.ui.theme.KebabDateInactiveBg
import com.sipos.kebabsk.ui.theme.KebabDivider
import com.sipos.kebabsk.ui.theme.KebabErrorBg
import com.sipos.kebabsk.ui.theme.KebabErrorText
import com.sipos.kebabsk.ui.theme.KebabIconHighlight
import com.sipos.kebabsk.ui.theme.KebabItemBg
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabPrimaryContainer
import com.sipos.kebabsk.ui.theme.KebabSuccess
import com.sipos.kebabsk.ui.theme.KebabSuccessBg
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KebabBg)
    ) {
        // === TOP APP BAR ===
        TransactionTopAppBar(onCalendarClick = { showDatePicker = true })

        // === DATE PICKER DIALOG ===
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = AppTime.toEpochMillisAtStartOfDay(uiState.currentDate)
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.setDate(AppTime.dateFromEpochMillis(millis))
                        }
                        showDatePicker = false
                    }) { Text("Pilih") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // === SCROLLABLE CONTENT ===
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- DATE SCROLLER ---
            DateScroller(
                currentDate = uiState.currentDate,
                onDateSelected = { viewModel.setDate(it) }
            )

            if (uiState.isLoading) {
                TransactionsSkeleton(modifier = Modifier.weight(1f))
            } else {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // --- SUMMARY METRICS (real data) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SummaryMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Transaksi",
                            value = "${uiState.paginatedTransactions.size}",
                            icon = Icons.Outlined.ReceiptLong
                        )
                        SummaryMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Pendapatan",
                            value = formatShortRupiah(
                                uiState.paginatedTransactions
                                    .filter { it.status.equals("Sukses", ignoreCase = true) }
                                    .sumOf { it.total }
                            ),
                            icon = Icons.Outlined.Payments
                        )
                    }

                    // --- MAIN CONTENT ---
                    when {
                        uiState.errorMessage != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = uiState.errorMessage ?: "",
                                        color = KebabErrorText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(onClick = { viewModel.fetchTransactions() }) {
                                        Text("Coba Lagi")
                                    }
                                }
                            }
                        }
                        uiState.paginatedTransactions.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Outlined.ReceiptLong,
                                        contentDescription = null,
                                        tint = KebabTextGray.copy(alpha = 0.4f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Belum ada transaksi pada tanggal ini",
                                        color = KebabTextGray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.paginatedTransactions) { trx ->
                                    TransactionItemCard(trx = trx)
                                }

                                // Pagination
                                if (uiState.totalPages > 1) {
                                    item {
                                        PaginationControls(
                                            currentPage = uiState.currentPage,
                                            totalPages = uiState.totalPages,
                                            onPrevious = { viewModel.loadPreviousPage() },
                                            onNext = { viewModel.loadNextPage() }
                                        )
                                    }
                                }

                                item { Spacer(modifier = Modifier.height(16.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// === TOP APP BAR ===
@Composable
private fun TransactionTopAppBar(onCalendarClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KebabBg)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(48.dp))

        Text(
            text = "RIWAYAT TRANSAKSI",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = KebabPrimary,
            letterSpacing = 0.5.sp
        )

        IconButton(onClick = onCalendarClick) {
            Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = KebabPrimary)
        }
    }
}

// === DATE SCROLLER ===
@Composable
private fun DateScroller(currentDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val today = AppTime.todayJakarta()
    val dates = (0..6).map { today.minusDays(it.toLong()) }
    val horizontalScroll = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScroll),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        dates.forEach { date ->
            val isActive = date == currentDate
            val dayLabel = when (date) {
                today -> "Hari ini"
                today.minusDays(1) -> "Kemarin"
                else -> date.format(DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("id-ID")))
            }
            val dateNum = date.dayOfMonth.toString()
            val month = date.format(DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("id-ID")))

            DateCard(
                label = dayLabel,
                date = dateNum,
                month = month,
                isActive = isActive,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
private fun DateCard(label: String, date: String, month: String, isActive: Boolean, onClick: () -> Unit) {
    val textColor = if (isActive) Color.White else KebabTextDark
    val alphaText = if (isActive) 0.8f else 0.5f

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = if (isActive) Brush.linearGradient(listOf(KebabPrimary, KebabPrimaryContainer))
                else Brush.linearGradient(listOf(KebabDateInactiveBg, KebabDateInactiveBg))
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .widthIn(min = 50.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = alphaText))
            Text(text = date, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
            Text(text = month, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = textColor.copy(alpha = alphaText))
        }
    }
}

// === SUMMARY METRIC CARD ===
@Composable
private fun SummaryMetricCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(KebabCardBg)
            .padding(16.dp)
    ) {
        // Faded watermark icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = KebabPrimary.copy(alpha = 0.05f),
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 16.dp, y = 16.dp)
        )

        Column {
            Text(text = title, fontSize = 12.sp, color = KebabTextGray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = KebabTextDark)
        }
    }
}

// === TRANSACTION ITEM CARD ===
@Composable
private fun TransactionItemCard(trx: TransactionHistoryItem) {
    val formatRupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
    val isCancelled = !trx.status.equals("Sukses", ignoreCase = true)
    val isSuccess = trx.status.equals("Sukses", ignoreCase = true)
    val opacity = if (isCancelled) 0.6f else 1f
    val textDecoration = if (isCancelled) TextDecoration.LineThrough else null
    val badgeBg = if (isSuccess) KebabSuccessBg else KebabErrorBg
    val badgeText = if (isSuccess) KebabSuccess else KebabErrorText
    val iconBgColor = if (isCancelled) Color(0xFFDED9D4) else KebabIconHighlight.copy(alpha = 0.2f)
    val iconTint = if (isCancelled) KebabTextGray else KebabPrimaryContainer

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(KebabItemBg.copy(alpha = opacity))
            .border(1.dp, KebabDivider.copy(alpha = 0.3f * opacity), RoundedCornerShape(16.dp))
            .clickable { /* detail transaksi */ }
            .padding(16.dp)
    ) {
        // Top: icon + info + badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Pukul ${trx.time}",
                        fontSize = 12.sp,
                        color = KebabTextGray.copy(alpha = opacity)
                    )
                    Text(
                        text = trx.code,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KebabTextDark.copy(alpha = opacity),
                        textDecoration = textDecoration
                    )
                }
            }

            // Status badge
            Surface(
                color = badgeBg.copy(alpha = opacity),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = trx.status.uppercase(),
                    color = badgeText.copy(alpha = opacity),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = KebabDivider.copy(alpha = 0.3f * opacity))
        Spacer(modifier = Modifier.height(12.dp))

        // Bottom: item count + amount
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Payments,
                    contentDescription = null,
                    tint = KebabTextGray.copy(alpha = opacity),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${trx.itemCount} Item",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = KebabTextGray.copy(alpha = opacity)
                )
            }
            Text(
                text = formatRupiah.format(trx.total),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCancelled) KebabTextGray.copy(alpha = opacity) else KebabTextDark,
                textDecoration = textDecoration
            )
        }
    }
}

// === PAGINATION ===
@Composable
private fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = currentPage > 1,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("Sebelumnya")
        }

        Text(
            text = "Hal $currentPage/$totalPages",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = KebabTextGray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        OutlinedButton(
            onClick = onNext,
            enabled = currentPage < totalPages,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("Selanjutnya")
        }
    }
}

// === HELPER FUNCTIONS ===
private fun formatShortRupiah(amount: Double): String {
    return when {
        amount >= 1_000_000 -> "Rp ${String.format(Locale.US, "%.1f", amount / 1_000_000)}jt"
        amount >= 1_000 -> "Rp ${String.format(Locale.US, "%.0f", amount / 1_000)}k"
        else -> "Rp ${String.format(Locale.US, "%.0f", amount)}"
    }
}
