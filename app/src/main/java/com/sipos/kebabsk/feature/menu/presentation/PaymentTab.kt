package com.sipos.kebabsk.feature.menu.presentation

import android.content.Intent
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.List
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.ui.theme.KebabSuccess
import com.sipos.kebabsk.ui.theme.KebabSuccessBg
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

private val QrisIcon: ImageVector
    get() = ImageVector.Builder(
        name = "QRIS", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(4f, 4f); lineTo(10f, 4f); lineTo(10f, 10f); lineTo(4f, 10f); close()
            moveTo(6f, 6f); lineTo(8f, 6f); lineTo(8f, 8f); lineTo(6f, 8f); close()
            
            moveTo(14f, 4f); lineTo(20f, 4f); lineTo(20f, 10f); lineTo(14f, 10f); close()
            moveTo(16f, 6f); lineTo(18f, 6f); lineTo(18f, 8f); lineTo(16f, 8f); close()
            
            moveTo(4f, 14f); lineTo(10f, 14f); lineTo(10f, 20f); lineTo(4f, 20f); close()
            moveTo(6f, 16f); lineTo(8f, 16f); lineTo(8f, 18f); lineTo(6f, 18f); close()
            
            moveTo(14f, 14f); lineTo(16f, 14f); lineTo(16f, 16f); lineTo(14f, 16f); close()
            moveTo(18f, 14f); lineTo(20f, 14f); lineTo(20f, 16f); lineTo(18f, 16f); close()
            moveTo(16f, 18f); lineTo(20f, 18f); lineTo(20f, 20f); lineTo(16f, 20f); close()
            moveTo(14f, 18f); lineTo(16f, 18f); lineTo(16f, 20f); lineTo(14f, 20f); close()
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

private val ZigzagShape = GenericShape { size, _ ->
    val zigzagWidth = 20f
    val zigzagHeight = 15f
    val count = (size.width / zigzagWidth).toInt()

    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height - zigzagHeight)
    for (i in count downTo 0) {
        val x = i * zigzagWidth
        val y = if (i % 2 == 0) size.height else size.height - zigzagHeight
        lineTo(x, y)
    }
    close()
}

@Composable
fun PaymentTab(
    uiState: MenuUiState,
    totalAmount: Double,
    exactAmount: Int,
    quickAmounts: List<Int>,
    onPaymentMethodSelected: (Long) -> Unit,
    onQuickAmountSelected: (Int) -> Unit,
    onPaidAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSubmitCheckout: () -> Unit,
    onDismissCheckoutPreview: () -> Unit,
    onBackToCart: () -> Unit
) {
    val context = LocalContext.current
    val changeAmount = uiState.checkoutChangeAmount
    if (changeAmount != null) {
        val receiptText = buildReceiptText(uiState)
        ReceiptSuccessDialog(
            uiState = uiState,
            onShare = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, receiptText)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Bagikan Struk"))
            },
            onDone = onDismissCheckoutPreview
        )
    }

    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- TOTAL TAGIHAN CARD ---
            TotalTagihanCard(totalAmount = totalAmount, itemsCount = uiState.cartItems.sumOf { it.qty })

            // --- METODE PEMBAYARAN ---
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Metode Pembayaran", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = KebabTextDark)
                
                if (uiState.paymentMethods.isEmpty()) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Metode pembayaran belum tersedia.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.paymentMethods.forEach { method ->
                            val isCash = method.name.equals("Cash", ignoreCase = true) || method.name.equals("Tunai", ignoreCase = true)
                            val title = if (isCash) "Tunai" else "QRIS"
                            val icon = if (isCash) TunaiIcon else QrisIcon
                            PaymentMethodCard(
                                modifier = Modifier.weight(1f),
                                title = title,
                                icon = icon,
                                isSelected = uiState.selectedPaymentMethodId == method.id,
                                onClick = { onPaymentMethodSelected(method.id) }
                            )
                        }
                    }
                }
            }

            // --- JUMLAH DIBAYAR ---
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Jumlah Dibayar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = KebabTextDark)
                
                // Input Nominal
                NominalCustomInput(value = uiState.paidAmountInput, onValueChange = onPaidAmountChanged)
                
                // Quick Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickAmounts.forEachIndexed { index, amount ->
                        val label = if (index == 0 && amount == exactAmount) "Pas" else "Rp ${amount / 1000}k"
                        QuickChip(label = label, onClick = { onQuickAmountSelected(amount) })
                    }
                }
            }

            // --- RINGKASAN ORDER ---
            val paidDouble = uiState.paidAmountInput.replace(".", "").toDoubleOrNull() ?: 0.0
            val kembalian = if (paidDouble > totalAmount) paidDouble - totalAmount else 0.0
            RingkasanOrderCard(uiState = uiState, kembalian = kembalian)
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- FIXED FOOTER BUTTONS ---
        val isEnabled = uiState.cartItems.isNotEmpty() && uiState.isDailySessionOpen && !uiState.isLoading && uiState.selectedPaymentMethodId != null
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KebabBg)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onSubmitCheckout,
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KebabPrimaryContainer)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Selesaikan Pembayaran", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun TotalTagihanCard(totalAmount: Double, itemsCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFF4EFEB), Color(0xFFEBDBC5))
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(text = "Total Tagihan", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = KebabTextGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = toRupiah(totalAmount), fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = KebabPrimary, letterSpacing = (-1.5).sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.List, contentDescription = null, tint = KebabTextGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "$itemsCount Items", fontSize = 14.sp, color = KebabTextGray)
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(modifier: Modifier, title: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) KebabPrimary.copy(alpha = 0.1f) else KebabInputBg
    val borderColor = if (isSelected) KebabPrimary else Color.Transparent
    val contentColor = if (isSelected) KebabPrimary else KebabTextGray

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = contentColor)
    }
}

@Composable
private fun NominalCustomInput(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KebabInputBg)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = KebabTextDark
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        ) { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Rp", fontSize = 18.sp, color = KebabTextGray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(8.dp))
                if (value.isEmpty()) {
                    Text(text = "0", fontSize = 24.sp, color = KebabTextGray.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                } else {
                    innerTextField()
                }
            }
        }
    }
}

@Composable
private fun QuickChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF3F4F6))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = KebabTextDark)
    }
}

@Composable
private fun RingkasanOrderCard(uiState: MenuUiState, kembalian: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(KebabSummaryBg)
            .padding(24.dp)
    ) {
        Column {
            Text(text = "Ringkasan Order", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Loop Order Items
            uiState.cartItems.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.menuName, fontSize = 16.sp, color = KebabTextDark, fontWeight = FontWeight.Medium)
                        val extra = if (item.variantName.equals("Regular", ignoreCase = true) || item.variantName.equals("Default", ignoreCase = true)) "" else " • ${item.variantName}"
                        Text(text = "${item.qty}x$extra", fontSize = 13.sp, color = KebabTextGray)
                    }
                    Text(text = toRupiah(item.price * item.qty), fontSize = 14.sp, color = KebabTextDark, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(KebabCyanBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Kembalian", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = KebabTextDark)
                    Text(text = toRupiah(kembalian), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KebabCyan)
                }
            }
        }
    }
}

// ==========================================
// RECEIPT DIALOG (KEEPING EXISTING LOGIC)
// ==========================================

@Composable
private fun ReceiptSuccessDialog(
    uiState: MenuUiState,
    onShare: () -> Unit,
    onDone: () -> Unit
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ZigzagShape)
                        .background(Color.White)
                        .padding(bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(KebabSuccessBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = KebabSuccess, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Pembayaran Berhasil!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = KebabTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppTime.nowJakartaDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))),
                            fontSize = 12.sp,
                            color = KebabTextGray
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        val totalAmount = uiState.cartItems.sumOf { it.price * it.qty }
                        ReceiptRow(label = "Total Belanja", value = toRupiah(totalAmount), isBold = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        val paidDouble = uiState.paidAmountInput.replace(".", "").toDoubleOrNull() ?: totalAmount
                        ReceiptRow(label = "Tunai/Dibayar", value = toRupiah(paidDouble))

                        Spacer(modifier = Modifier.height(16.dp))

                        val dashColor = KebabDivider
                        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                            drawLine(
                                color = dashColor,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val change = if (paidDouble > totalAmount) paidDouble - totalAmount else 0.0
                        Text(
                            text = "Kembalian",
                            fontSize = 14.sp,
                            color = KebabTextGray
                        )
                        Text(
                            text = toRupiah(change),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KebabPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KebabPrimary)
                ) {
                    Text("Transaksi Selesai", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bagikan Struk", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isBold) KebabTextDark else KebabTextGray,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = KebabTextDark,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private fun buildReceiptText(uiState: MenuUiState): String {
    val sb = StringBuilder()
    sb.append("KEBAB SK\n")
    sb.append("-----------------------------\n")
    uiState.cartItems.forEach {
        sb.append("${it.menuName}\n")
        sb.append("${it.qty} x ${toRupiah(it.price)} = ${toRupiah(it.price * it.qty)}\n")
    }
    sb.append("-----------------------------\n")
    val total = uiState.cartItems.sumOf { it.price * it.qty }
    sb.append("Total: ${toRupiah(total)}\n")
    return sb.toString()
}

