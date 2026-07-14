package com.sipos.kebabsk.feature.menu.presentation

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.items
import com.sipos.kebabsk.feature.cart.domain.model.CartItem
import com.sipos.kebabsk.feature.checkout.presentation.CheckoutUiState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.feature.checkout.data.BluetoothReceiptPrinter
import com.sipos.kebabsk.feature.checkout.domain.ReceiptBuilder
import com.sipos.kebabsk.feature.checkout.domain.model.ReceiptData
import com.sipos.kebabsk.feature.checkout.domain.model.ReceiptItem
import com.sipos.kebabsk.feature.checkout.presentation.ReceiptSuccessDialog
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

// --- Custom Icons ---
private val TunaiIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Tunai", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            // Outline of money bill
            moveTo(2f, 6f)
            lineTo(22f, 6f)
            lineTo(22f, 18f)
            lineTo(2f, 18f)
            close()
        }
        path(fill = SolidColor(Color.White)) {
            moveTo(4f, 8f)
            lineTo(20f, 8f)
            lineTo(20f, 16f)
            lineTo(4f, 16f)
            close()
        }
        path(fill = SolidColor(Color.Black)) {
            // Circle in middle
            moveTo(12f, 10f)
            arcTo(2f, 2f, 0f, true, false, 12f, 14f)
            arcTo(2f, 2f, 0f, true, false, 12f, 10f)
        }
    }.build()

// --- Palette Warna ---
private val KebabBg = Color(0xFFFEF8F3)
private val KebabPrimary = Color(0xFF904D00)
private val KebabPrimaryContainer = Color(0xFFFF8C00)
private val KebabTextDark = Color(0xFF1D1B19)
private val KebabTextGray = Color(0xFF564334)
private val KebabCardBg = Color(0xFFEDE7E2)
private val KebabInputBg = Color(0xFFF8F3EE)
private val KebabSummaryBg = Color(0xFFFFFFFF)
private val KebabDivider = Color(0xFFDDC1AE).copy(alpha = 0.5f)
private val KebabCyan = Color(0xFF0EA5E9)
private val KebabCyanBg = Color(0xFFE0F2FE)

@Composable
fun PaymentTab(
    checkoutUiState: CheckoutUiState,
    cartItems: List<CartItem>,
    isDailySessionOpen: Boolean,
    isLoading: Boolean,
    totalAmount: Long,
    exactAmount: Long,
    quickAmounts: List<Long>,
    cartInteractionEnabled: Boolean = true,
    onPaymentMethodSelected: (Long) -> Unit,
    onQuickAmountSelected: (Long) -> Unit,
    onPaidAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmitCheckout: () -> Unit,
    onDismissCheckoutPreview: () -> Unit,
    onBackToCart: () -> Unit
) {
    val context = LocalContext.current
    val changeAmount = checkoutUiState.checkoutChangeAmount
    val printedReceiptKey = remember { mutableStateOf<String?>(null) }
    val receiptBuilder = remember { ReceiptBuilder() }
    val receiptPrinter = remember { BluetoothReceiptPrinter() }
    val receiptKey = checkoutUiState.checkoutTransactionCode
        ?: changeAmount?.let { "${checkoutUiState.checkoutTotalAmount}-${checkoutUiState.checkoutPaidAmount}-$it" }
    val receiptData = remember(receiptKey) {
        if (changeAmount != null) checkoutUiState.toReceiptData() else null
    }

    LaunchedEffect(receiptKey) {
        if (
            receiptKey != null &&
            receiptData != null &&
            printedReceiptKey.value != receiptKey &&
            receiptPrinter.isConnected
        ) {
            printedReceiptKey.value = receiptKey
            receiptPrinter.print(receiptBuilder.buildEscPos(receiptData))
        }
    }

    if (receiptData != null) {
        val receiptText = receiptBuilder.buildText(receiptData)
        ReceiptSuccessDialog(
            receiptData = receiptData,
            isPrinting = false,
            onPrint = {},
            onShare = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, receiptText)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Bagikan Struk"))
            },
            onClose = onDismissCheckoutPreview
        )
    }

    val scrollState = rememberScrollState()

    val paidLong = sanitizeMoneyInput(checkoutUiState.paidAmountInput).toLongOrNull() ?: 0L
    val kembalian = if (paidLong > totalAmount) paidLong - totalAmount else 0L
    val cashMethod = checkoutUiState.paymentMethods.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KebabBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TotalTagihanCard(totalAmount = totalAmount, itemsCount = cartItems.sumOf { it.qty })

            if (cashMethod == null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Metode pembayaran tunai belum tersedia.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                CashPaymentCard(
                    selected = checkoutUiState.selectedPaymentMethodId == cashMethod.id,
                    value = checkoutUiState.paidAmountInput,
                    quickAmounts = quickAmounts,
                    exactAmount = exactAmount,
                    enabled = cartInteractionEnabled,
                    onSelectCash = { onPaymentMethodSelected(cashMethod.id) },
                    onPaidAmountChanged = onPaidAmountChanged,
                    onQuickAmountSelected = onQuickAmountSelected
                )
            }

            RingkasanOrderCard(
                cartItems = cartItems,
                totalAmount = totalAmount,
                paidAmount = paidLong,
                kembalian = kembalian
            )

            Spacer(modifier = Modifier.height(164.dp))
        }

        val isEnabled = cartItems.isNotEmpty() &&
            isDailySessionOpen &&
            !isLoading &&
            cartInteractionEnabled &&
            checkoutUiState.selectedPaymentMethodId != null

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 72.dp)
                .shadow(6.dp, RoundedCornerShape(26.dp))
                .clip(RoundedCornerShape(26.dp))
                .background(Color.White.copy(alpha = 0.86f))
                .border(1.dp, Color.White.copy(alpha = 0.72f), RoundedCornerShape(26.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total dibayar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KebabTextGray
                    )
                    Text(
                        text = com.sipos.kebabsk.common.MoneyUtils.formatRupiah(totalAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KebabTextDark
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Kembalian",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KebabTextGray
                    )
                    Text(
                        text = com.sipos.kebabsk.common.MoneyUtils.formatRupiah(kembalian),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KebabCyan
                    )
                }
            }

            Button(
                onClick = {
                    if (cartInteractionEnabled) {
                        onSubmitCheckout()
                    }
                },
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KebabPrimaryContainer)
            ) {
                Text(text = "Bayar Tunai", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun TotalTagihanCard(totalAmount: Long, itemsCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF8A4B10), Color(0xFFFF8C00))
                )
            )
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Tagihan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$itemsCount item",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Text(
                text = com.sipos.kebabsk.common.MoneyUtils.formatRupiah(totalAmount),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-1.5).sp,
                fontSize = when {
                    totalAmount >= 10_000_000 -> 34.sp  // >= 10 juta
                    totalAmount >= 1_000_000  -> 40.sp  // >= 1 juta
                    totalAmount >= 100_000    -> 44.sp  // >= 100 ribu
                    else                      -> 48.sp  // normal
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Pembayaran langsung tunai",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.82f)
            )
        }
    }
}

@Composable
private fun CashPaymentCard(
    selected: Boolean,
    value: String,
    quickAmounts: List<Long>,
    exactAmount: Long,
    enabled: Boolean,
    onSelectCash: () -> Unit,
    onPaidAmountChanged: (String) -> Unit,
    onQuickAmountSelected: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .border(1.dp, KebabDivider, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(if (selected) KebabPrimary.copy(alpha = 0.10f) else KebabInputBg)
                .border(
                    width = 1.dp,
                    color = if (selected) KebabPrimary.copy(alpha = 0.32f) else KebabDivider,
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(enabled = enabled) { onSelectCash() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(KebabPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = TunaiIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Tunai", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = KebabTextDark)
                Text(text = "Input uang diterima dari pelanggan", fontSize = 13.sp, color = KebabTextGray)
            }
            Text(
                text = if (selected) "Aktif" else "Pilih",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) KebabPrimary else KebabTextGray,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) KebabPrimary.copy(alpha = 0.12f) else KebabInputBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Uang Diterima", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
            NominalCustomInput(value = value, enabled = enabled, onValueChange = onPaidAmountChanged)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickAmounts.forEachIndexed { index, amount ->
                val label = if (index == 0 && amount == exactAmount) "Uang Pas" else "Rp ${amount / 1000}k"
                QuickChip(label = label, enabled = enabled, onClick = { onQuickAmountSelected(amount) })
            }
        }
    }
}

@Composable
private fun NominalCustomInput(value: String, enabled: Boolean, onValueChange: (String) -> Unit) {
    val displayValue = formatMoneyInputForDisplay(value)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(KebabInputBg)
            .border(1.dp, KebabDivider, RoundedCornerShape(18.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        BasicTextField(
            value = displayValue,
            onValueChange = onValueChange,
            enabled = enabled,
            textStyle = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KebabTextDark
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        ) { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Rp", fontSize = 18.sp, color = KebabPrimary, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(10.dp))
                if (value.isEmpty()) {
                    Text(text = "0", fontSize = 30.sp, color = KebabTextGray.copy(alpha = 0.42f), fontWeight = FontWeight.ExtraBold)
                } else {
                    innerTextField()
                }
            }
        }
    }
}

@Composable
private fun QuickChip(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(KebabPrimary.copy(alpha = 0.08f))
            .border(1.dp, KebabPrimary.copy(alpha = 0.14f), RoundedCornerShape(50))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = KebabPrimary,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun RingkasanOrderCard(cartItems: List<CartItem>, totalAmount: Long, paidAmount: Long, kembalian: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(KebabSummaryBg)
            .border(1.dp, KebabDivider, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Ringkasan Order", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = KebabTextDark)
                Text(
                    text = "${cartItems.sumOf { it.qty }} item",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = KebabTextGray,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(KebabInputBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Loop Order Items
            cartItems.forEach { item ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val hasVariant = !item.variantName.equals("Regular", ignoreCase = true) && !item.variantName.equals("Default", ignoreCase = true)
                    val displayVariant = com.sipos.kebabsk.common.VariantDisplayUtils.formatVariantName(item.menuName, item.variantName)

                    if (hasVariant) {
                        Text(text = item.menuName, fontSize = 15.sp, color = KebabTextDark, fontWeight = FontWeight.ExtraBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = displayVariant, fontSize = 13.sp, color = KebabTextGray)
                            Text(text = com.sipos.kebabsk.common.MoneyUtils.formatRupiah(item.price * item.qty), fontSize = 14.sp, color = KebabTextDark, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = item.menuName, fontSize = 15.sp, color = KebabTextDark, fontWeight = FontWeight.Bold)
                            Text(text = com.sipos.kebabsk.common.MoneyUtils.formatRupiah(item.price * item.qty), fontSize = 14.sp, color = KebabTextDark, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(text = "${item.qty}x ${com.sipos.kebabsk.common.MoneyUtils.formatRupiah(item.price)}", fontSize = 13.sp, color = KebabTextGray, modifier = Modifier.padding(top = 4.dp))
                }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
            PaymentSummaryRow(label = "Tunai", value = com.sipos.kebabsk.common.MoneyUtils.formatRupiah(paidAmount))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(KebabCyanBg)
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Kembalian", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                    Text(text = com.sipos.kebabsk.common.MoneyUtils.formatRupiah(kembalian), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = KebabCyan)
                }
            }
        }
    }
}

@Composable
private fun PaymentSummaryRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = if (isBold) 15.sp else 14.sp,
            color = KebabTextGray,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = if (isBold) 16.sp else 14.sp,
            color = KebabTextDark,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold
        )
    }
}

private fun CheckoutUiState.toReceiptData(): ReceiptData {
    val totalAmount = checkoutTotalAmount ?: 0L
    return ReceiptData(
        transactionCode = checkoutTransactionCode.orEmpty(),
        cashierName = "",
        items = checkoutReceiptItems.map { item ->
            ReceiptItem(
                menuName = item.menuName,
                variantName = item.variantName,
                quantity = item.qty,
                unitPrice = item.price
            )
        },
        totalAmount = totalAmount,
        paidAmount = checkoutPaidAmount ?: totalAmount,
        changeAmount = checkoutChangeAmount ?: 0L,
        paymentMethodName = "Tunai",
        note = null,
        createdAt = AppTime.nowJakartaDateTime().format(
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
        )
    )
}

private fun sanitizeMoneyInput(value: String): String {
    return value.filter { it.isDigit() }
}

private fun formatMoneyInputForDisplay(value: String): String {
    val clean = sanitizeMoneyInput(value)
    if (clean.isBlank()) return ""

    return clean
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
}
