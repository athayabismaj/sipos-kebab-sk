package com.sipos.kebabsk.feature.transactions.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.DeleteForever

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.sipos.kebabsk.R
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.feature.profile.presentation.BluetoothPrinterConnection
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionHistoryItem
import com.sipos.kebabsk.common.MoneyUtils
import com.sipos.kebabsk.common.ThermalTextFormatter
import com.sipos.kebabsk.common.ThermalPrinterPreferences
import com.sipos.kebabsk.common.TransactionCodeFormatter
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionReceipt
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionReceiptItem
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabDateInactiveBg
import com.sipos.kebabsk.ui.theme.KebabDivider
import com.sipos.kebabsk.ui.theme.KebabErrorBg
import com.sipos.kebabsk.ui.theme.KebabErrorText
import com.sipos.kebabsk.ui.theme.KebabItemBg
import com.sipos.kebabsk.ui.theme.KebabInputBg
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabPrimaryContainer
import com.sipos.kebabsk.ui.theme.KebabSuccess
import com.sipos.kebabsk.ui.theme.KebabSuccessBg
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    modifier: Modifier = Modifier,
    sessionId: Long? = null,
    onForceLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var transactionToVoid by rememberSaveable { mutableStateOf<Long?>(null) }
    var isPrintingReceipt by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isSelectedDateToday = uiState.currentDate == AppTime.todayJakarta()

    LaunchedEffect(isSelectedDateToday) {
        if (!isSelectedDateToday) {
            transactionToVoid = null
        }
    }

    LaunchedEffect(uiState.voidSuccess, uiState.voidErrorMessage) {
        if (uiState.voidSuccess) {
            transactionToVoid = null
            Toast.makeText(context, uiState.voidMessage ?: "Transaksi dibatalkan", Toast.LENGTH_SHORT).show()
            viewModel.clearVoidState()
            viewModel.fetchTransactions()
        } else if (!uiState.voidErrorMessage.isNullOrBlank()) {
            Toast.makeText(context, uiState.voidErrorMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearVoidState()
        }
    }

    if (uiState.receiptTransactionId != null) {
        ReceiptReprintDialog(
            isLoading = uiState.isLoadingReceipt,
            receipt = uiState.receipt,
            errorMessage = uiState.receiptErrorMessage,
            isPrinting = isPrintingReceipt,
            onDismiss = {
                isPrintingReceipt = false
                viewModel.dismissReceipt()
            },
            onRetry = {
                uiState.receiptTransactionId?.let(viewModel::openReceipt)
            },
            onPrint = { receipt ->
                if (!receipt.isDetailed) {
                    Toast.makeText(
                        context,
                        "Detail produk belum lengkap. Struk belum bisa dicetak ulang.",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (!BluetoothPrinterConnection.isConnected) {
                    Toast.makeText(
                        context,
                        "Printer Bluetooth belum tersambung. Hubungkan dari Profil > Printer Bluetooth.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    coroutineScope.launch {
                        isPrintingReceipt = true
                        val paperSize = ThermalPrinterPreferences.loadPaperSize(context)
                        val result = BluetoothPrinterConnection.print(
                            buildReceiptEscPosBytes(receipt, paperSize.charactersPerLine)
                        )
                        isPrintingReceipt = false
                        Toast.makeText(
                            context,
                            if (result.isSuccess) "Struk berhasil dicetak ulang." else result.exceptionOrNull()?.message
                                ?: "Gagal mencetak struk. Periksa printer.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    if (transactionToVoid != null && isSelectedDateToday) {
        VoidTransactionDialog(
            isLoading = uiState.isVoiding,
            onDismiss = { transactionToVoid = null },
            onRestock = {
                sessionId?.let { viewModel.voidTransaction(transactionToVoid!!, VoidReason.RESTOCK, it) }
            },
            onWaste = {
                sessionId?.let { viewModel.voidTransaction(transactionToVoid!!, VoidReason.WASTE, it) }
            }
        )
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = viewModel::fetchTransactions,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 132.dp)
            ) {
            // --- DATE SCROLLER ---
            item {
                DateScroller(
                    currentDate = uiState.currentDate,
                    onDateSelected = { viewModel.setDate(it) }
                )
            }

            // --- SUMMARY METRICS ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryMetricCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        title = "Transaksi",
                        value = "${uiState.totalTransactionsCount}",
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong
                    )
                    SummaryMetricCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        title = "Pendapatan",
                        value = formatShortRupiah(uiState.totalRevenue),
                        icon = Icons.Outlined.Payments
                    )
                }
            }

            // --- LIST CONTENT ---
            when {
                uiState.isLoading -> {
                    item {
                        TransactionsSkeleton(modifier = Modifier.fillMaxWidth())
                    }
                }
                uiState.errorMessage != null -> {
                    item {
                        val isSessionExpired = uiState.errorMessage?.contains("Sesi login sudah berakhir", ignoreCase = true) == true
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(KebabErrorBg)
                                .padding(20.dp),
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
                                OutlinedButton(onClick = { if (isSessionExpired) onForceLogout() else viewModel.fetchTransactions() }) {
                                    Text(if (isSessionExpired) "Login Ulang" else "Coba Lagi")
                                }
                            }
                        }
                    }
                }
                uiState.paginatedTransactions.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White)
                                .border(1.dp, KebabDivider.copy(alpha = 0.24f), RoundedCornerShape(24.dp))
                                .padding(vertical = 36.dp, horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    tint = KebabTextGray.copy(alpha = 0.4f),
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Belum ada transaksi",
                                    color = KebabTextGray,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "pada tanggal ini",
                                    color = KebabTextGray.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
                else -> {
                    items(uiState.paginatedTransactions) { trx ->
                        TransactionItemCard(
                            trx = trx,
                            isVoidable = isSelectedDateToday,
                            onVoidClicked = {
                                if (isSelectedDateToday) {
                                    transactionToVoid = trx.id
                                }
                            },
                            onReceiptClicked = { viewModel.openReceipt(trx) }
                        )
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
                }
            }
        }
    }
}

}

@Composable
private fun VoidTransactionDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onRestock: () -> Unit,
    onWaste: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 18.dp,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .widthIn(max = 400.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = KebabErrorBg,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = KebabErrorText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Batalkan transaksi",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KebabTextDark
                        )
                        Text(
                            text = "Pilih perlakuan stok untuk transaksi ini.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = KebabTextGray
                        )
                    }
                }

                if (isLoading) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = KebabInputBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = KebabPrimary,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Memproses pembatalan",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KebabTextDark
                                )
                                Text(
                                    text = "Mohon tunggu sebentar",
                                    fontSize = 11.sp,
                                    color = KebabTextGray
                                )
                            }
                        }
                    }
                } else {
                    VoidActionOption(
                        icon = Icons.Default.Recycling,
                        title = "Kembalikan ke stok",
                        subtitle = "Bahan masih tersedia dan dapat digunakan kembali.",
                        accentColor = KebabSuccess,
                        accentBackground = KebabSuccessBg,
                        onClick = onRestock
                    )
                    VoidActionOption(
                        icon = Icons.Default.DeleteForever,
                        title = "Catat sebagai waste",
                        subtitle = "Bahan sudah terpakai, rusak, atau tidak layak.",
                        accentColor = KebabErrorText,
                        accentBackground = KebabErrorBg,
                        onClick = onWaste
                    )
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(13.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KebabDivider)
                    ) {
                        Text(
                            text = "Tutup",
                            color = KebabTextDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoidActionOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    accentBackground: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, KebabDivider),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = accentBackground,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KebabTextDark
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = KebabTextGray
                )
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
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.transactions_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KebabTextDark
            )
            Text(
                text = stringResource(R.string.transactions_subtitle),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = KebabTextGray
            )
        }

        IconButton(
            onClick = onCalendarClick,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, KebabPrimary.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.transactions_select_date), tint = KebabPrimary)
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dates.forEach { date ->
            val isActive = date == currentDate
            val dayLabel = when (date) {
                today -> stringResource(R.string.transactions_today)
                today.minusDays(1) -> stringResource(R.string.transactions_yesterday)
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
    val subAlpha = if (isActive) 0.85f else 0.55f

    Box(
        modifier = Modifier
            .height(76.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = if (isActive) Brush.linearGradient(listOf(KebabPrimary, KebabPrimaryContainer))
                else Brush.linearGradient(listOf(Color.White, KebabDateInactiveBg.copy(alpha = 0.45f)))
            )
            .border(
                1.dp,
                if (isActive) Color.Transparent else KebabDivider.copy(alpha = 0.35f),
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .widthIn(min = 66.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label.uppercase(),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = subAlpha),
                letterSpacing = 0.5.sp
            )
            Text(
                text = date,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            Text(
                text = month,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = subAlpha)
            )
        }
    }
}

// === SUMMARY METRIC CARD ===
@Composable
private fun SummaryMetricCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector) {
    Box(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, KebabDivider.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // Faded watermark icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = KebabPrimary.copy(alpha = 0.08f),
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 16.dp, y = 16.dp)
        )

        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                color = KebabTextGray,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KebabTextDark
            )
        }
    }
}

// === TRANSACTION ITEM CARD ===
@Composable
private fun TransactionItemCard(
    trx: TransactionHistoryItem,
    isVoidable: Boolean,
    onVoidClicked: () -> Unit,
    onReceiptClicked: () -> Unit
) {
    val printReceiptDescription = stringResource(R.string.cd_print_receipt)
    val isSuccess = trx.status.equals("Sukses", ignoreCase = true) ||
        trx.status.equals("Success", ignoreCase = true) ||
        trx.status.equals("Lunas", ignoreCase = true) ||
        trx.status.equals("Paid", ignoreCase = true)
    val isCancelled = !isSuccess
    val opacity = if (isCancelled) 0.62f else 1f
    val textDecoration = if (isCancelled) TextDecoration.LineThrough else null
    val badgeBg = if (isSuccess) KebabSuccessBg else KebabErrorBg
    val badgeText = if (isSuccess) KebabSuccess else KebabErrorText

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, KebabDivider, RoundedCornerShape(18.dp))
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = TransactionCodeFormatter.formatForDisplay(trx.code),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KebabTextDark.copy(alpha = opacity),
                    textDecoration = textDecoration,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.transactions_time, trx.time),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = KebabTextGray.copy(alpha = opacity)
                )
            }
            Surface(
                color = badgeBg.copy(alpha = opacity),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = receiptStatusDisplay(trx.status),
                    color = badgeText.copy(alpha = opacity),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    maxLines = 1
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Total pembayaran",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = KebabTextGray.copy(alpha = opacity)
                )
                Text(
                    text = MoneyUtils.formatRupiah(trx.total),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCancelled) KebabTextGray.copy(alpha = opacity) else KebabTextDark,
                    textDecoration = textDecoration
                )
            }
            Surface(
                color = KebabInputBg,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = KebabTextGray.copy(alpha = opacity),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = pluralStringResource(R.plurals.item_count, trx.itemCount, trx.itemCount),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KebabTextGray.copy(alpha = opacity)
                    )
                }
            }
        }

        HorizontalDivider(color = KebabDivider)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSuccess && isVoidable) {
                Row(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(11.dp))
                        .background(KebabErrorBg)
                        .clickable { onVoidClicked() }
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = KebabErrorText,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Batalkan",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = KebabErrorText
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Row(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(11.dp))
                    .background(KebabPrimary.copy(alpha = 0.1f))
                    .semantics { contentDescription = printReceiptDescription }
                    .clickable { onReceiptClicked() }
                    .padding(horizontal = 11.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = null,
                    tint = KebabPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = stringResource(R.string.transactions_print_short),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = KebabPrimary
                )
            }
        }
    }
}

@Composable
private fun ReceiptReprintDialog(
    isLoading: Boolean,
    receipt: TransactionReceipt?,
    errorMessage: String?,
    isPrinting: Boolean,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onPrint: (TransactionReceipt) -> Unit
) {
    val receiptLoadingState = stringResource(R.string.transactions_receipt_loading_state)

    Dialog(
        onDismissRequest = { if (!isPrinting) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isPrinting,
            dismissOnClickOutside = !isPrinting,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 18.dp,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .widthIn(max = 410.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = KebabInputBg,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null,
                                tint = KebabPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Cetak ulang struk",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KebabTextDark
                        )
                        Text(
                            text = receipt?.let(::receiptDisplayCode) ?: "Memuat detail transaksi",
                            fontSize = 10.sp,
                            color = KebabTextGray,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    receipt?.let {
                        Text(
                            text = MoneyUtils.formatRupiah(it.totalAmount),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KebabTextDark
                        )
                    }
                }

                when {
                    isLoading -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = KebabInputBg,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    stateDescription = receiptLoadingState
                                    liveRegion = LiveRegionMode.Polite
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = KebabPrimary,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = stringResource(R.string.transactions_loading_receipt),
                                        color = KebabTextDark,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.transactions_loading_wait),
                                        color = KebabTextGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    !errorMessage.isNullOrBlank() -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = KebabErrorBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, KebabErrorText.copy(alpha = 0.16f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { liveRegion = LiveRegionMode.Assertive }
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.transactions_receipt_unavailable),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KebabErrorText
                                )
                                Text(
                                    text = errorMessage,
                                    fontSize = 11.sp,
                                    color = KebabErrorText.copy(alpha = 0.78f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(13.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, KebabDivider)
                            ) {
                                Text(stringResource(R.string.action_close), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onRetry,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(13.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KebabPrimary)
                            ) {
                                Text(stringResource(R.string.action_retry), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    receipt != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 440.dp)
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            ReceiptPreviewCard(receipt = receipt)
                        }

                        ReceiptPrinterInfo(receipt = receipt)

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = onDismiss,
                                enabled = !isPrinting,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(13.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, KebabDivider)
                            ) {
                                Text(stringResource(R.string.action_close), fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onPrint(receipt) },
                                enabled = !isPrinting && receipt.isDetailed,
                                modifier = Modifier.weight(1.35f).height(48.dp),
                                shape = RoundedCornerShape(13.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KebabPrimary)
                            ) {
                                if (isPrinting) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Print,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = if (isPrinting) {
                                        stringResource(R.string.action_printing)
                                    } else {
                                        stringResource(R.string.action_print_receipt)
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptPreviewCard(receipt: TransactionReceipt) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 352.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ReceiptDarkBg)
            .border(1.dp, KebabDivider, RoundedCornerShape(16.dp))
            .padding(horizontal = 17.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.sk_receipt_logo),
                contentDescription = "Logo Kebab SK",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "KEBAB SK",
                color = ReceiptDarkText,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.ExtraBold
            )
            receipt.branchAddress?.trim()?.takeIf { it.isNotEmpty() }?.let { address ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = address,
                    color = ReceiptDarkMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        DashedReceiptDivider()

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ReceiptPreviewRow(label = "No.", value = receiptDisplayCode(receipt))
            ReceiptPreviewRow(label = "Kasir", value = receipt.cashierName)
        }

        if (receipt.items.isEmpty()) {
            MissingReceiptItemsNotice()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                receipt.items.forEachIndexed { index, item ->
                    ReceiptPreviewItem(index = index + 1, item = item)
                }
            }
        }

        DashedReceiptDivider()

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            ReceiptPreviewRow(label = "Total QTY", value = receipt.items.sumOf { it.qty }.toString())
        }

        DashedReceiptDivider()

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            ReceiptPreviewRow(label = "Sub Total", value = MoneyUtils.formatRupiah(receipt.totalAmount))
            ReceiptPreviewRow(label = "Total", value = MoneyUtils.formatRupiah(receipt.totalAmount), isBold = true)
            ReceiptPreviewRow(label = "Bayar", value = MoneyUtils.formatRupiah(receipt.paidAmount))
            ReceiptPreviewRow(
                label = "Kembalian",
                value = MoneyUtils.formatRupiah(receipt.changeAmount),
                highlight = true
            )
        }

        DashedReceiptDivider()

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "Terima kasih telah berbelanja",
                color = ReceiptDarkMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = receipt.createdAtLabel,
                color = ReceiptDarkMuted.copy(alpha = 0.82f),
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DashedReceiptDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(24) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(ReceiptDarkDash.copy(alpha = 0.55f))
            )
        }
    }
}

@Composable
private fun MissingReceiptItemsNotice() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = KebabErrorBg.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, KebabErrorText.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Detail produk belum lengkap",
                color = KebabErrorText,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Cetak ulang ditahan sampai nama menu, jumlah, dan harga item tersedia.",
                color = KebabErrorText.copy(alpha = 0.78f),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun ReceiptPrinterInfo(receipt: TransactionReceipt) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (BluetoothPrinterConnection.isConnected) {
            KebabSuccessBg
        } else {
            KebabInputBg
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(9.dp),
                color = if (BluetoothPrinterConnection.isConnected) {
                    KebabSuccess.copy(alpha = 0.14f)
                } else {
                    KebabPrimary.copy(alpha = 0.10f)
                },
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = if (BluetoothPrinterConnection.isConnected) KebabSuccess else KebabPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = receiptPrintHelperText(receipt),
                fontSize = 11.sp,
                color = if (BluetoothPrinterConnection.isConnected) KebabSuccess else KebabTextGray,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 15.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ReceiptPreviewItem(index: Int, item: TransactionReceiptItem) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "$index. ${item.name}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ReceiptDarkText
                )
                item.variantName?.takeIf { it.isNotBlank() && !it.equals("Default", ignoreCase = true) }?.let {
                    Text(
                        text = "  $it",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = ReceiptDarkMuted
                    )
                }
            }
            Text(
                text = MoneyUtils.formatRupiah(item.subtotal),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ReceiptDarkText
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "  ${item.qty} x ${MoneyUtils.formatRupiah(item.price)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = ReceiptDarkMuted
            )
            Text(
                text = "",
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ReceiptPreviewRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.42f),
            fontFamily = FontFamily.Monospace,
            fontSize = if (isBold) 14.sp else 13.sp,
            color = if (isBold) ReceiptDarkText else ReceiptDarkMuted,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.58f),
            fontFamily = FontFamily.Monospace,
            fontSize = if (isBold) 14.sp else 13.sp,
            color = if (highlight) ReceiptDarkText else ReceiptDarkText,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
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
    val previousPageDescription = stringResource(R.string.cd_previous_page)
    val nextPageDescription = stringResource(R.string.cd_next_page)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev button
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (currentPage > 1) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
                .semantics { contentDescription = previousPageDescription }
                .clickable(enabled = currentPage > 1) { onPrevious() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = if (currentPage > 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }

        // Page indicator pills
        Spacer(modifier = Modifier.width(12.dp))
        repeat(totalPages) { index ->
            val page = index + 1
            val isCurrentPage = page == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isCurrentPage) KebabPrimary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { if (isCurrentPage.not()) { if (page < currentPage) onPrevious() else onNext() } }
                    .then(
                        if (isCurrentPage) Modifier.width(20.dp).height(8.dp)
                        else Modifier.size(8.dp)
                    )
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        // Next button
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (currentPage < totalPages) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
                .semantics { contentDescription = nextPageDescription }
                .clickable(enabled = currentPage < totalPages) { onNext() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (currentPage < totalPages) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// === HELPER FUNCTIONS ===
private fun buildReceiptEscPosBytes(
    receipt: TransactionReceipt,
    charactersPerLine: Int
): ByteArray {
    val printerWidth = charactersPerLine.coerceAtLeast(16)
    val charset = Charset.forName("CP437")
    val buffer = ByteArrayOutputStream()

    fun command(vararg bytes: Int) {
        buffer.write(bytes.map { it.toByte() }.toByteArray())
    }

    fun text(value: String = "") {
        buffer.write(value.toByteArray(charset))
        buffer.write('\n'.code)
    }

    fun align(mode: Int) = command(0x1B, 0x61, mode)
    fun bold(enabled: Boolean) = command(0x1B, 0x45, if (enabled) 1 else 0)
    fun size(mode: Int) = command(0x1D, 0x21, mode)
    fun line() = text("-".repeat(printerWidth))

    command(0x1B, 0x40)
    align(1)
    bold(true)
    size(0x11)
    text("KEBAB SK")
    size(0x00)
    bold(false)
    receipt.branchAddress
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?.let { address ->
            ThermalTextFormatter.wrap(address, printerWidth).forEach(::text)
        }
    line()
    text(receiptColumns("No.", receiptDisplayCode(receipt).take(20), printerWidth))
    text("Kasir")
    text(receipt.cashierName.take(printerWidth))
    align(0)
    line()

    receipt.items.forEachIndexed { index, item ->
        val variantName = item.variantName
            ?.takeIf { it.isNotBlank() && !it.equals("Default", ignoreCase = true) }

        bold(true)
        text("${index + 1}. ${item.name}".take(printerWidth))
        bold(false)
        variantName?.let { text("   ${it}".take(printerWidth)) }
        text(receiptColumns("   ${item.qty} x ${MoneyUtils.formatRupiah(item.price)}", MoneyUtils.formatRupiah(item.subtotal), printerWidth))
    }

    line()
    text(receiptColumns("Total QTY", receipt.items.sumOf { it.qty }.toString(), printerWidth))
    line()
    text(receiptColumns("Sub Total", MoneyUtils.formatRupiah(receipt.totalAmount), printerWidth))
    bold(true)
    text(receiptColumns("Total", MoneyUtils.formatRupiah(receipt.totalAmount), printerWidth))
    bold(false)
    text(receiptColumns("Bayar", MoneyUtils.formatRupiah(receipt.paidAmount), printerWidth))
    text(receiptColumns("Kembali", MoneyUtils.formatRupiah(receipt.changeAmount), printerWidth))
    align(1)
    line()
    bold(true)
    text("Terimakasih Telah Berbelanja")
    bold(false)
    text(receipt.createdAtLabel.take(printerWidth))
    text()
    text()
    command(0x1D, 0x56, 0x42, 0x00)

    return buffer.toByteArray()
}

private fun receiptColumns(left: String, right: String, printerWidth: Int): String {
    val safeLeft = left.take(printerWidth)
    val safeRight = right.take(printerWidth)
    val spaces = (printerWidth - safeLeft.length - safeRight.length).coerceAtLeast(1)
    return safeLeft + " ".repeat(spaces) + safeRight
}


private fun receiptStatusDisplay(status: String): String {
    return when (status.lowercase(Locale.US)) {
        "success", "sukses", "paid", "lunas" -> "LUNAS"
        "void", "cancel", "canceled", "cancelled", "batal", "dibatalkan" -> "DIBATALKAN"
        "waste", "hangus" -> "HANGUS"
        "return", "returned", "retur" -> "RETUR"
        else -> status.uppercase(Locale.US)
    }
}

private fun receiptDisplayCode(receipt: TransactionReceipt): String {
    val originalCode = receipt.code.trim().takeIf { it.isNotBlank() } ?: "TRX-${receipt.id}"
    return TransactionCodeFormatter.formatForDisplay(originalCode)
}

private fun receiptPrintHelperText(receipt: TransactionReceipt): String {
    if (!receipt.isDetailed) {
        return "Detail produk belum lengkap. Cetak ulang tersedia setelah data item lengkap."
    }

    if (!BluetoothPrinterConnection.isConnected) {
        return "Printer belum tersambung. Hubungkan dari Profil > Printer Bluetooth sebelum mencetak."
    }

    return if (receipt.isDetailed) {
        "Printer Bluetooth tersambung. Struk akan dicetak sesuai detail di atas."
    } else {
        "Printer Bluetooth tersambung. Struk ringkas tetap bisa dicetak ulang."
    }
}

private fun formatShortRupiah(amount: Long): String {
    return when {
        amount >= 1_000_000 -> "Rp ${String.format(Locale.US, "%.1f", amount.toDouble() / 1_000_000)}jt"
        amount >= 1_000 -> "Rp ${String.format(Locale.US, "%.0f", amount.toDouble() / 1_000)}k"
        else -> "Rp ${String.format(Locale.US, "%.0f", amount.toDouble())}"
    }
}

private val ReceiptDarkBg = Color.White
private val ReceiptDarkDash = Color(0xFFD0D5DD)
private val ReceiptDarkText = Color(0xFF18212F)
private val ReceiptDarkMuted = Color(0xFF667085)
