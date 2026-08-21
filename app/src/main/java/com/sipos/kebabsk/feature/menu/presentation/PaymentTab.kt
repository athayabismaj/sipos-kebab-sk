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
import com.sipos.kebabsk.feature.checkout.presentation.QrisPaymentDialog
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.common.MoneyUtils
import com.sipos.kebabsk.common.VariantDisplayUtils
import com.sipos.kebabsk.R
import com.sipos.kebabsk.feature.checkout.data.BluetoothReceiptPrinter
import com.sipos.kebabsk.feature.checkout.domain.ReceiptBuilder
import com.sipos.kebabsk.feature.checkout.domain.model.ReceiptData
import com.sipos.kebabsk.feature.checkout.domain.model.ReceiptItem
import com.sipos.kebabsk.feature.checkout.presentation.ReceiptSuccessDialog
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
    cashierName: String,
    isDailySessionOpen: Boolean,
    isDailySessionStatusKnown: Boolean,
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
    onRetryQris: () -> Unit,
    onConfirmQrisPayment: () -> Unit,
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
    val receiptData = remember(
        receiptKey,
        cashierName,
        checkoutUiState.checkoutBranchAddress,
        checkoutUiState.isQrisAwaitingConfirmation,
        checkoutUiState.checkoutPaymentMethodName
    ) {
        if (changeAmount != null && !checkoutUiState.isQrisAwaitingConfirmation) {
            checkoutUiState.toReceiptData(cashierName)
        } else null
    }

    LaunchedEffect(receiptKey, receiptData) {
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

    if (checkoutUiState.isQrisAwaitingConfirmation) {
        QrisPaymentDialog(
            state = checkoutUiState,
            onRetry = onRetryQris,
            onPaymentReceived = onConfirmQrisPayment
        )
    }

    val scrollState = rememberScrollState()

    val selectedMethod = checkoutUiState.paymentMethods
        .firstOrNull { it.id == checkoutUiState.selectedPaymentMethodId }
    val isQrisSelected = selectedMethod?.name.equals("QRIS", ignoreCase = true)
    val paidLong = if (isQrisSelected) {
        totalAmount
    } else {
        MoneyUtils.sanitizeMoneyInput(checkoutUiState.paidAmountInput).toLongOrNull() ?: 0L
    }
    val kembalian = if (!isQrisSelected && paidLong > totalAmount) paidLong - totalAmount else 0L
    val cashMethod = checkoutUiState.paymentMethods.firstOrNull { it.name.isCashPaymentName() }
    val qrisMethod = checkoutUiState.paymentMethods.firstOrNull { it.name.equals("QRIS", ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KebabBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TotalTagihanCard(
                totalAmount = totalAmount,
                itemsCount = cartItems.sumOf { it.qty },
                isQrisSelected = isQrisSelected
            )

            if (checkoutUiState.paymentMethods.isEmpty() && (!checkoutUiState.paymentMethodsLoadCompleted || checkoutUiState.isLoading)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = stringResource(R.string.checkout_cash_loading),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else if (checkoutUiState.paymentMethods.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Assertive }
                ) {
                    Text(
                        text = "Metode pembayaran Tunai atau QRIS belum tersedia.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                PaymentMethodSelector(
                    cashMethodId = cashMethod?.id,
                    qrisMethodId = qrisMethod?.id,
                    selectedPaymentMethodId = checkoutUiState.selectedPaymentMethodId,
                    enabled = cartInteractionEnabled && !checkoutUiState.isSubmitting,
                    onSelected = onPaymentMethodSelected
                )

                if (isQrisSelected && qrisMethod != null) {
                    QrisPaymentCard()
                } else if (cashMethod != null) {
                    CashPaymentCard(
                        selected = checkoutUiState.selectedPaymentMethodId == cashMethod.id,
                        value = checkoutUiState.paidAmountInput,
                        quickAmounts = quickAmounts,
                        exactAmount = exactAmount,
                        enabled = cartInteractionEnabled && !checkoutUiState.isSubmitting,
                        onSelectCash = { onPaymentMethodSelected(cashMethod.id) },
                        onPaidAmountChanged = onPaidAmountChanged,
                        onQuickAmountSelected = onQuickAmountSelected
                    )
                }
            }

            if (!isDailySessionStatusKnown && !isLoading) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.checkout_session_unknown_title),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.checkout_session_unknown_message),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else if (!isDailySessionOpen && !isLoading) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.checkout_session_closed_title),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.checkout_session_closed_message),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            checkoutUiState.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Assertive }
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            RingkasanOrderCard(
                cartItems = cartItems,
                totalAmount = totalAmount,
                paidAmount = paidLong,
                kembalian = kembalian,
                paymentMethodName = selectedMethod?.name.orEmpty()
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        val isEnabled = cartItems.isNotEmpty() &&
            isDailySessionStatusKnown &&
            isDailySessionOpen &&
            !isLoading &&
            !checkoutUiState.isLoading &&
            !checkoutUiState.isSubmitting &&
            cartInteractionEnabled &&
            checkoutUiState.selectedPaymentMethodId != null

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 14.dp)
                .padding(bottom = 68.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(1.dp, KebabDivider, RoundedCornerShape(18.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(0.9f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = stringResource(R.string.checkout_total_paid),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = KebabTextGray,
                    maxLines = 1
                )
                Text(
                    text = MoneyUtils.formatRupiah(totalAmount),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KebabTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(
                modifier = Modifier.weight(0.8f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = if (isQrisSelected) "Metode" else stringResource(R.string.checkout_change),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = KebabTextGray,
                    maxLines = 1
                )
                Text(
                    text = if (isQrisSelected) "QRIS" else MoneyUtils.formatRupiah(kembalian),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KebabCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = {
                    if (cartInteractionEnabled && !checkoutUiState.isSubmitting) {
                        onSubmitCheckout()
                    }
                },
                enabled = isEnabled,
                modifier = Modifier
                    .weight(1.15f)
                    .height(48.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KebabPrimaryContainer),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                if (checkoutUiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.checkout_processing_payment),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(text = if (isQrisSelected) "Buat QRIS" else stringResource(R.string.checkout_pay_cash), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun TotalTagihanCard(totalAmount: Long, itemsCount: Int, isQrisSelected: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF7D440D), Color(0xFFE98208))
                )
            )
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.checkout_total_bill),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                            text = pluralStringResource(R.plurals.item_count, itemsCount, itemsCount),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Text(
                text = MoneyUtils.formatRupiah(totalAmount),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = (-1.5).sp,
                fontSize = when {
                    totalAmount >= 10_000_000 -> 28.sp
                    totalAmount >= 1_000_000  -> 30.sp
                    else                      -> 34.sp
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (isQrisSelected) "QRIS dinamis sesuai total transaksi" else stringResource(R.string.checkout_cash_direct),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.82f)
            )
        }
    }
}

@Composable
private fun PaymentMethodSelector(
    cashMethodId: Long?,
    qrisMethodId: Long?,
    selectedPaymentMethodId: Long?,
    enabled: Boolean,
    onSelected: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, KebabDivider, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Metode Pembayaran", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = KebabTextDark)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            cashMethodId?.let { methodId ->
                PaymentMethodOption(
                    label = "Tunai",
                    icon = TunaiIcon,
                    selected = selectedPaymentMethodId == methodId,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelected(methodId) }
                )
            }
            qrisMethodId?.let { methodId ->
                PaymentMethodOption(
                    label = "QRIS",
                    icon = Icons.Default.QrCode2,
                    selected = selectedPaymentMethodId == methodId,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelected(methodId) }
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val activeColor = if (label == "QRIS") Color(0xFF087F5B) else KebabPrimary
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) activeColor.copy(alpha = 0.10f) else KebabInputBg)
            .border(
                width = 1.dp,
                color = if (selected) activeColor.copy(alpha = 0.45f) else KebabDivider,
                shape = RoundedCornerShape(14.dp)
            )
            .minimumInteractiveComponentSize()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) activeColor else KebabTextGray, modifier = Modifier.size(19.dp))
        Text(label, fontWeight = FontWeight.Bold, color = if (selected) activeColor else KebabTextGray, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = activeColor, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun QrisPaymentCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF0FDF8))
            .border(1.dp, Color(0xFFB7E4D3), RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).background(Color(0xFF087F5B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("QRIS Dinamis", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0B5D46))
            Text(
                "QR dibuat otomatis sesuai total. Tidak perlu memasukkan nominal pembayaran.",
                modifier = Modifier.padding(top = 2.dp),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = Color(0xFF477466)
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
    val activeState = stringResource(R.string.checkout_cash_active_state)
    val inactiveState = stringResource(R.string.checkout_cash_inactive_state)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, KebabDivider, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) KebabPrimary.copy(alpha = 0.10f) else KebabInputBg)
                .border(
                    width = 1.dp,
                    color = if (selected) KebabPrimary.copy(alpha = 0.32f) else KebabDivider,
                    shape = RoundedCornerShape(14.dp)
                )
                .minimumInteractiveComponentSize()
                .semantics {
                    stateDescription = if (selected) activeState else inactiveState
                }
                .clickable(enabled = enabled) { onSelectCash() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(KebabPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = TunaiIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.checkout_cash_method), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = KebabTextDark)
                Text(text = stringResource(R.string.checkout_cash_input_help), fontSize = 12.sp, color = KebabTextGray)
            }
            Text(
                text = if (selected) stringResource(R.string.checkout_active_badge) else stringResource(R.string.action_select),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) KebabPrimary else KebabTextGray,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selected) KebabPrimary.copy(alpha = 0.12f) else KebabInputBg)
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(text = stringResource(R.string.checkout_received_amount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
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
    val displayValue = MoneyUtils.formatRupiahInputForDisplay(value)
    val receivedAmountDescription = stringResource(R.string.cd_received_amount)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(KebabInputBg)
            .border(1.dp, KebabDivider, RoundedCornerShape(14.dp))
            .semantics { contentDescription = receivedAmountDescription }
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        BasicTextField(
            value = displayValue,
            onValueChange = onValueChange,
            enabled = enabled,
            textStyle = TextStyle(
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = KebabTextDark
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        ) { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Rp", fontSize = 16.sp, color = KebabPrimary, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(8.dp))
                if (value.isEmpty()) {
                    Text(text = "0", fontSize = 25.sp, color = KebabTextGray.copy(alpha = 0.42f), fontWeight = FontWeight.ExtraBold)
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
            .minimumInteractiveComponentSize()
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 13.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = KebabPrimary,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun RingkasanOrderCard(
    cartItems: List<CartItem>,
    totalAmount: Long,
    paidAmount: Long,
    kembalian: Long,
    paymentMethodName: String
) {
    val isQris = paymentMethodName.equals("QRIS", ignoreCase = true)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(KebabSummaryBg)
            .border(1.dp, KebabDivider, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.checkout_order_summary), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = KebabTextDark)
                Text(
                    text = pluralStringResource(
                        R.plurals.item_count,
                        cartItems.sumOf { it.qty },
                        cartItems.sumOf { it.qty }
                    ),
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
                    val displayVariant = VariantDisplayUtils.formatVariantName(item.menuName, item.variantName)

                    if (hasVariant) {
                        Text(text = item.menuName, fontSize = 15.sp, color = KebabTextDark, fontWeight = FontWeight.ExtraBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = displayVariant, fontSize = 13.sp, color = KebabTextGray)
                            Text(text = MoneyUtils.formatRupiah(item.price * item.qty), fontSize = 14.sp, color = KebabTextDark, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = item.menuName, fontSize = 15.sp, color = KebabTextDark, fontWeight = FontWeight.Bold)
                            Text(text = MoneyUtils.formatRupiah(item.price * item.qty), fontSize = 14.sp, color = KebabTextDark, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(text = "${item.qty}x ${MoneyUtils.formatRupiah(item.price)}", fontSize = 13.sp, color = KebabTextGray, modifier = Modifier.padding(top = 4.dp))
                }
            }

            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
            PaymentSummaryRow(
                label = if (isQris) "Metode pembayaran" else stringResource(R.string.checkout_cash_label),
                value = if (isQris) "QRIS" else MoneyUtils.formatRupiah(paidAmount)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(KebabCyanBg)
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (isQris) "Nominal QRIS" else stringResource(R.string.checkout_change), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                    Text(text = MoneyUtils.formatRupiah(if (isQris) totalAmount else kembalian), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = KebabCyan)
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

private fun CheckoutUiState.toReceiptData(cashierName: String): ReceiptData {
    val totalAmount = checkoutTotalAmount ?: 0L
    return ReceiptData(
        transactionCode = checkoutTransactionCode.orEmpty(),
        cashierName = cashierName.trim(),
        branchAddress = checkoutBranchAddress,
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
        paymentMethodName = checkoutPaymentMethodName?.takeIf(String::isNotBlank) ?: "Tunai",
        note = null,
        createdAt = AppTime.nowJakartaDateTime().format(
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
        )
    )
}

private fun String.isCashPaymentName(): Boolean {
    return equals("Cash", ignoreCase = true) || equals("Tunai", ignoreCase = true)
}
