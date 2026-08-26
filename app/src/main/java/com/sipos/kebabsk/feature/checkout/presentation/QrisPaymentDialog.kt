package com.sipos.kebabsk.feature.checkout.presentation

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode2
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
import com.sipos.kebabsk.ui.theme.KebabDivider
import com.sipos.kebabsk.ui.theme.KebabInputBg
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun QrisPaymentDialog(
    state: CheckoutUiState,
    onRetry: () -> Unit,
    onPaymentReceived: () -> Unit
) {
    val expiresAt = remember(state.qrisExpiresAt) {
        state.qrisExpiresAt?.let { runCatching { OffsetDateTime.parse(it) }.getOrNull() }
    }
    val isLocallyExpired = expiresAt?.isBefore(OffsetDateTime.now()) == true

    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 390.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE8F7F1), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color(0xFF087F5B), modifier = Modifier.size(21.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pembayaran QRIS", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = KebabTextDark)
                        Text(
                            text = state.qrisMerchantName?.takeIf(String::isNotBlank)
                                ?: state.qrisBranchName?.takeIf(String::isNotBlank)
                                ?: "Merchant cabang",
                            fontSize = 11.sp,
                            color = KebabTextGray,
                            maxLines = 2
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (isLocallyExpired) Color(0xFFFFF1F2) else Color(0xFFE8F7F1)
                    ) {
                        Text(
                            text = if (isLocallyExpired) "Kedaluwarsa" else "Menunggu",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            color = if (isLocallyExpired) Color(0xFFBE123C) else Color(0xFF087F5B),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = KebabInputBg
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                        Text("Total pembayaran", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = KebabTextGray)
                        Text(
                            text = MoneyUtils.formatRupiah(state.qrisAmount ?: state.checkoutTotalAmount ?: 0L),
                            modifier = Modifier.padding(top = 2.dp),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = KebabTextDark
                        )
                    }
                }

                when {
                    state.isGeneratingQris -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(238.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = KebabPrimary, strokeWidth = 2.5.dp)
                                Text("Menyiapkan QR...", modifier = Modifier.padding(top = 12.dp), color = KebabTextGray, fontSize = 12.sp)
                            }
                        }
                    }

                    !state.qrisPayload.isNullOrBlank() -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            QrisCodeImage(
                                payload = state.qrisPayload,
                                modifier = Modifier
                                    .fillMaxWidth(0.76f)
                                    .aspectRatio(1f)
                            )
                        }
                        Text(
                            text = if (isLocallyExpired) {
                                "QR telah kedaluwarsa. Buat QR baru untuk melanjutkan."
                            } else {
                                "Pindai QR, lalu pastikan pembayaran masuk sebelum dikonfirmasi."
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                            color = KebabTextGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    else -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp),
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

                state.qrisExpiresAt?.let {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, KebabDivider)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                            Text(
                                text = expiresAt?.format(DateTimeFormatter.ofPattern("dd MMM, HH:mm:ss"))
                                    ?.let { formatted -> "Berlaku sampai $formatted" }
                                    ?: "Masa berlaku mengikuti server",
                                color = if (isLocallyExpired) Color(0xFFBE123C) else KebabTextGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            state.qrisReference?.let { reference ->
                                Text("Ref. $reference", modifier = Modifier.padding(top = 2.dp), color = Color(0xFF98A2B3), fontSize = 9.sp)
                            }
                        }
                    }
                }

                if (!state.qrisErrorMessage.isNullOrBlank() &&
                    !state.qrisPayload.isNullOrBlank() &&
                    !state.isGeneratingQris
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF1F2)
                    ) {
                        Text(
                            text = state.qrisErrorMessage,
                            modifier = Modifier.padding(12.dp),
                            color = Color(0xFFBE123C),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = onPaymentReceived,
                    enabled = !state.qrisPayload.isNullOrBlank() &&
                        !state.qrisReference.isNullOrBlank() &&
                        !state.isGeneratingQris &&
                        !state.isConfirmingQris &&
                        !isLocallyExpired,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F5B))
                ) {
                    Text(if (state.isConfirmingQris) "Memverifikasi..." else "Konfirmasi Pembayaran", fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.padding(start = 7.dp).size(17.dp))
                }
                OutlinedButton(
                    onClick = onRetry,
                    enabled = !state.isGeneratingQris && !state.isConfirmingQris,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(44.dp),
                    shape = RoundedCornerShape(13.dp),
                    border = BorderStroke(1.dp, KebabDivider)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(if (isLocallyExpired) "Buat QR Baru" else "Muat Ulang QR", modifier = Modifier.padding(start = 7.dp), fontWeight = FontWeight.Bold)
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
        Surface(
            modifier = modifier.border(1.dp, KebabDivider, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
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
