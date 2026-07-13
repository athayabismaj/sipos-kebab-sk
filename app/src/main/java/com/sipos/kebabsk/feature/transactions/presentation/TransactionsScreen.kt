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
import androidx.compose.material.icons.filled.RestaurantMenu
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.painterResource
import com.sipos.kebabsk.R
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.feature.profile.presentation.BluetoothPrinterConnection
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionHistoryItem
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionReceipt
import com.sipos.kebabsk.feature.transactions.domain.model.TransactionReceiptItem
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
                        val result = BluetoothPrinterConnection.print(buildReceiptEscPosBytes(receipt))
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
        Dialog(
            onDismissRequest = { if (!uiState.isVoiding) transactionToVoid = null },
            properties = DialogProperties(dismissOnBackPress = !uiState.isVoiding, dismissOnClickOutside = !uiState.isVoiding)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = KebabCardBg,
                tonalElevation = 8.dp,
                shadowElevation = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = KebabPrimaryContainer.copy(alpha = 0.75f),
                        modifier = Modifier.size(58.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = KebabPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Alasan Pembatalan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = KebabTextDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tentukan perlakuan bahan dari transaksi yang dibatalkan.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = KebabTextGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    if (uiState.isVoiding) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = KebabDateInactiveBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = KebabPrimary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Membatalkan transaksi...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = KebabTextDark
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (sessionId != null) {
                                    viewModel.voidTransaction(transactionToVoid!!, VoidReason.RESTOCK, sessionId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KebabSuccess),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.18f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Recycling,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Kembalikan ke Stok",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Bahan masih layak dipakai kembali",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.86f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                if (sessionId != null) {
                                    viewModel.voidTransaction(transactionToVoid!!, VoidReason.WASTE, sessionId)
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = KebabErrorBg.copy(alpha = 0.18f),
                                contentColor = KebabErrorText
                            ),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, KebabErrorText.copy(alpha = 0.55f)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.DeleteForever,
                                            contentDescription = null,
                                            tint = KebabErrorText,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Buang sebagai Sampah",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KebabErrorText
                                    )
                                    Text(
                                        text = "Bahan rusak atau tidak boleh dipakai",
                                        fontSize = 11.sp,
                                        color = KebabErrorText.copy(alpha = 0.72f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = KebabDivider.copy(alpha = 0.35f))
                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = { transactionToVoid = null },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "Batal",
                                color = KebabTextGray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

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
                text = "Riwayat Transaksi",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KebabTextDark
            )
            Text(
                text = "Pantau penjualan dan pembatalan",
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
            Icon(Icons.Default.CalendarToday, contentDescription = "Pilih tanggal", tint = KebabPrimary)
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
    val context = LocalContext.current
    val formatRupiah = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
    val isSuccess = trx.status.equals("Sukses", ignoreCase = true) ||
        trx.status.equals("Success", ignoreCase = true) ||
        trx.status.equals("Lunas", ignoreCase = true) ||
        trx.status.equals("Paid", ignoreCase = true)
    val isCancelled = !isSuccess
    val opacity = if (isCancelled) 0.6f else 1f
    val textDecoration = if (isCancelled) TextDecoration.LineThrough else null
    val badgeBg = if (isSuccess) KebabSuccessBg else KebabErrorBg
    val badgeText = if (isSuccess) KebabSuccess else KebabErrorText
    val iconBgColor = if (isCancelled) Color(0xFFDED9D4) else KebabIconHighlight.copy(alpha = 0.2f)
    val iconTint = if (isCancelled) KebabTextGray else KebabPrimaryContainer

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = opacity))
            .border(1.dp, KebabDivider.copy(alpha = 0.22f * opacity), RoundedCornerShape(22.dp))
            .clickable {
                if (isSuccess) {
                    if (isVoidable) {
                        onVoidClicked()
                    } else {
                        Toast.makeText(
                            context,
                            "Transaksi sesi sebelumnya tidak bisa dibatalkan.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .padding(18.dp)
    ) {
        // Top: icon + info + badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KebabTextDark.copy(alpha = opacity),
                        textDecoration = textDecoration,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            // Status badge and optional void button
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = badgeBg.copy(alpha = opacity),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = receiptStatusDisplay(trx.status),
                        color = badgeText.copy(alpha = opacity),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        maxLines = 1
                    )
                }
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

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    !isSuccess -> "Transaksi sudah dibatalkan, struk tetap bisa dilihat"
                    isVoidable -> "Ketuk kartu untuk membatalkan transaksi hari ini"
                    else -> "Hanya transaksi hari ini yang bisa dibatalkan"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = KebabTextGray.copy(alpha = 0.72f),
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(KebabPrimary.copy(alpha = 0.1f))
                    .clickable { onReceiptClicked() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = null,
                    tint = KebabPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Cetak",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
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
    Dialog(
        onDismissRequest = { if (!isPrinting) onDismiss() },
        properties = DialogProperties(dismissOnBackPress = !isPrinting, dismissOnClickOutside = !isPrinting)
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            tonalElevation = 10.dp,
            shadowElevation = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 398.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFF7F1),
                                    Color.White
                                )
                            )
                        )
                        .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = KebabPrimary.copy(alpha = 0.10f)
                            ) {
                                Text(
                                    text = "Cetak ulang",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = KebabPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = "Detail Struk",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KebabTextDark
                            )
                            Text(
                                text = "Periksa detail sebelum mencetak kembali.",
                                fontSize = 12.sp,
                                color = KebabTextGray,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 17.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = KebabPrimary,
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when {
                        isLoading -> {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = Color(0xFFFFF7F1),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 30.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = KebabPrimary, strokeWidth = 3.dp)
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Memuat detail struk...",
                                        color = KebabTextDark,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Mohon tunggu sebentar.",
                                        color = KebabTextGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        !errorMessage.isNullOrBlank() -> {
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = KebabErrorBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, KebabErrorText.copy(alpha = 0.16f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Detail struk belum tersedia",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = KebabErrorText
                                    )
                                    Text(
                                        text = errorMessage,
                                        fontSize = 12.sp,
                                        color = KebabErrorText.copy(alpha = 0.78f),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Text("Tutup", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = onRetry,
                                    modifier = Modifier.weight(1f).height(52.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = KebabPrimary)
                                ) {
                                    Text("Coba Lagi", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        receipt != null -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                ReceiptPreviewCard(receipt = receipt)
                            }

                            ReceiptPrinterInfo(receipt = receipt)

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    enabled = !isPrinting,
                                    modifier = Modifier.weight(1f).height(54.dp),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Text("Tutup", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { onPrint(receipt) },
                                    enabled = !isPrinting && receipt.isDetailed,
                                    modifier = Modifier.weight(1.35f).height(54.dp),
                                    shape = RoundedCornerShape(18.dp),
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
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isPrinting) "Mencetak..." else "Cetak Struk",
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
}

@Composable
private fun ReceiptPreviewCard(receipt: TransactionReceipt) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 352.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ReceiptDarkBg)
            .border(1.dp, ReceiptDarkBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 22.dp, vertical = 26.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.sk_receipt_logo),
                contentDescription = "Logo Kebab SK",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(46.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "KEBAB SK",
                color = ReceiptDarkText,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        DashedReceiptDivider()

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ReceiptPreviewRow(label = "No.", value = receiptDisplayCode(receipt))
            ReceiptPreviewRow(label = "Kasir", value = receipt.cashierName)
        }

        ReceiptPreviewTopBadges(
            status = receiptStatusDisplay(receipt.status),
            paymentMethod = receiptPaymentDisplay(receipt.paymentMethod)
        )

        DashedReceiptDivider()

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
            ReceiptPreviewRow(label = "Sub Total", value = toRupiahNoDecimal(receipt.totalAmount))
            ReceiptPreviewRow(label = "Total", value = toRupiahNoDecimal(receipt.totalAmount), isBold = true)
            ReceiptPreviewRow(label = "Bayar", value = toRupiahNoDecimal(receipt.paidAmount))
            ReceiptPreviewRow(
                label = "Kembalian",
                value = toRupiahNoDecimal(receipt.changeAmount),
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
private fun ReceiptPreviewTopBadges(status: String, paymentMethod: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ReceiptDarkDash, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReceiptPreviewBadge(text = status)
        ReceiptPreviewBadge(text = paymentMethod)
    }
}

@Composable
private fun ReceiptPreviewBadge(text: String) {
    Text(
        text = text,
        color = ReceiptDarkText,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold
    )
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
        shape = RoundedCornerShape(18.dp),
        color = if (BluetoothPrinterConnection.isConnected) {
            KebabSuccessBg.copy(alpha = 0.9f)
        } else {
            Color.White.copy(alpha = 0.72f)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (BluetoothPrinterConnection.isConnected) {
                    KebabSuccess.copy(alpha = 0.14f)
                } else {
                    KebabPrimary.copy(alpha = 0.10f)
                },
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = if (BluetoothPrinterConnection.isConnected) KebabSuccess else KebabPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = receiptPrintHelperText(receipt),
                fontSize = 12.sp,
                color = if (BluetoothPrinterConnection.isConnected) KebabSuccess else KebabTextGray,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 17.sp,
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
                text = toRupiahNoDecimal(item.subtotal),
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
                text = "  ${item.qty} x ${toRupiahNoDecimal(item.price)}",
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
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (currentPage > 1) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
                .clickable(enabled = currentPage > 1) { onPrevious() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Sebelumnya",
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
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (currentPage < totalPages) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
                .clickable(enabled = currentPage < totalPages) { onNext() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Selanjutnya",
                tint = if (currentPage < totalPages) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// === HELPER FUNCTIONS ===
private fun buildReceiptEscPosBytes(receipt: TransactionReceipt): ByteArray {
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
    fun line() = text("-".repeat(PRINTER_RECEIPT_WIDTH))

    command(0x1B, 0x40)
    align(1)
    bold(true)
    size(0x11)
    text("KEBAB SK")
    size(0x00)
    bold(false)
    RECEIPT_BUSINESS_CONTACT_LINES.forEach { text(it.take(PRINTER_RECEIPT_WIDTH)) }
    line()
    text(receiptColumns("No.", receiptDisplayCode(receipt).take(20)))
    text("Kasir")
    text(receipt.cashierName.take(PRINTER_RECEIPT_WIDTH))
    text("${receiptStatusDisplay(receipt.status).take(12)} | ${receiptPaymentDisplay(receipt.paymentMethod).take(14)}".take(PRINTER_RECEIPT_WIDTH))
    align(0)
    line()

    receipt.items.forEachIndexed { index, item ->
        val variantName = item.variantName
            ?.takeIf { it.isNotBlank() && !it.equals("Default", ignoreCase = true) }

        bold(true)
        text("${index + 1}. ${item.name}".take(PRINTER_RECEIPT_WIDTH))
        bold(false)
        variantName?.let { text("   ${it}".take(PRINTER_RECEIPT_WIDTH)) }
        text(receiptColumns("   ${item.qty} x ${toRupiahNoDecimal(item.price)}", toRupiahNoDecimal(item.subtotal)))
    }

    line()
    text(receiptColumns("Total QTY", receipt.items.sumOf { it.qty }.toString()))
    line()
    text(receiptColumns("Sub Total", toRupiahNoDecimal(receipt.totalAmount)))
    bold(true)
    text(receiptColumns("Total", toRupiahNoDecimal(receipt.totalAmount)))
    bold(false)
    text(receiptColumns("Bayar", toRupiahNoDecimal(receipt.paidAmount)))
    text(receiptColumns("Kembali", toRupiahNoDecimal(receipt.changeAmount)))
    align(1)
    line()
    bold(true)
    text("Terimakasih Telah Berbelanja")
    bold(false)
    text(receipt.createdAtLabel.take(PRINTER_RECEIPT_WIDTH))
    text()
    text()
    command(0x1D, 0x56, 0x42, 0x00)

    return buffer.toByteArray()
}

private fun receiptColumns(left: String, right: String): String {
    val safeLeft = left.take(PRINTER_RECEIPT_WIDTH)
    val safeRight = right.take(PRINTER_RECEIPT_WIDTH)
    val spaces = (PRINTER_RECEIPT_WIDTH - safeLeft.length - safeRight.length).coerceAtLeast(1)
    return safeLeft + " ".repeat(spaces) + safeRight
}

private fun toRupiahNoDecimal(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID"))
    return "Rp${formatter.format(amount)}"
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

private fun receiptPaymentDisplay(paymentMethod: String): String {
    return when (paymentMethod.lowercase(Locale.US)) {
        "cash", "tunai" -> "Tunai"
        else -> paymentMethod
    }
}

private fun receiptDisplayCode(receipt: TransactionReceipt): String {
    return receipt.code.trim().takeIf { it.isNotBlank() } ?: "TRX-${receipt.id}"
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

private const val PRINTER_RECEIPT_WIDTH = 32
private val RECEIPT_BUSINESS_CONTACT_LINES = emptyList<String>()
private val ReceiptDarkBg = Color.White
private val ReceiptDarkBorder = Color(0xFFD8D0C6)
private val ReceiptDarkDash = Color(0xFFB8AEA3)
private val ReceiptDarkText = Color(0xFF1F1F1F)
private val ReceiptDarkMuted = Color(0xFF6B625A)
