package com.sipos.kebabsk.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Step 1: Rekap Fisik
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
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
            Text(
                text = "Rekap Fisik", 
                fontSize = 12.sp, 
                fontWeight = FontWeight.SemiBold, 
                color = if (isActive1 || isDone1) KebabPrimary else KebabTextGray,
                maxLines = 1,
                softWrap = false
            )
        }

        // Line
        HorizontalDivider(
            modifier = Modifier.width(40.dp).padding(top = 16.dp),
            color = if (step > 1) KebabPrimary.copy(alpha = 0.3f) else KebabDivider,
            thickness = 2.dp
        )

        // Step 2: Review
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
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
            Text(
                text = "Review", 
                fontSize = 12.sp, 
                fontWeight = if (isActive2) FontWeight.Bold else FontWeight.SemiBold, 
                color = if (isActive2) KebabTextDark else KebabTextGray,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}


@Composable
fun RingkasanSesiCard() {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
    // Menggunakan business date agar tanggal tidak otomatis berubah saat jam 12 malam sampai toleransi jam 4 pagi
    val hariIniStr = AppTime.businessDateJakarta().format(dateFormatter)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF0E4DB), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ringkasan Sesi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("TANGGAL SESI", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = KebabTextGray, letterSpacing = 1.sp)
                    Text(hariIniStr, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KebabTextDark, modifier = Modifier.padding(top = 3.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("STATUS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = KebabTextGray, letterSpacing = 1.sp)
                    Text("Akan Ditutup", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KebabPrimary, modifier = Modifier.padding(top = 3.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Profil Kasir Bertugas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(KebabCardBg)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(KebabTertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = KebabOnTertiaryContainer)
                }
                Spacer(modifier = Modifier.width(10.dp))
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
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = KebabTextDark,
            modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFF0E4DB))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(KebabInputBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = KebabTextDark,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(KebabError.copy(alpha = 0.08f))
                        .padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = KebabError, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$terpakai $unit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KebabError)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(KebabInputBg)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text("Stok awal", fontSize = 10.sp, color = KebabTextGray)
                    Text("$stockAwal $unit", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(KebabPrimary.copy(alpha = 0.07f))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text("Sisa akhir", fontSize = 10.sp, color = KebabTextGray)
                    Text("$sisaAkhir $unit", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KebabPrimary)
                }
            }
        }
    }
}

@Composable
fun BahanInputCard(
    title: String,
    stokAwal: String,
    sisaValue: String,
    maxValue: Double = Double.MAX_VALUE,
    onSisaChange: (String) -> Unit,
    satuan: String,
    terpakai: String,
    recipeVariantLabel: String? = null,
    onRecipeVariantClick: (() -> Unit)? = null
) {
    val inputStep = if (satuan in setOf("KG", "GR", "GRAM", "L", "ML")) 0.01 else 1.0
    fun updateSisa(delta: Double) {
        val currentValue = sisaValue.toDoubleOrNull() ?: 0.0
        val nextValue = (currentValue + delta).coerceIn(0.0, maxValue)
        val nextText = if (inputStep < 1.0) {
            String.format(Locale.US, "%.2f", nextValue)
        } else {
            nextValue.toInt().toString()
        }
        onSisaChange(nextText)
    }

    val terpakaiText = if (terpakai == "--") terpakai else "$terpakai $satuan"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, KebabDivider)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KebabTextDark, maxLines = 2)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = "Awal $stokAwal", fontSize = 11.sp, color = KebabTextGray)
                    if (!recipeVariantLabel.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .then(
                                    if (onRecipeVariantClick != null) {
                                        Modifier.clickable(onClick = onRecipeVariantClick)
                                    } else {
                                        Modifier
                                    }
                                )
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Resep: $recipeVariantLabel",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KebabPrimary,
                                maxLines = 2
                            )
                            if (onRecipeVariantClick != null) {
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Pilih resep",
                                    tint = KebabPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Terpakai $terpakaiText",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (terpakai == "--") KebabTextGray else KebabPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(KebabInputBg)
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                )
            }

            HorizontalDivider(color = KebabDivider.copy(alpha = 0.7f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sisa fisik",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KebabTextDark
                    )
                    Text(
                        text = satuan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = KebabTextGray
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(KebabInputBg)
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { updateSisa(-inputStep) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Kurangi sisa", tint = KebabPrimary, modifier = Modifier.size(18.dp))
                    }
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = sisaValue,
                            onValueChange = onSisaChange,
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = KebabTextDark,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    innerTextField()
                                }
                            }
                        )
                    }
                    IconButton(
                        onClick = { updateSisa(inputStep) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(KebabPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah sisa", tint = Color.White, modifier = Modifier.size(18.dp))
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


