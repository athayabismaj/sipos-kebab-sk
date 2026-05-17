package com.sipos.kebabsk.feature.shift.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.feature.shift.data.remote.CloseSessionData
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabCardBg
import com.sipos.kebabsk.ui.theme.KebabDivider
import com.sipos.kebabsk.ui.theme.KebabErrorText
import com.sipos.kebabsk.ui.theme.KebabInputBg
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabPrimaryContainer
import com.sipos.kebabsk.ui.theme.KebabSuccess
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun CloseShiftScreen(
    modifier: Modifier = Modifier,
    uiState: CloseShiftUiState,
    onBack: () -> Unit,
    onSubmit: (actualPhysicalCash: Double, closingNotes: String?) -> Unit,
    onRetryReadiness: () -> Unit,
    onSessionClosed: () -> Unit // Callback ke parent untuk clear session + navigate login
) {
    var cashInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Tampilkan dialog rekonsiliasi jika hasil sudah ada
    if (uiState.reconciliationResult != null) {
        ReconciliationSummaryDialog(
            result = uiState.reconciliationResult,
            onFinish = onSessionClosed
        )
    }

    // Dialog konfirmasi sebelum submit
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(KebabPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = KebabPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    "Konfirmasi Tutup Shift",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Aksi ini bersifat final dan tidak dapat dibatalkan.",
                        textAlign = TextAlign.Center,
                        color = KebabTextGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Uang Kas Fisik: ${formatRupiahLocal(cashInput.toDoubleOrNull() ?: 0.0)}",
                        fontWeight = FontWeight.Bold,
                        color = KebabTextDark,
                        fontSize = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        val cash = cashInput.toDoubleOrNull() ?: return@Button
                        val notes = notesInput.trim().takeIf { it.isNotBlank() }
                        onSubmit(cash, notes)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KebabPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ya, Tutup Shift", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Batal", color = KebabTextGray)
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KebabBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // === TOP BAR ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    enabled = !uiState.isSubmitting
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = KebabPrimary
                    )
                }
                Text(
                    text = "Tutup Shift",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KebabPrimary
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // === SCROLLABLE CONTENT ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // --- LOADING STATE (cek kesiapan) ---
                if (uiState.isCheckingReadiness) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = KebabCardBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = KebabPrimary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                "Memeriksa kesiapan stok...",
                                color = KebabTextGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // --- WARNING BANNER ---
                if (!uiState.isCheckingReadiness && !uiState.readinessMessage.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.isReadyToClose) {
                                KebabSuccess.copy(alpha = 0.08f)
                            } else {
                                KebabErrorText.copy(alpha = 0.08f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (uiState.isReadyToClose) KebabSuccess else KebabErrorText,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (uiState.isReadyToClose) "Siap Ditutup" else "Belum Siap",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (uiState.isReadyToClose) KebabSuccess else KebabErrorText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.readinessMessage,
                                    fontSize = 13.sp,
                                    color = KebabTextGray,
                                    lineHeight = 18.sp
                                )
                                if (!uiState.isReadyToClose) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(
                                        onClick = onRetryReadiness,
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = KebabPrimary
                                        )
                                    ) {
                                        Text(
                                            "Periksa Ulang",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- INPUT UANG KAS FISIK ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Setoran Uang Kas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = KebabTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hitung uang fisik di laci kasir dan masukkan jumlahnya.",
                            fontSize = 13.sp,
                            color = KebabTextGray,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = cashInput,
                            onValueChange = { newValue ->
                                // Filter: hanya angka dan satu titik desimal
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    cashInput = newValue
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Uang Kas Fisik (Rp)") },
                            placeholder = { Text("Contoh: 500000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            enabled = uiState.isReadyToClose && !uiState.isSubmitting,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KebabPrimary,
                                unfocusedBorderColor = KebabDivider,
                                focusedLabelColor = KebabPrimary,
                                unfocusedContainerColor = KebabInputBg,
                                focusedContainerColor = Color.White
                            )
                        )

                        // Live preview
                        if (cashInput.isNotBlank()) {
                            val parsed = cashInput.toDoubleOrNull()
                            if (parsed != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = formatRupiahLocal(parsed),
                                    fontSize = 13.sp,
                                    color = KebabPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // --- CATATAN PENUTUPAN ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Catatan Penutupan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KebabTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tambahkan catatan jika ada kejadian khusus (opsional).",
                            fontSize = 13.sp,
                            color = KebabTextGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Misal: Ada selisih karena kembalian salah...") },
                            enabled = uiState.isReadyToClose && !uiState.isSubmitting,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KebabPrimary,
                                unfocusedBorderColor = KebabDivider,
                                focusedLabelColor = KebabPrimary,
                                unfocusedContainerColor = KebabInputBg,
                                focusedContainerColor = Color.White
                            )
                        )
                    }
                }

                // --- ERROR MESSAGE ---
                if (!uiState.errorMessage.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = KebabErrorText.copy(alpha = 0.08f)
                        )
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            modifier = Modifier.padding(16.dp),
                            color = KebabErrorText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                // --- SUBMIT BUTTON ---
                val canSubmit = uiState.isReadyToClose &&
                        !uiState.isSubmitting &&
                        !uiState.isCheckingReadiness &&
                        cashInput.isNotBlank() &&
                        (cashInput.toDoubleOrNull() ?: -1.0) >= 0.0

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = if (canSubmit) 8.dp else 0.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = KebabPrimaryContainer
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (canSubmit) {
                                Brush.horizontalGradient(listOf(KebabPrimary, KebabPrimaryContainer))
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Gray.copy(alpha = 0.4f),
                                        Color.Gray.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        )
                        .then(
                            if (canSubmit) {
                                Modifier.border(0.dp, Color.Transparent, RoundedCornerShape(16.dp))
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        enabled = canSubmit,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Memproses...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        } else {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (canSubmit) Color.White else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tutup Shift Sekarang",
                                color = if (canSubmit) Color.White else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Dialog hasil rekonsiliasi.
 * Semua angka finansial berasal mentah dari API (SSOT).
 * Warna: Hijau jika variance == 0 (PAS), Merah tebal jika != 0.
 */
@Composable
private fun ReconciliationSummaryDialog(
    result: CloseSessionData,
    onFinish: () -> Unit
) {
    val varianceColor = if (result.variance == 0.0) KebabSuccess else KebabErrorText
    val varianceLabel = when {
        result.variance == 0.0 -> "PAS"
        result.variance > 0.0 -> "LEBIH"
        else -> "KURANG"
    }

    AlertDialog(
        onDismissRequest = { /* Tidak bisa dismiss — harus tekan Selesai */ },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(KebabSuccess.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = KebabSuccess,
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        title = {
            Text(
                text = "Shift Berhasil Ditutup",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Hasil Rekonsiliasi Kas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KebabTextGray
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Uang Sistem
                ReconciliationRow(
                    label = "Uang Sistem",
                    value = formatRupiahLocal(result.systemCash),
                    valueColor = KebabTextDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Uang Fisik
                ReconciliationRow(
                    label = "Uang Fisik",
                    value = formatRupiahLocal(result.actualCash),
                    valueColor = KebabTextDark
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = KebabDivider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Variance — warna tergantung nilai
                Text(
                    text = "SELISIH ($varianceLabel)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = varianceColor,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = buildVarianceText(result.variance),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = varianceColor
                )

                if (result.variance != 0.0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ada selisih kas. Silakan periksa catatan penutupan.",
                        fontSize = 12.sp,
                        color = KebabTextGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KebabPrimary)
            ) {
                Text(
                    "Selesai",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    )
}

@Composable
private fun ReconciliationRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = KebabTextGray
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

private fun formatRupiahLocal(value: Double): String {
    return NumberFormat
        .getCurrencyInstance(Locale.forLanguageTag("id-ID"))
        .format(value)
}

private fun buildVarianceText(variance: Double): String {
    val prefix = when {
        variance > 0.0 -> "+"
        variance < 0.0 -> "-"
        else -> ""
    }
    return "${prefix}${formatRupiahLocal(variance.absoluteValue)}"
}
