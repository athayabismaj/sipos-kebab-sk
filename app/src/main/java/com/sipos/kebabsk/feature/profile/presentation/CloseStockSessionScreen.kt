package com.sipos.kebabsk.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.common.AppTime
import com.sipos.kebabsk.common.validation.ValidationResult
import com.sipos.kebabsk.feature.dailystock.domain.validation.DailyStockValidator
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipeAnchorInput
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipeGroup
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipeGroupVariant
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipePreset
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipePreview
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabCardBg
import com.sipos.kebabsk.ui.theme.KebabDivider
import com.sipos.kebabsk.ui.theme.KebabErrorText
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabPrimaryContainer
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray
import com.sipos.kebabsk.ui.theme.KebabInputBg
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

val KebabItemBg = Color(0xFFFFFFFF)
val KebabIconBg = Color(0xFFE7E1DD)
val KebabTertiaryContainer = Color(0xFF00B5FC)
val KebabOnTertiaryContainer = Color(0xFF004360)
val KebabYellowActive = Color(0xFFFFBF00)
val KebabError = Color(0xFFBA1A1A)

private val LocalKebabSecondary = Color(0xFF795900)
private val LocalKebabTertiary = Color(0xFF00658F)
private val LocalKebabInfoBg = KebabInputBg

@Composable
fun CloseStockSessionScreen(
    modifier: Modifier = Modifier,
    items: List<DailyStockItem>,
    isClosing: Boolean,
    closeErrorMessage: String?,
    closingPresets: List<ClosingRecipePreset> = emptyList(),
    closingGroups: List<ClosingRecipeGroup> = emptyList(),
    isPreviewingClosing: Boolean = false,
    closingPreview: ClosingRecipePreview? = null,
    closingPreviewError: String? = null,
    onBack: () -> Unit,
    onPreview: (List<ClosingRecipeAnchorInput>) -> Unit = {},
    onClearPreview: () -> Unit = {},
    onSubmit: (remaining: Map<Long, Double>, anchors: List<ClosingRecipeAnchorInput>, notes: String?) -> Unit
) {
    val remainingInputs = remember(items) {
        mutableStateMapOf<Long, String>().apply {
            items.forEach { item ->
                // Jika backend memberikan sisa real-time (remainingQty), gunakan itu sebagai prefill.
                // Jika tidak ada, fallback ke stok awal (qty).
                val prefillValue = item.remainingQty ?: item.qty
                put(item.ingredientId, formatInitialQty(prefillValue))
            }
        }
    }
    val notesInput = remember { mutableStateOf("") }
    var recipeAnchorEdited by remember { mutableStateOf(false) }
    val effectiveClosingGroups = remember(closingGroups, closingPresets) {
        if (closingGroups.isNotEmpty()) {
            closingGroups
        } else {
            closingPresets.mapIndexed { index, preset ->
                ClosingRecipeGroup(
                    groupId = -(index + 1L),
                    label = preset.anchorName,
                    anchorIngredientId = preset.anchorIngredientId,
                    anchorName = preset.anchorName,
                    anchorUnit = preset.anchorUnit,
                    systemRemaining = preset.systemRemaining,
                    defaultMenuVariantId = preset.menuVariantId,
                    requiresAllocation = false,
                    ready = preset.ready,
                    variants = listOf(
                        ClosingRecipeGroupVariant(
                            menuVariantId = preset.menuVariantId,
                            label = preset.label,
                            anchorQuantity = preset.quantityPerServing,
                            isDefault = true
                        )
                    )
                )
            }
        }
    }
    val anchorInputs = remember(effectiveClosingGroups) {
        mutableStateMapOf<Long, String>().apply {
            effectiveClosingGroups.forEach {
                put(it.anchorIngredientId, formatInitialQty(it.systemRemaining))
            }
        }
    }
    val selectedVariants = remember(effectiveClosingGroups) {
        mutableStateMapOf<Long, Long>().apply {
            effectiveClosingGroups.forEach { group ->
                if (!group.requiresAllocation || group.variants.size == 1) {
                    put(group.anchorIngredientId, group.defaultMenuVariantId)
                }
            }
        }
    }
    var variantPickerGroup by remember { mutableStateOf<ClosingRecipeGroup?>(null) }
    var step by remember { mutableIntStateOf(1) } // 1: input, 2: review
    val dailyStockValidator = remember { DailyStockValidator() }

    val hasInvalidInput = items.any { item ->
        val value = remainingInputs[item.ingredientId]?.toDoubleOrNull()
        dailyStockValidator.validateRemainingQuantity(value) is ValidationResult.Invalid
    }
    val anchors = effectiveClosingGroups.mapNotNull { group ->
        val selectedVariantId = selectedVariants[group.anchorIngredientId]
            ?: return@mapNotNull null
        anchorInputs[group.anchorIngredientId]?.toDoubleOrNull()?.let {
            ClosingRecipeAnchorInput(selectedVariantId, it)
        }
    }
    val recipeModeReady = !recipeAnchorEdited ||
        (closingPreview != null && anchors.size == effectiveClosingGroups.size)

    LaunchedEffect(anchors, effectiveClosingGroups, recipeAnchorEdited) {
        if (recipeAnchorEdited && effectiveClosingGroups.isNotEmpty() &&
            anchors.size == effectiveClosingGroups.size &&
            effectiveClosingGroups.all { it.ready }
        ) {
            delay(450)
            onPreview(anchors)
        }
    }

    LaunchedEffect(closingPreview) {
        closingPreview?.remainingItems?.forEach { result ->
            remainingInputs[result.ingredientId] = formatInitialQty(result.remainingQty)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KebabBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // === TOP BAR ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (step == 2) step = 1 else onBack()
                    },
                    enabled = !isClosing
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = KebabPrimary
                    )
                }
                Text(
                    text = if (step == 1) "Input Sisa Bahan" else "Review Tutup Sesi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KebabPrimary
                )
                Spacer(modifier = Modifier.width(48.dp)) // Menjaga judul di tengah
            }

            // === SCROLLABLE CONTENT ===
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalArrangement = if (step == 1) Arrangement.spacedBy(8.dp) else Arrangement.Top,
                horizontalAlignment = if (step == 2) Alignment.CenterHorizontally else Alignment.Start
            ) {
                // Shared Header (Stepper + Error)
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    StepperSection(step = step)
                        Spacer(modifier = Modifier.height(16.dp))

                    val displayedError = closeErrorMessage ?: closingPreviewError
                    if (!displayedError.isNullOrBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = KebabErrorText.copy(alpha = 0.05f))
                        ) {
                            Text(
                                text = displayedError,
                                style = MaterialTheme.typography.bodySmall,
                                color = KebabErrorText,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                if (step == 1) {
                    // --- STEP 1: INPUT SISA ---
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Stok fisik akhir",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KebabTextDark
                                )
                                Text(
                                    text = "Isi jumlah yang benar-benar tersisa.",
                                    fontSize = 12.sp,
                                    color = KebabTextGray
                                )
                            }
                            Text(
                                text = "${items.size} bahan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KebabPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(KebabPrimary.copy(alpha = 0.08f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(KebabPrimary.copy(alpha = 0.06f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isPreviewingClosing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = KebabPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (recipeAnchorEdited && closingPreview != null) Icons.Default.Check else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = KebabPrimary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(9.dp))
                            Text(
                                text = when {
                                    isPreviewingClosing -> "Menyesuaikan sisa bahan terkait..."
                                    recipeAnchorEdited && closingPreview != null -> "Bahan terkait sudah disesuaikan dari resep."
                                    else -> "Masukkan sisa fisik seperti biasa. Perhitungan resep berjalan otomatis saat diperlukan."
                                },
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = KebabTextGray
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(items.size, key = { index -> items[index].ingredientId }) { index ->
                        val item = items[index]
                        val closingGroup = effectiveClosingGroups.firstOrNull {
                            it.anchorIngredientId == item.ingredientId
                        }
                        val selectedVariant = closingGroup?.variants?.firstOrNull {
                            it.menuVariantId == selectedVariants[closingGroup.anchorIngredientId]
                        }
                        
                        val sisa = remainingInputs[item.ingredientId]?.toDoubleOrNull() ?: 0.0
                        val terpakaiVal = (item.qty - sisa).coerceAtLeast(0.0)
                        
                        // Tandai sedang tidak ada perubahan jika nilainya persis sama seperti qty (belum terpakai)
                        // atau sesuai dengan remainingQty dari backend jika user belum input apa-apa.
                        val isEdited = remainingInputs.containsKey(item.ingredientId) && remainingInputs[item.ingredientId] != ""
                        // format terpakai or "--"
                        val terpakaiFormat = if (isEdited) formatDisplayQty(terpakaiVal) else "--"

                        BahanInputCard(
                            title = item.name,
                            stokAwal = "${formatDisplayQty(item.qty)} ${item.unit ?: "unit"}",
                            sisaValue = remainingInputs[item.ingredientId] ?: "",
                            maxValue = Double.MAX_VALUE,
                            onSisaChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    remainingInputs[item.ingredientId] = newValue
                                    if (closingGroup != null) {
                                        recipeAnchorEdited = true
                                        anchorInputs[closingGroup.anchorIngredientId] = newValue
                                        onClearPreview()
                                        if (selectedVariants[closingGroup.anchorIngredientId] == null) {
                                            variantPickerGroup = closingGroup
                                        }
                                    }
                                }
                            },
                            satuan = (item.unit ?: "unit").uppercase(Locale.ROOT),
                            terpakai = terpakaiFormat,
                            recipeVariantLabel = selectedVariant?.label
                                ?: closingGroup?.takeIf { it.requiresAllocation }?.let { "Pilih varian resep" },
                            onRecipeVariantClick = if (closingGroup != null && closingGroup.variants.size > 1) {
                                { variantPickerGroup = closingGroup }
                            } else null
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Catatan Info Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LocalKebabInfoBg)
                                .border(1.dp, KebabDivider, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Catatan Penutupan Shift", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pastikan timbangan telah dikalibrasi (tare) sebelum mengukur sisa. Laporan terpakai akan otomatis masuk ke Jurnal Inventori.",
                                    fontSize = 13.sp,
                                    color = KebabTextGray,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                } else {
                    // --- STEP 2: REVIEW TUTUP SESI ---
                    item {
                        RingkasanSesiCard()
                        Spacer(modifier = Modifier.height(18.dp))
                        DetailPenggunaanSection(items = items, remainingInputs = remainingInputs)
                        Spacer(modifier = Modifier.height(18.dp))
                        
                        // Catatan Sesi Pindah Ke Step 2 Sesuai Mockup
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Catatan Sesi",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = KebabTextDark,
                                modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(104.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFF0E4DB), RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                if (notesInput.value.isEmpty()) {
                                    Text(
                                        text = "Tambahkan catatan khusus untuk shift ini (opsional)...",
                                        color = KebabTextGray.copy(alpha = 0.5f),
                                        fontSize = 14.sp
                                    )
                                }
                                BasicTextField(
                                    value = notesInput.value,
                                    onValueChange = { notesInput.value = it },
                                    modifier = Modifier.fillMaxSize(),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 14.sp,
                                        color = KebabTextDark
                                    ),
                                    enabled = !isClosing
                                )
                            }
                        }
                    }
                }
                
                // Bottom Spacing (Buat Floating Button)
                item {
                    Spacer(modifier = Modifier.height(if (step == 1) 220.dp else 280.dp))
                }
            }
        }

        // --- FLOATING ACTION BUTTONS ---
        if (step == 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .imePadding()
                    .background(KebabBg.copy(alpha = 0.9f))
                    .border(1.dp, KebabDivider, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 108.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                    // Tombol Kembali
                    Row(
                        modifier = Modifier.clickable { onBack() }.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = KebabTextDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Kembali", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = KebabTextDark)
                    }

                    // Tombol Lanjut Review
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                            .height(56.dp)
                            .shadow(
                                elevation = if (!isClosing && items.isNotEmpty() && !hasInvalidInput && recipeModeReady) 8.dp else 0.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = KebabPrimaryContainer
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (!isClosing && items.isNotEmpty() && !hasInvalidInput && recipeModeReady)
                                    Brush.horizontalGradient(listOf(KebabPrimary, KebabPrimaryContainer))
                                else
                                    Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.5f), Color.Gray.copy(alpha = 0.5f)))
                            )
                            .clickable(enabled = !isClosing && items.isNotEmpty() && !hasInvalidInput && recipeModeReady) { step = 2 },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Lanjut ke\nReview", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            } else {
                // Tombol Step 2
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .imePadding()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, KebabBg, KebabBg)))
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp, bottom = 108.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                        onClick = { step = 1 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isClosing,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, KebabPrimary.copy(alpha = 0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KebabPrimary)
                    ) {
                        Text("Ubah Input", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = KebabPrimaryContainer)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(KebabPrimary, KebabPrimaryContainer)))
                            .clickable(enabled = !isClosing && !hasInvalidInput) {
                                val remaining = mutableMapOf<Long, Double>()
                                items.forEach { item ->
                                    val value = remainingInputs[item.ingredientId]?.toDoubleOrNull()
                                    if (dailyStockValidator.validateRemainingQuantity(value) is ValidationResult.Invalid) {
                                        return@clickable
                                    }
                                    remaining[item.ingredientId] = value ?: return@clickable
                                }
                                val notes = notesInput.value.trim().takeIf { it.isNotBlank() }
                                onSubmit(remaining, if (recipeAnchorEdited) anchors else emptyList(), notes)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isClosing) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp).padding(end = 8.dp))
                            Text("Memproses...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Text(text = "Tutup Sesi Sekarang", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        variantPickerGroup?.let { group ->
            RecipeVariantPickerDialog(
                group = group,
                selectedVariantId = selectedVariants[group.anchorIngredientId],
                onDismiss = { variantPickerGroup = null },
                onSelect = { variant ->
                    selectedVariants[group.anchorIngredientId] = variant.menuVariantId
                    variantPickerGroup = null
                    if (recipeAnchorEdited) {
                        onClearPreview()
                    }
                }
            )
        }
    }
}

@Composable
private fun RecipeVariantPickerDialog(
    group: ClosingRecipeGroup,
    selectedVariantId: Long?,
    onDismiss: () -> Unit,
    onSelect: (ClosingRecipeGroupVariant) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Column {
                Text(
                    text = "Pilih resep",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KebabTextDark
                )
                Text(
                    text = "${group.anchorName} dipakai oleh beberapa varian.",
                    fontSize = 12.sp,
                    color = KebabTextGray
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(group.variants, key = { it.menuVariantId }) { variant ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (variant.menuVariantId == selectedVariantId) {
                                    KebabPrimary.copy(alpha = 0.08f)
                                } else {
                                    KebabInputBg
                                }
                            )
                            .clickable { onSelect(variant) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = variant.menuVariantId == selectedVariantId,
                            onClick = { onSelect(variant) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = variant.label,
                            modifier = Modifier.weight(1f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KebabTextDark
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

