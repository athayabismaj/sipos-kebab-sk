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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun RingkasanSesiCard(
    totalIngredients: Int,
    usedIngredients: Int
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
    val hariIniStr = AppTime.businessDateJakarta().format(dateFormatter)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, KebabDivider)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KebabPrimary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = KebabPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ringkasan sesi", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                    Text("Periksa kembali sebelum menutup", fontSize = 11.sp, color = KebabTextGray)
                }
                Text(
                    text = "Siap ditutup",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = KebabPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(KebabPrimary.copy(alpha = 0.08f))
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 14.dp),
                color = KebabDivider
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReviewMetric(
                    label = "Tanggal",
                    value = hariIniStr,
                    modifier = Modifier.weight(1.45f)
                )
                ReviewMetric(
                    label = "Diperiksa",
                    value = "$totalIngredients bahan",
                    modifier = Modifier.weight(1f)
                )
                ReviewMetric(
                    label = "Terpakai",
                    value = "$usedIngredients bahan",
                    modifier = Modifier.weight(1f),
                    accent = usedIngredients > 0
                )
            }
        }
    }
}

@Composable
private fun ReviewMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (accent) KebabPrimary.copy(alpha = 0.07f) else KebabInputBg)
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = KebabTextGray)
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (accent) KebabPrimary else KebabTextDark,
            maxLines = 1
        )
    }
}

private data class ReviewUsageItem(
    val title: String,
    val stockAwal: String,
    val sisaAkhir: String,
    val terpakai: String,
    val usedValue: Double,
    val unit: String,
    val usedUnit: String
)

@Composable
fun DetailPenggunaanSection(
    items: List<DailyStockItem>,
    remainingInputs: Map<Long, String>
) {
    val usageItems = items.map { item ->
        val sisa = remainingInputs[item.ingredientId]?.toDoubleOrNull() ?: 0.0
        val used = (item.qty - sisa).coerceAtLeast(0.0)
        val convertedUsage = convertUsedQuantityForReview(used, item.unit)
        ReviewUsageItem(
            title = item.name,
            stockAwal = formatDisplayQty(item.qty),
            sisaAkhir = formatDisplayQty(sisa),
            terpakai = formatDisplayQty(convertedUsage.first),
            usedValue = used,
            unit = item.unit ?: "unit",
            usedUnit = convertedUsage.second
        )
    }
    val changedItems = usageItems.filter { it.usedValue > 0.000001 }
    val unchangedItems = usageItems.filterNot { it.usedValue > 0.000001 }
    var showUnchanged by remember(items) { mutableStateOf(false) }
    val visibleItems = if (showUnchanged) changedItems + unchangedItems else changedItems

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Pemakaian bahan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                Text("${changedItems.size} dari ${items.size} bahan berubah", fontSize = 11.sp, color = KebabTextGray)
            }
            Text(
                text = "${changedItems.size} terpakai",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = KebabPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(KebabPrimary.copy(alpha = 0.08f))
                    .padding(horizontal = 9.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, KebabDivider)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (visibleItems.isEmpty()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Tidak ada bahan yang terpakai.", fontSize = 12.sp, color = KebabTextGray)
                    }
                } else {
                    visibleItems.forEachIndexed { index, usage ->
                        PenggunaanItemCard(
                            title = usage.title,
                            stockAwal = usage.stockAwal,
                            sisaAkhir = usage.sisaAkhir,
                            terpakai = usage.terpakai,
                            usedValue = usage.usedValue,
                            unit = usage.unit,
                            usedUnit = usage.usedUnit
                        )
                        if (index < visibleItems.lastIndex) {
                            HorizontalDivider(color = KebabDivider.copy(alpha = 0.7f))
                        }
                    }
                }

                if (unchangedItems.isNotEmpty()) {
                    if (visibleItems.isNotEmpty()) HorizontalDivider(color = KebabDivider)
                    TextButton(
                        onClick = { showUnchanged = !showUnchanged },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (showUnchanged) {
                                "Sembunyikan bahan tanpa perubahan"
                            } else {
                                "Tampilkan ${unchangedItems.size} bahan tanpa perubahan"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KebabPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PenggunaanItemCard(
    title: String,
    stockAwal: String,
    sisaAkhir: String,
    terpakai: String,
    usedValue: Double,
    unit: String,
    usedUnit: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (usedValue > 0.000001) KebabPrimary.copy(alpha = 0.08f) else KebabInputBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = if (usedValue > 0.000001) KebabPrimary else KebabTextGray,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KebabTextDark, maxLines = 2)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$stockAwal → $sisaAkhir ${unit.lowercase(Locale.ROOT)}",
                fontSize = 10.sp,
                color = KebabTextGray
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text("Terpakai", fontSize = 9.sp, color = KebabTextGray)
            Text(
                text = "$terpakai $usedUnit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (usedValue > 0.000001) KebabPrimary else KebabTextGray
            )
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
    hasUsage: Boolean,
    hasAutomaticRecipe: Boolean = false,
    isAutomaticRecipeChanged: Boolean = false,
    affectedIngredientCount: Int = 0,
    isRecipePreviewLoading: Boolean = false,
    recipePreviewError: String? = null,
    onRecipeDetailClick: (() -> Unit)? = null,
    onRecipeRetry: (() -> Unit)? = null,
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

    val usageBadgeText = when {
        terpakai == "--" -> "--"
        hasUsage -> "Terpakai $terpakai $satuan"
        else -> "Tidak berubah"
    }
    val usageBadgeHighlighted = terpakai != "--" && hasUsage

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
                    text = usageBadgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (usageBadgeHighlighted) KebabPrimary else KebabTextGray,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (usageBadgeHighlighted) KebabPrimary.copy(alpha = 0.08f) else KebabInputBg)
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

            if (hasAutomaticRecipe) {
                HorizontalDivider(color = KebabDivider.copy(alpha = 0.7f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KebabPrimary.copy(alpha = 0.06f))
                        .padding(horizontal = 11.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRecipePreviewLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = KebabPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = if (recipePreviewError == null) KebabPrimary else KebabError,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                isRecipePreviewLoading -> "Menghitung bahan terkait..."
                                recipePreviewError != null -> "Tidak dapat menghitung bahan terkait."
                                isAutomaticRecipeChanged && affectedIngredientCount > 0 ->
                                    "$affectedIngredientCount bahan akan dihitung otomatis"
                                isAutomaticRecipeChanged -> "Bahan resep akan dihitung otomatis"
                                else -> "Pengurangan resep otomatis"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (recipePreviewError == null) KebabTextDark else KebabError,
                            lineHeight = 15.sp
                        )
                        when {
                            recipePreviewError != null && onRecipeRetry != null -> {
                                Text(
                                    text = "Coba lagi",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KebabPrimary,
                                    modifier = Modifier.padding(top = 3.dp).clickable(onClick = onRecipeRetry)
                                )
                            }
                            affectedIngredientCount > 0 && onRecipeDetailClick != null -> {
                                Text(
                                    text = "Lihat detail",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KebabPrimary,
                                    modifier = Modifier.padding(top = 3.dp).clickable(onClick = onRecipeDetailClick)
                                )
                            }
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

internal fun convertUsedQuantityForReview(qty: Double, unit: String?): Pair<Double, String> {
    return when (unit?.trim()?.lowercase(Locale.ROOT)) {
        "kg", "kilogram" -> qty * 1_000.0 to "g"
        "l", "lt", "liter", "litre" -> qty * 1_000.0 to "ml"
        else -> qty to (unit?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotBlank() } ?: "unit")
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
