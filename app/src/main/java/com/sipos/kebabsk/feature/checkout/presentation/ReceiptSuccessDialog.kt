package com.sipos.kebabsk.feature.checkout.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sipos.kebabsk.R
import com.sipos.kebabsk.common.MoneyUtils
import com.sipos.kebabsk.common.TransactionCodeFormatter
import com.sipos.kebabsk.common.VariantDisplayUtils
import com.sipos.kebabsk.feature.checkout.domain.model.ReceiptData
import com.sipos.kebabsk.feature.checkout.domain.model.ReceiptItem
import com.sipos.kebabsk.ui.theme.KebabSuccess
import com.sipos.kebabsk.ui.theme.KebabSuccessBg

private val KebabPrimary = Color(0xFF904D00)
private val KebabTextDark = Color(0xFF1F1F1F)
private val KebabTextGray = Color(0xFF6B625A)
private val ReceiptBorder = Color(0xFFD8D0C6)
private val ReceiptDash = Color(0xFFB8AEA3)

@Composable
fun ReceiptSuccessDialog(
    receiptData: ReceiptData,
    onShare: () -> Unit,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 398.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFFFFF0EA),
            tonalElevation = 10.dp,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SuccessHeader()

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CheckoutReceiptPreview(receiptData)
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KebabPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transaksi Selesai",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = KebabPrimary.copy(alpha = 0.45f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.72f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = KebabPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bagikan Struk",
                        color = KebabPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = KebabSuccessBg
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = KebabSuccess,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "Pembayaran Berhasil",
                color = KebabTextDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Periksa detail transaksi sebelum selesai.",
                color = KebabTextGray,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun CheckoutReceiptPreview(receipt: ReceiptData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 352.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, ReceiptBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 22.dp, vertical = 26.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.sk_receipt_logo),
                contentDescription = "Logo Kebab SK",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(46.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "KEBAB SK",
                color = KebabTextDark,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.ExtraBold
            )
            receipt.branchAddress?.trim()?.takeIf { it.isNotEmpty() }?.let { address ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = address,
                    color = KebabTextGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        ReceiptDashedDivider()

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CheckoutReceiptRow(
                label = "No.",
                value = TransactionCodeFormatter.formatForDisplay(receipt.transactionCode).ifBlank { "-" }
            )
            CheckoutReceiptRow(
                label = "Kasir",
                value = receipt.cashierName.ifBlank { "Kasir" }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ReceiptDash, RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReceiptBadge("LUNAS")
            ReceiptBadge(receipt.paymentMethodName?.takeIf(String::isNotBlank) ?: "Tunai")
        }

        ReceiptDashedDivider()

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            receipt.items.forEachIndexed { index, item ->
                CheckoutReceiptItem(index = index + 1, item = item)
            }
        }

        ReceiptDashedDivider()
        CheckoutReceiptRow(
            label = "Total QTY",
            value = receipt.items.sumOf { it.quantity }.toString()
        )
        ReceiptDashedDivider()

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            CheckoutReceiptRow("Sub Total", MoneyUtils.formatRupiah(receipt.totalAmount))
            CheckoutReceiptRow("Total", MoneyUtils.formatRupiah(receipt.totalAmount), isBold = true)
            CheckoutReceiptRow("Bayar", MoneyUtils.formatRupiah(receipt.paidAmount))
            CheckoutReceiptRow("Kembalian", MoneyUtils.formatRupiah(receipt.changeAmount))
        }

        ReceiptDashedDivider()

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "Terima kasih telah berbelanja",
                color = KebabTextGray,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = receipt.createdAt,
                color = KebabTextGray.copy(alpha = 0.82f),
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
private fun CheckoutReceiptItem(index: Int, item: ReceiptItem) {
    val variant = VariantDisplayUtils.formatVariantName(item.menuName, item.variantName)
        .takeIf {
            item.variantName.isNotBlank() &&
                !item.variantName.equals("Default", ignoreCase = true) &&
                !item.variantName.equals("Regular", ignoreCase = true)
        }

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = "$index. ${item.menuName}",
                    color = KebabTextDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                variant?.let {
                    Text(
                        text = "  $it",
                        color = KebabTextGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
            Text(
                text = MoneyUtils.formatRupiah(item.subtotal),
                color = KebabTextDark,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(
            text = "  ${item.quantity} x ${MoneyUtils.formatRupiah(item.unitPrice)}",
            color = KebabTextGray,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun CheckoutReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.42f),
            color = if (isBold) KebabTextDark else KebabTextGray,
            fontFamily = FontFamily.Monospace,
            fontSize = if (isBold) 14.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.58f),
            color = KebabTextDark,
            fontFamily = FontFamily.Monospace,
            fontSize = if (isBold) 14.sp else 13.sp,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ReceiptBadge(text: String) {
    Text(
        text = text,
        color = KebabTextDark,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun ReceiptDashedDivider() {
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
                    .height(1.dp)
                    .background(ReceiptDash.copy(alpha = 0.55f))
            )
        }
    }
}
