package com.sipos.kebabsk.feature.menu.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutCartItem

import com.sipos.kebabsk.ui.theme.*

@Composable
fun MenuListTab(
    menuItems: List<MenuVariantItem>,
    categories: List<String?>,
    selectedCategory: String?,
    cartItems: List<CheckoutCartItem>,
    emptyStateMessage: String = "Tidak ada menu tersedia",
    onCategorySelected: (String?) -> Unit,
    onRefresh: () -> Unit,
    onAddVariant: (String, Long, String, Double) -> Unit,
    onRemoveVariant: (Long) -> Unit
) {
    val cartQtyMap = remember(cartItems) {
        cartItems.associate { it.variantId to it.qty }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pilih Menu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = KebabTextDark
            )
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = KebabPrimaryContainer
                )
            }
        }

        // --- CATEGORY CHIPS ---
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { category ->
                CategoryChip(
                    title = category ?: "Semua",
                    isSelected = category == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (menuItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = KebabTextGray.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = emptyStateMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = KebabTextGray,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // --- MENU GRID ---
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp, start = 24.dp, end = 24.dp)
            ) {
                items(items = menuItems, key = { it.variantId }) { item ->
                    MenuItemCard(
                        item = item,
                        qtyInCart = cartQtyMap[item.variantId] ?: 0,
                        onAdd = { onAddVariant(item.menuName, item.variantId, item.variantName, item.price) },
                        onRemove = { onRemoveVariant(item.variantId) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(title: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) KebabPrimaryContainer else KebabChipInactiveBg
    val textColor = if (isSelected) Color.White else KebabTextGray

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun MenuItemCard(
    item: MenuVariantItem,
    qtyInCart: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    // insufficientStock = bahan kurang (tampil, tapi tidak bisa dipesan)
    // !isAvailable && !insufficientStock = dinonaktifkan admin (tersaring, tapi handle gracefully)
    val isInsufficientStock = item.insufficientStock && !item.isAvailable
    val isAdminDisabled = !item.isAvailable && !item.insufficientStock
    val isOrderable = item.isAvailable && !item.insufficientStock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isOrderable, onClick = onAdd),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInsufficientStock) KebabCardBg else KebabCardBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Gambar Menu (Rasio 1:1) placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isInsufficientStock)
                            Color(0xFFFFF3CD) // warna kuning redup untuk stok kurang
                        else
                            Color.LightGray.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = if (isInsufficientStock) Color(0xFFF59E0B) else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Menu Name
            Text(
                text = item.menuName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isInsufficientStock) KebabTextDark.copy(alpha = 0.7f) else KebabTextDark,
                maxLines = 1,
                lineHeight = 18.sp,
                overflow = TextOverflow.Ellipsis
            )

            // Variant name
            val displayVariantName = if (item.variantName.startsWith(item.menuName, ignoreCase = true)) {
                item.variantName.substring(item.menuName.length).trim().ifBlank { item.variantName }
            } else {
                item.variantName
            }

            if (displayVariantName.isNotBlank() && displayVariantName != item.menuName) {
                Text(
                    text = displayVariantName,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isInsufficientStock) KebabTextGray.copy(alpha = 0.7f) else KebabTextGray
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom row: Price and Add controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = toRupiah(item.price),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isInsufficientStock) KebabPrimary.copy(alpha = 0.5f) else KebabPrimary
                )

                if (qtyInCart > 0 && isOrderable) {
                    QtyControlCompact(qty = qtyInCart, onAdd = onAdd, onRemove = onRemove)
                } else if (isOrderable) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(KebabPrimaryContainer)
                            .clickable(onClick = onAdd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Badge status
            Spacer(modifier = Modifier.height(4.dp))
            when {
                isInsufficientStock -> {
                    // Badge stok bahan kurang - warna kuning/warning
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFF3CD)
                    ) {
                        Text(
                            text = "⚠ Stok Bahan Kurang",
                            color = Color(0xFF92400E),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                isAdminDisabled -> {
                    // Seharusnya tidak tampil ke kasir (sudah difilter)
                    // Tapi jika admin melihat, tampilkan badge "Nonaktif"
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = "Nonaktif",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QtyControlCompact(qty: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Kurang",
                tint = KebabTextDark,
                modifier = Modifier.size(14.dp)
            )
        }

        Text(
            text = "$qty",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = KebabTextDark,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 22.dp)
        )

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(KebabPrimaryContainer)
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

