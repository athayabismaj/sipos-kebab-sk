package com.sipos.kebabsk.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun StepperSection(step: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Step 1: Rekap Fisik
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
            val isActive1 = step == 1
            val isDone1 = step > 1
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isActive1 || isDone1) KebabPrimary else Color.White)
                    .border(
                        4.dp,
                        if (isActive1 || isDone1) KebabPrimary.copy(alpha = 0.2f) else KebabDivider,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone1) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text("1", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Rekap Fisik", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isActive1 || isDone1) KebabPrimary else KebabTextGray)
        }

        // Line
        HorizontalDivider(
            modifier = Modifier.width(40.dp).padding(bottom = 20.dp),
            color = if (step > 1) KebabPrimary.copy(alpha = 0.3f) else KebabDivider,
            thickness = 2.dp
        )

        // Step 2: Review
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
            val isActive2 = step == 2
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isActive2) KebabYellowActive else Color.White)
                    .border(
                        4.dp,
                        if (isActive2) KebabYellowActive.copy(alpha = 0.2f) else KebabDivider,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("2", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isActive2) KebabTextDark else KebabTextGray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Review", fontSize = 12.sp, fontWeight = if (isActive2) FontWeight.Bold else FontWeight.SemiBold, color = if (isActive2) KebabTextDark else KebabTextGray)
        }
    }
}


@Composable
fun RingkasanSesiCard() {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
    val hariIniStr = AppTime.todayJakarta().format(dateFormatter)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ringkasan Sesi", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("TANGGAL SESI", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = KebabTextGray, letterSpacing = 1.sp)
                    Text(hariIniStr, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KebabTextDark, modifier = Modifier.padding(vertical = 4.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("STATUS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = KebabTextGray, letterSpacing = 1.sp)
                    Text("Akan Ditutup", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KebabPrimary, modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Profil Kasir Bertugas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(KebabCardBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(KebabTertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = KebabOnTertiaryContainer)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Sesi Operasional", fontSize = 12.sp, color = KebabTextGray)
                    Text("Harian", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                }
            }
        }
    }
}

@Composable
fun DetailPenggunaanSection(
    items: List<DailyStockItem>,
    remainingInputs: Map<Long, String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Detail Penggunaan",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = KebabTextDark,
            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEachIndexed { index, item ->
                val sisa = remainingInputs[item.ingredientId]?.toDoubleOrNull() ?: 0.0
                val used = (item.qty - sisa).coerceAtLeast(0.0)
                
                // Rotasi icon untuk dummy mockup behavior (bisa menggunakan icon default API jika tersedia)
                val iconList = listOf(Icons.Default.LocalCafe, Icons.Default.WaterDrop, Icons.Default.Spa)
                val assignedIcon = iconList[index % iconList.size]

                PenggunaanItemCard(
                    title = item.name,
                    icon = assignedIcon,
                    stockAwal = formatDisplayQty(item.qty),
                    sisaAkhir = formatDisplayQty(sisa),
                    terpakai = formatDisplayQty(used),
                    unit = item.unit ?: "unit"
                )
            }
        }
    }
}

@Composable
fun PenggunaanItemCard(
    title: String, 
    icon: ImageVector, 
    stockAwal: String, 
    sisaAkhir: String, 
    terpakai: String, 
    unit: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(KebabCardBg)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(KebabItemBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = KebabTextGray, modifier = Modifier.size(20.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = KebabTextDark)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Stock Awal", fontSize = 14.sp, color = KebabTextGray)
                Text("$stockAwal $unit", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = KebabTextDark)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Sisa Akhir", fontSize = 14.sp, color = KebabTextGray)
                Text("$sisaAkhir $unit", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = KebabTextDark)
            }
            
            HorizontalDivider(color = KebabDivider, modifier = Modifier.padding(vertical = 12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = KebabError, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Terpakai", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = KebabError)
                }
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = KebabTextDark)) { append(terpakai) }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, color = KebabTextGray)) { append(" $unit") }
                    }
                )
            }
        }
    }
}

@Composable
fun BahanInputCard(
    title: String,
    stokAwal: String,
    sisaValue: String,
    onSisaChange: (String) -> Unit,
    satuan: String,
    terpakai: String,
    icon: ImageVector,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        // Decorative Shape Kanan Atas
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(80.dp)
                .offset(x = 16.dp, y = (-16).dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f))
        )

        Column(modifier = Modifier.padding(20.dp)) {
            // Header Card
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(text = "Stok Awal: ", fontSize = 14.sp, color = KebabTextGray)
                        Text(text = stokAwal, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                    }
                }
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(KebabInputBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input Sisa & Kalkulasi Terpakai
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                // Input Sisa
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "SISA AKHIR ($satuan)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = KebabTextGray, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(KebabInputBg)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = sisaValue,
                            onValueChange = onSisaChange,
                            textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = KebabTextDark),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Badge Terpakai
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Terpakai", fontSize = 10.sp, color = KebabTextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (terpakai == "--") {
                        // Badge kosong (belum diinput)
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(50)).border(1.dp, KebabDivider, RoundedCornerShape(50)).padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(text = "--", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = KebabTextGray.copy(alpha = 0.5f))
                        }
                    } else {
                        // Badge Terisi
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0xFFE7E1DD)).padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = terpakai, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = KebabTextDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}


internal fun formatDisplayQty(qty: Double): String {
    return if ((qty % 1.0).absoluteValue < 0.000001) {
        qty.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.2f", qty)
    }
}

internal fun formatInitialQty(qty: Double): String {
    return if ((qty % 1.0).absoluteValue < 0.000001) {
        qty.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.2f", qty)
    }
}

// Helper to fix conflict with Modifier.border vs function border 
private fun border(width: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(width, color)
}


