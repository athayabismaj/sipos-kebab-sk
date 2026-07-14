package com.sipos.kebabsk.feature.checkout.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sipos.kebabsk.common.MoneyUtils
import com.sipos.kebabsk.common.VariantDisplayUtils
import com.sipos.kebabsk.feature.checkout.domain.model.ReceiptData
import com.sipos.kebabsk.ui.theme.KebabSuccess
import com.sipos.kebabsk.ui.theme.KebabSuccessBg

private val KebabPrimary = Color(0xFF904D00)
private val KebabTextDark = Color(0xFF1D1B19)
private val KebabTextGray = Color(0xFF564334)
private val KebabDivider = Color(0xFFDDC1AE).copy(alpha = 0.5f)

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
fun ReceiptSuccessDialog(
    receiptData: ReceiptData,
    isPrinting: Boolean,
    onPrint: () -> Unit,
    onShare: () -> Unit,
    onClose: () -> Unit
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
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
                            text = receiptData.createdAt,
                            fontSize = 12.sp,
                            color = KebabTextGray
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            receiptData.items.forEach { item ->
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val hasVariant = !item.variantName.equals("Regular", ignoreCase = true) &&
                                        !item.variantName.equals("Default", ignoreCase = true)
                                    val displayVariant = VariantDisplayUtils.formatVariantName(item.menuName, item.variantName)

                                    if (hasVariant) {
                                        Text(text = item.menuName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = displayVariant, fontSize = 12.sp, color = KebabTextGray)
                                            Text(text = MoneyUtils.formatRupiah(item.subtotal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                                        }
                                    } else {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = item.menuName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                                            Text(text = MoneyUtils.formatRupiah(item.subtotal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                                        }
                                    }

                                    Text(text = "${item.quantity}x ${MoneyUtils.formatRupiah(item.unitPrice)}", fontSize = 12.sp, color = KebabTextGray, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }

                        DashedDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        ReceiptRow(label = "Total Belanja", value = MoneyUtils.formatRupiah(receiptData.totalAmount), isBold = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        ReceiptRow(label = "Tunai/Dibayar", value = MoneyUtils.formatRupiah(receiptData.paidAmount))

                        Spacer(modifier = Modifier.height(16.dp))

                        DashedDivider()

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Kembalian",
                            fontSize = 14.sp,
                            color = KebabTextGray
                        )
                        Text(
                            text = MoneyUtils.formatRupiah(receiptData.changeAmount),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KebabPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onClose,
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
                    border = BorderStroke(1.dp, Color.White),
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
private fun DashedDivider() {
    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = KebabDivider,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
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
