package com.sipos.kebabsk.feature.checkout.presentation

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.sipos.kebabsk.common.MoneyUtils

@Composable
fun QrisPaymentDialog(
    state: CheckoutUiState,
    onRetry: () -> Unit,
    onPaymentReceived: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFE8F7F1), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("QR", color = Color(0xFF087F5B), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Pembayaran QRIS",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF172033)
                )
                Text(
                    text = state.qrisMerchantName?.takeIf(String::isNotBlank)
                        ?: state.qrisBranchName?.takeIf(String::isNotBlank)
                        ?: "Merchant cabang",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF667085),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = MoneyUtils.formatRupiah(state.qrisAmount ?: state.checkoutTotalAmount ?: 0L),
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF172033)
                )

                when {
                    state.isGeneratingQris -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2563EB))
                        }
                        Text("Menyiapkan QRIS dinamis...", color = Color(0xFF667085), fontSize = 13.sp)
                    }

                    !state.qrisPayload.isNullOrBlank() -> {
                        QrisCodeImage(
                            payload = state.qrisPayload,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .padding(top = 14.dp)
                        )
                        Text(
                            text = "Minta pelanggan memindai QR, lalu periksa notifikasi pembayaran pada aplikasi merchant.",
                            modifier = Modifier.padding(top = 10.dp),
                            color = Color(0xFF667085),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    else -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFF1F2)
                        ) {
                            Text(
                                text = state.qrisErrorMessage ?: "QRIS belum dapat dibuat.",
                                modifier = Modifier.padding(14.dp),
                                color = Color(0xFFBE123C),
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                state.checkoutTransactionCode?.let { code ->
                    Text(
                        text = code,
                        modifier = Modifier.padding(top = 10.dp),
                        color = Color(0xFF98A2B3),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRetry,
                        enabled = !state.isGeneratingQris,
                        modifier = Modifier.weight(0.85f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(17.dp))
                        Text("Muat Ulang", modifier = Modifier.padding(start = 6.dp), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onPaymentReceived,
                        enabled = !state.qrisPayload.isNullOrBlank() && !state.isGeneratingQris,
                        modifier = Modifier.weight(1.15f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9F70))
                    ) {
                        Text("Sudah Dibayar", fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.padding(start = 6.dp).size(17.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun QrisCodeImage(payload: String, modifier: Modifier = Modifier) {
    val bitmapResult = remember(payload) { runCatching { createQrisBitmap(payload) } }
    val bitmap = bitmapResult.getOrNull()

    if (bitmap != null) {
        Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Color.White) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QRIS dinamis untuk pembayaran",
                modifier = Modifier.padding(12.dp)
            )
        }
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = "QR tidak dapat dirender. Muat ulang untuk mencoba kembali.",
                color = Color(0xFFBE123C),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun createQrisBitmap(payload: String, size: Int = 720): Bitmap {
    val matrix = QRCodeWriter().encode(
        payload,
        BarcodeFormat.QR_CODE,
        size,
        size,
        mapOf(EncodeHintType.MARGIN to 1)
    )
    val pixels = IntArray(size * size)
    for (y in 0 until size) {
        val row = y * size
        for (x in 0 until size) {
            pixels[row + x] = if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
    }

    return createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, size, 0, 0, size, size)
    }
}
