package com.sipos.kebabsk.feature.profile.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabDivider
import com.sipos.kebabsk.ui.theme.KebabErrorText
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabPrimaryContainer
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val KebabBarLight = Color(0xFFFFB74D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevenueSummaryScreen(
    modifier: Modifier = Modifier,
    uiState: RevenueUiState,
    onDateChanged: (LocalDate) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Hari Ini") }

    val isToday = uiState.selectedDate == AppTime.todayJakarta()
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"))

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = AppTime.toEpochMillisAtStartOfDay(uiState.selectedDate)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateChanged(AppTime.dateFromEpochMillis(millis))
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KebabBg)
    ) {
        // === TOP BAR ===
        RingkasanTopBar(onBack = onBack, onRefresh = onRefresh)

        // === SCROLLABLE CONTENT ===
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- FILTER & DATE ---
            FilterSection(
                selectedFilter = selectedFilter,
                currentDate = uiState.selectedDate,
                dateFormatter = dateFormatter,
                onFilterSelected = { filter ->
                    selectedFilter = filter
                    val today = AppTime.todayJakarta()
                    when (filter) {
                        "Hari Ini" -> onDateChanged(today)
                        "Kemarin" -> onDateChanged(today.minusDays(1))
                    }
                },
                onDateClick = { showDatePicker = true }
            )

            // --- LOADING / ERROR / CONTENT ---
            when {
                uiState.isLoading -> {
                    RevenueSummarySkeleton()
                }
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KebabErrorText.copy(alpha = 0.05f))
                            .border(1.dp, KebabErrorText.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = KebabErrorText,
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    // --- METRIC CARDS ---
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        MetricSummaryCard(
                            title = "TOTAL PENDAPATAN",
                            value = formatter.format(uiState.revenueAmount),
                            trend = if (isToday) "Hari ini" else uiState.selectedDate.format(dateFormatter),
                            icon = Icons.Outlined.Payments,
                            valueColor = KebabPrimary
                        )
                        MetricSummaryCard(
                            title = "JUMLAH TRANSAKSI",
                            value = "${uiState.transactionCount}",
                            trend = if (uiState.transactionCount > 0) {
                                "Rata-rata ${formatter.format(
                                    if (uiState.transactionCount > 0) uiState.revenueAmount / uiState.transactionCount else 0.0
                                )}/order"
                            } else "Belum ada transaksi",
                            icon = Icons.Outlined.ReceiptLong,
                            valueColor = KebabTextDark
                        )
                    }

                    // --- CHART SECTION ---
                    ChartSection(trendData = uiState.trendData)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// === TOP BAR ===
@Composable
private fun RingkasanTopBar(onBack: () -> Unit, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KebabBg)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = KebabTextDark)
        }

        Text(
            text = "Ringkasan Penjualan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = KebabTextDark
        )

        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = KebabTextDark)
        }
    }
}

// === FILTER SECTION ===
@Composable
private fun FilterSection(
    selectedFilter: String,
    currentDate: LocalDate,
    dateFormatter: DateTimeFormatter,
    onFilterSelected: (String) -> Unit,
    onDateClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Filter
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFF8F3EE))
                .border(1.dp, KebabDivider, RoundedCornerShape(50))
                .padding(4.dp)
        ) {
            listOf("Hari Ini", "Kemarin", "Bulan Ini").forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) KebabPrimary else Color.Transparent)
                        .clickable { onFilterSelected(filter) }
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else KebabTextGray
                    )
                }
            }
        }

        // Date Display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onDateClick() }
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                tint = KebabPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = currentDate.format(dateFormatter),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = KebabTextDark
            )
        }
    }
}

// === METRIC SUMMARY CARD ===
@Composable
private fun MetricSummaryCard(
    title: String,
    value: String,
    trend: String,
    icon: ImageVector,
    valueColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Watermark Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = KebabDivider.copy(alpha = 0.3f),
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.TopEnd)
                .offset(x = 16.dp, y = (-8).dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = KebabTextGray,
                letterSpacing = 1.sp
            )
            Text(
                text = value,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = KebabPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trend,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = KebabPrimary
                )
            }
        }
    }
}

// === CHART SECTION ===
@Composable
private fun ChartSection(trendData: List<Pair<String, Double>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Grafik Tren Penjualan",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = KebabTextDark
            )
            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = KebabTextDark)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (trendData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada data tren", color = KebabTextGray, fontSize = 14.sp)
            }
        } else {
            // Bar Chart
            BarChartFromTrend(trendData = trendData)
        }
    }
}

// === BAR CHART ===
@Composable
private fun BarChartFromTrend(trendData: List<Pair<String, Double>>) {
    val maxRevenue = trendData.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1.0

    // Chart bars
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color(0xFFF8F3EE), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Grid lines
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalDivider(color = KebabDivider)
            HorizontalDivider(color = KebabDivider)
            HorizontalDivider(color = KebabDivider)
            HorizontalDivider(color = KebabDivider)
        }

        // Bars
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val highestIndex = trendData.indices.maxByOrNull { trendData[it].second } ?: -1
            trendData.forEachIndexed { index, (_, value) ->
                val percentage = (value / maxRevenue).toFloat().coerceIn(0.05f, 1f)
                val isHighest = index == highestIndex
                ChartBar(heightPercentage = percentage, isHighest = isHighest)
            }
        }
    }

    // X-axis labels
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        trendData.forEach { (dateStr, _) ->
            val label = try {
                LocalDate.parse(dateStr).format(
                    DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("id-ID"))
                )
            } catch (_: Exception) {
                dateStr.takeLast(5)
            }
            Text(text = label, fontSize = 10.sp, color = KebabTextGray)
        }
    }
}

@Composable
private fun ChartBar(heightPercentage: Float, isHighest: Boolean) {
    val barColor = if (isHighest) KebabPrimary else KebabBarLight

    Box(
        modifier = Modifier
            .width(24.dp)
            .fillMaxHeight(heightPercentage)
            .background(barColor, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
    )
}
