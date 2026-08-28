package com.sipos.kebabsk.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
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
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipeAffectedIngredient
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipeGroupVariant
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipePreset
import com.sipos.kebabsk.feature.dailystock.domain.model.ClosingRecipePreview
import com.sipos.kebabsk.feature.dailystock.domain.model.CashReconciliation
import com.sipos.kebabsk.feature.menu.domain.model.DailyStockItem
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabCardBg
import com.sipos.kebabsk.ui.theme.KebabDivider
import com.sipos.kebabsk.ui.theme.KebabErrorText
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray
import com.sipos.kebabsk.ui.theme.KebabInputBg
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.text.NumberFormat
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

private data class RecipeAllocation(
    val menuVariantId: Long,
    val quantity: Double
)

private data class PendingRecipeAllocation(
    val group: ClosingRecipeGroup,
    val previousRemaining: Double,
    val newRemaining: Double,
    val quantity: Double
)

private fun rollbackRecipeAllocations(
    entries: List<RecipeAllocation>,
    restoredQuantity: Double
): List<RecipeAllocation> {
    var quantityToRestore = restoredQuantity.coerceAtLeast(0.0)
    val result = entries.toMutableList()
    while (quantityToRestore > 0.000001 && result.isNotEmpty()) {
        val lastIndex = result.lastIndex
        val last = result[lastIndex]
        if (last.quantity <= quantityToRestore + 0.000001) {
            quantityToRestore -= last.quantity
            result.removeAt(lastIndex)
        } else {
            result[lastIndex] = last.copy(quantity = last.quantity - quantityToRestore)
            quantityToRestore = 0.0
        }
    }
    return result.filter { it.quantity > 0.000001 }
}

private fun aggregateAffectedIngredients(
    preview: ClosingRecipePreview?,
    anchorIngredientId: Long,
    allocatedVariantIds: Set<Long>
): List<ClosingRecipeAffectedIngredient> {
    return preview?.summaries.orEmpty()
        .filter { summary ->
            summary.anchorIngredientId == anchorIngredientId ||
                (summary.anchorIngredientId == null && summary.menuVariantId in allocatedVariantIds)
        }
        .flatMap { it.affectedIngredients }
        .filter { it.ingredientId != anchorIngredientId && it.usedQty > 0.0 }
        .groupBy { it.ingredientId }
        .values
        .map { rows ->
            val first = rows.first()
            first.copy(usedQty = rows.sumOf { it.usedQty })
        }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
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
    cashReconciliation: CashReconciliation? = null,
    isLoadingCashReconciliation: Boolean = false,
    cashReconciliationError: String? = null,
    onBack: () -> Unit,
    onPreview: (List<ClosingRecipeAnchorInput>) -> Unit = {},
    onClearPreview: () -> Unit = {},
    onSubmit: (remaining: Map<Long, Double>, anchors: List<ClosingRecipeAnchorInput>, notes: String?, actualCash: Long) -> Unit
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
    val lastValidRemainingInputs = remember(items) {
        mutableStateMapOf<Long, Double>().apply {
            items.forEach { item ->
                put(item.ingredientId, item.remainingQty ?: item.qty)
            }
        }
    }
    val notesInput = remember { mutableStateOf("") }
    var actualCashInput by rememberSaveable { mutableStateOf("") }
    var selectedDetailAnchorId by remember { mutableStateOf<Long?>(null) }
    var bottomActionHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val bottomContentPadding = with(density) {
        bottomActionHeightPx.toDp() + 16.dp
    }

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
    val recipeAllocations = remember(effectiveClosingGroups) {
        mutableStateMapOf<Long, List<RecipeAllocation>>()
    }
    var pendingRecipeAllocation by remember {
        mutableStateOf<PendingRecipeAllocation?>(null)
    }
    var variantPickerGroup by remember { mutableStateOf<ClosingRecipeGroup?>(null) }
    var step by remember { mutableIntStateOf(1) } // 1: input, 2: review
    val dailyStockValidator = remember { DailyStockValidator() }

    val hasInvalidInput = items.any { item ->
        val value = remainingInputs[item.ingredientId]?.toDoubleOrNull()
        dailyStockValidator.validateRemainingQuantity(value) is ValidationResult.Invalid
    }
    val editedClosingGroups = effectiveClosingGroups.filter {
        recipeAllocations[it.anchorIngredientId].orEmpty().isNotEmpty()
    }
    val anchors = editedClosingGroups.flatMap { group ->
        val actualRemaining = anchorInputs[group.anchorIngredientId]
            ?.toDoubleOrNull()
            ?: return@flatMap emptyList()
        recipeAllocations[group.anchorIngredientId]
            .orEmpty()
            .groupBy { it.menuVariantId }
            .map { (menuVariantId, rows) ->
                ClosingRecipeAnchorInput(
                    menuVariantId = menuVariantId,
                    actualRemaining = actualRemaining,
                    allocatedQuantity = rows.sumOf { it.quantity }
                )
            }
    }
    val allocationsMatchPhysicalInput = editedClosingGroups.all { group ->
        val actualRemaining = anchorInputs[group.anchorIngredientId]?.toDoubleOrNull()
            ?: return@all false
        val expectedUsage = (group.systemRemaining - actualRemaining).coerceAtLeast(0.0)
        val allocatedUsage = recipeAllocations[group.anchorIngredientId]
            .orEmpty()
            .sumOf { it.quantity }
        (expectedUsage - allocatedUsage).absoluteValue <= 0.000001
    }
    val recipeAnchorEdited = anchors.isNotEmpty()
    val recipeModeReady = pendingRecipeAllocation == null &&
        (!recipeAnchorEdited ||
            (closingPreview != null && allocationsMatchPhysicalInput &&
                editedClosingGroups.all { it.ready }))
    val actualCash = actualCashInput.toLongOrNull()
    val cashDifference = if (actualCash != null && cashReconciliation != null) {
        actualCash - cashReconciliation.expectedCash
    } else {
        null
    }
    val cashInputReady = cashReconciliation != null &&
        actualCash != null &&
        actualCash >= 0L &&
        (cashDifference == 0L || notesInput.value.isNotBlank())

    LaunchedEffect(anchors, pendingRecipeAllocation) {
        if (anchors.isNotEmpty() && pendingRecipeAllocation == null &&
            allocationsMatchPhysicalInput && editedClosingGroups.all { it.ready }
        ) {
            delay(450)
            onPreview(anchors)
        }
    }

    LaunchedEffect(closingPreview) {
        closingPreview?.remainingItems?.forEach { result ->
            remainingInputs[result.ingredientId] = formatInitialQty(result.remainingQty)
            lastValidRemainingInputs[result.ingredientId] = result.remainingQty
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
                contentPadding = PaddingValues(bottom = bottomContentPadding),
                verticalArrangement = if (step == 1) Arrangement.spacedBy(8.dp) else Arrangement.Top,
                horizontalAlignment = if (step == 2) Alignment.CenterHorizontally else Alignment.Start
            ) {
                // Shared Header (Stepper + Error)
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    StepperSection(step = step)
                        Spacer(modifier = Modifier.height(16.dp))

                    val displayedError = closeErrorMessage
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
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = KebabPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(9.dp))
                            Column {
                                Text(
                                    text = "Pengurangan otomatis aktif",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KebabTextDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Saat sisa bahan utama berubah, bahan resep yang terkait akan dihitung otomatis.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = KebabTextGray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(items.size, key = { index -> items[index].ingredientId }) { index ->
                        val item = items[index]
                        val closingGroup = effectiveClosingGroups.firstOrNull {
                            it.anchorIngredientId == item.ingredientId
                        }
                        val allocatedVariantIds = recipeAllocations[item.ingredientId]
                            .orEmpty()
                            .map { it.menuVariantId }
                            .toSet()
                        val affectedIngredients = aggregateAffectedIngredients(
                            preview = closingPreview,
                            anchorIngredientId = item.ingredientId,
                            allocatedVariantIds = allocatedVariantIds
                        )
                        val recipeAllocationActive = allocatedVariantIds.isNotEmpty()

                        
                        val parsedRemaining = remainingInputs[item.ingredientId]?.toDoubleOrNull()
                        val sisa = parsedRemaining ?: 0.0
                        val terpakaiVal = (item.qty - sisa).coerceAtLeast(0.0)
                        val isAutomaticRecipeChanged = parsedRemaining != null &&
                            closingGroup != null &&
                            recipeAllocationActive &&
                            sisa < closingGroup.systemRemaining - 0.000001
                        
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
                                    val ingredientId = item.ingredientId
                                    val previousValue = lastValidRemainingInputs[ingredientId]
                                    val nextValue = newValue.toDoubleOrNull()
                                    remainingInputs[ingredientId] = newValue

                                    if (closingGroup == null) {
                                        if (nextValue != null) {
                                            lastValidRemainingInputs[ingredientId] = nextValue
                                        }
                                    } else {
                                        anchorInputs[ingredientId] = newValue
                                        if (previousValue != null && nextValue != null) {
                                            lastValidRemainingInputs[ingredientId] = nextValue
                                            val currentAllocatedUsage = recipeAllocations[ingredientId]
                                                .orEmpty()
                                                .sumOf { it.quantity }
                                            val targetAllocatedUsage =
                                                (closingGroup.systemRemaining - nextValue).coerceAtLeast(0.0)
                                            when {
                                                nextValue < previousValue - 0.000001 &&
                                                    targetAllocatedUsage > currentAllocatedUsage + 0.000001 -> {
                                                    val quantity =
                                                        targetAllocatedUsage - currentAllocatedUsage
                                                    val onlyVariant = closingGroup.variants.singleOrNull()
                                                    if (onlyVariant != null) {
                                                        recipeAllocations[ingredientId] =
                                                            recipeAllocations[ingredientId].orEmpty() +
                                                                RecipeAllocation(onlyVariant.menuVariantId, quantity)
                                                        onClearPreview()
                                                    } else {
                                                        pendingRecipeAllocation = PendingRecipeAllocation(
                                                            group = closingGroup,
                                                            previousRemaining = previousValue,
                                                            newRemaining = nextValue,
                                                            quantity = quantity
                                                        )
                                                        variantPickerGroup = closingGroup
                                                    }
                                                }

                                                nextValue > previousValue + 0.000001 -> {
                                                    val updatedAllocations = rollbackRecipeAllocations(
                                                        entries = recipeAllocations[ingredientId].orEmpty(),
                                                        restoredQuantity =
                                                            (currentAllocatedUsage - targetAllocatedUsage)
                                                                .coerceAtLeast(0.0)
                                                    )
                                                    if (updatedAllocations.isEmpty()) {
                                                        recipeAllocations.remove(ingredientId)
                                                    } else {
                                                        recipeAllocations[ingredientId] = updatedAllocations
                                                    }
                                                    pendingRecipeAllocation = null
                                                    variantPickerGroup = null
                                                    onClearPreview()
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            satuan = (item.unit ?: "unit").uppercase(Locale.ROOT),
                            terpakai = terpakaiFormat,
                            hasUsage = terpakaiVal > 0.000001,
                            hasAutomaticRecipe = closingGroup != null,
                            isAutomaticRecipeChanged = isAutomaticRecipeChanged,
                            affectedIngredientCount = affectedIngredients.size,
                            isRecipePreviewLoading = isPreviewingClosing && recipeAllocationActive,
                            recipePreviewError = closingPreviewError.takeIf {
                                recipeAllocationActive && !isPreviewingClosing
                            },
                            onRecipeDetailClick = if (isAutomaticRecipeChanged && affectedIngredients.isNotEmpty()) {
                                { selectedDetailAnchorId = item.ingredientId }
                            } else null,
                            onRecipeRetry = if (closingPreviewError != null && recipeAllocationActive) {
                                { onPreview(anchors) }
                            } else null,
                            recipeVariantLabel = null,
                            onRecipeVariantClick = null
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
                        val usedIngredients = items.count { item ->
                            val remaining = remainingInputs[item.ingredientId]?.toDoubleOrNull() ?: 0.0
                            item.qty - remaining > 0.000001
                        }
                        RingkasanSesiCard(
                            totalIngredients = items.size,
                            usedIngredients = usedIngredients
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailPenggunaanSection(items = items, remainingInputs = remainingInputs)
                        Spacer(modifier = Modifier.height(16.dp))

                        CashReconciliationSection(
                            reconciliation = cashReconciliation,
                            actualCashInput = actualCashInput,
                            difference = cashDifference,
                            isLoading = isLoadingCashReconciliation,
                            errorMessage = cashReconciliationError,
                            enabled = !isClosing,
                            onActualCashChanged = { value ->
                                actualCashInput = value.filter(Char::isDigit).take(12)
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 2.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = KebabPrimary,
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Catatan sesi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
                                    Text(
                                        if (cashDifference != null && cashDifference != 0L) {
                                            "Wajib diisi karena terdapat selisih kas"
                                        } else {
                                            "Opsional, untuk informasi pergantian shift"
                                        },
                                        fontSize = 11.sp,
                                        color = if (cashDifference != null && cashDifference != 0L) KebabErrorText else KebabTextGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, KebabDivider)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(96.dp)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(KebabInputBg)
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    if (notesInput.value.isEmpty()) {
                                        Text(
                                            text = "Contoh: stok fisik sudah dicek ulang...",
                                            color = KebabTextGray.copy(alpha = 0.65f),
                                            fontSize = 12.sp
                                        )
                                    }
                                    BasicTextField(
                                        value = notesInput.value,
                                        onValueChange = { notesInput.value = it },
                                        modifier = Modifier.fillMaxSize(),
                                        textStyle = TextStyle(fontSize = 13.sp, color = KebabTextDark),
                                        enabled = !isClosing
                                    )
                                }
                            }
                        }
                    }
                }
                
            }
        }

        // --- FLOATING ACTION BUTTONS ---
        if (step == 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .onSizeChanged { bottomActionHeightPx = it.height }
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (!isClosing && items.isNotEmpty() && !hasInvalidInput && recipeModeReady)
                                    KebabPrimary
                                else
                                    Color.Gray.copy(alpha = 0.5f)
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
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .onSizeChanged { bottomActionHeightPx = it.height }
                        .imePadding()
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        .background(Color.White)
                        .border(
                            1.dp,
                            KebabDivider,
                            RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                        )
                        .padding(horizontal = 16.dp)
                        .padding(top = 14.dp, bottom = 104.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                    OutlinedButton(
                        onClick = { step = 1 },
                        modifier = Modifier
                            .weight(0.85f)
                            .height(52.dp),
                        enabled = !isClosing,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KebabDivider),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KebabTextDark)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ubah", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.4f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                KebabPrimary.copy(
                                    alpha = if (!isClosing && !hasInvalidInput && recipeModeReady && cashInputReady) 1f else 0.5f
                                )
                            )
                            .clickable(enabled = !isClosing && !hasInvalidInput && recipeModeReady && cashInputReady) {
                                val remaining = mutableMapOf<Long, Double>()
                                items.forEach { item ->
                                    val value = remainingInputs[item.ingredientId]?.toDoubleOrNull()
                                    if (dailyStockValidator.validateRemainingQuantity(value) is ValidationResult.Invalid) {
                                        return@clickable
                                    }
                                    remaining[item.ingredientId] = value ?: return@clickable
                                }
                                val notes = notesInput.value.trim().takeIf { it.isNotBlank() }
                                onSubmit(
                                    remaining,
                                    if (recipeAnchorEdited) anchors else emptyList(),
                                    notes,
                                    actualCash ?: return@clickable
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isClosing) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Memproses", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Text(text = "Tutup Sesi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        selectedDetailAnchorId?.let { anchorId ->
            val anchorItem = items.firstOrNull { it.ingredientId == anchorId }
            val affectedIngredients = aggregateAffectedIngredients(
                preview = closingPreview,
                anchorIngredientId = anchorId,
                allocatedVariantIds = recipeAllocations[anchorId]
                    .orEmpty()
                    .map { it.menuVariantId }
                    .toSet()
            )
            if (anchorItem != null && affectedIngredients.isNotEmpty()) {
                val physicalRemaining = remainingInputs[anchorId]?.toDoubleOrNull() ?: anchorItem.qty
                AutomaticDeductionBottomSheet(
                    anchorName = anchorItem.name,
                    usedQuantity = (anchorItem.qty - physicalRemaining).coerceAtLeast(0.0),
                    anchorUnit = (anchorItem.unit ?: "unit").uppercase(Locale.ROOT),
                    affectedIngredients = affectedIngredients,
                    onDismiss = { selectedDetailAnchorId = null }
                )
            }
        }

        variantPickerGroup?.let { group ->
            RecipeVariantPickerDialog(
                group = group,
                selectedVariantId = null,
                onDismiss = {
                    pendingRecipeAllocation?.takeIf {
                        it.group.anchorIngredientId == group.anchorIngredientId
                    }?.let { pending ->
                        val restored = formatInitialQty(pending.previousRemaining)
                        remainingInputs[group.anchorIngredientId] = restored
                        lastValidRemainingInputs[group.anchorIngredientId] = pending.previousRemaining
                        anchorInputs[group.anchorIngredientId] = restored
                    }
                    pendingRecipeAllocation = null
                    variantPickerGroup = null
                },
                onSelect = { variant ->
                    pendingRecipeAllocation?.takeIf {
                        it.group.anchorIngredientId == group.anchorIngredientId
                    }?.let { pending ->
                        val ingredientId = group.anchorIngredientId
                        recipeAllocations[ingredientId] =
                            recipeAllocations[ingredientId].orEmpty() +
                                RecipeAllocation(variant.menuVariantId, pending.quantity)
                        val remaining = formatInitialQty(pending.newRemaining)
                        remainingInputs[ingredientId] = remaining
                        lastValidRemainingInputs[ingredientId] = pending.newRemaining
                        anchorInputs[ingredientId] = remaining
                        onClearPreview()
                    }
                    pendingRecipeAllocation = null
                    variantPickerGroup = null
                }
            )
        }
    }
}

@Composable
private fun CashReconciliationSection(
    reconciliation: CashReconciliation?,
    actualCashInput: String,
    difference: Long?,
    isLoading: Boolean,
    errorMessage: String?,
    enabled: Boolean,
    onActualCashChanged: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Rekonsiliasi Kas",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = KebabTextDark
        )
        Text(
            text = "Hitung seluruh uang tunai yang benar-benar ada di laci kas.",
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = KebabTextGray
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, KebabDivider)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when {
                    isLoading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = KebabPrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Menghitung kas dari server...", fontSize = 12.sp, color = KebabTextGray)
                        }
                    }

                    reconciliation == null -> {
                        Text(
                            text = errorMessage ?: "Rekonsiliasi kas belum tersedia.",
                            fontSize = 12.sp,
                            color = KebabErrorText
                        )
                    }

                    else -> {
                        CashSummaryRow("Kas awal", formatRupiah(reconciliation.openingCash))
                        CashSummaryRow("Penjualan tunai", formatRupiah(reconciliation.cashSales))
                        CashSummaryRow("Pengeluaran dari kas", formatRupiah(reconciliation.cashExpenses))
                        HorizontalDivider(color = KebabDivider)
                        CashSummaryRow(
                            "Kas seharusnya",
                            formatRupiah(reconciliation.expectedCash),
                            emphasized = true
                        )

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Uang fisik aktual",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KebabTextDark
                        )
                        OutlinedTextField(
                            value = actualCashInput,
                            onValueChange = onActualCashChanged,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = enabled,
                            singleLine = true,
                            prefix = {
                                Text("Rp", fontWeight = FontWeight.Bold, color = KebabPrimary)
                            },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = KebabInputBg,
                                unfocusedContainerColor = KebabInputBg,
                                disabledContainerColor = KebabInputBg,
                                focusedIndicatorColor = KebabPrimary,
                                unfocusedIndicatorColor = KebabDivider
                            )
                        )

                        HorizontalDivider(color = KebabDivider)
                        val differenceColor = when {
                            difference == null -> KebabTextGray
                            difference == 0L -> Color(0xFF16794B)
                            else -> KebabErrorText
                        }
                        CashSummaryRow(
                            "Selisih kas",
                            difference?.let(::formatSignedRupiah) ?: "-",
                            emphasized = true,
                            valueColor = differenceColor
                        )
                        CashSummaryRow(
                            "Status",
                            when {
                                difference == null -> "-"
                                difference < 0L -> "KURANG"
                                difference > 0L -> "LEBIH"
                                else -> "SESUAI"
                            },
                            emphasized = true,
                            valueColor = differenceColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CashSummaryRow(
    label: String,
    value: String,
    emphasized: Boolean = false,
    valueColor: Color = KebabTextDark
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasized) KebabTextDark else KebabTextGray
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

private fun formatRupiah(value: Long): String =
    "Rp " + NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(value)

private fun formatSignedRupiah(value: Long): String = when {
    value > 0L -> "+" + formatRupiah(value)
    value < 0L -> "-" + formatRupiah(-value)
    else -> formatRupiah(0)
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
                    text = "Pilih menu yang berkurang",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KebabTextDark
                )
                Text(
                    text = "${group.anchorName} dipakai di beberapa menu. Pilih menu untuk menghitung perubahan ini.",
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutomaticDeductionBottomSheet(
    anchorName: String,
    usedQuantity: Double,
    anchorUnit: String,
    affectedIngredients: List<ClosingRecipeAffectedIngredient>,
    onDismiss: () -> Unit
) {
    val displayedAnchorUsage = convertUsedQuantityForReview(usedQuantity, anchorUnit)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Pengurangan otomatis",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = KebabTextDark
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(anchorName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KebabTextDark)
            Text(
                text = "Terpakai: ${formatDisplayQty(displayedAnchorUsage.first)} ${displayedAnchorUsage.second.uppercase(Locale.ROOT)}",
                fontSize = 13.sp,
                color = KebabTextGray
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = KebabDivider)
            Text(
                text = "Bahan yang ikut berubah",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = KebabTextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(affectedIngredients, key = { it.ingredientId }) { ingredient ->
                    val displayedUsage = convertUsedQuantityForReview(
                        ingredient.usedQty,
                        ingredient.unit
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ingredient.name,
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            color = KebabTextDark
                        )
                        Text(
                            text = "-${formatDisplayQty(displayedUsage.first)} ${displayedUsage.second.uppercase(Locale.ROOT)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = KebabPrimary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(KebabPrimary.copy(alpha = 0.06f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dihitung otomatis berdasarkan resep.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KebabTextDark
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Perubahan ini belum disimpan.",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = KebabError
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KebabPrimary)
            ) {
                Text("Tutup", fontWeight = FontWeight.Bold)
            }
        }
    }
}
