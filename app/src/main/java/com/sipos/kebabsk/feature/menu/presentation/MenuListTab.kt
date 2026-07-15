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
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.R
import com.sipos.kebabsk.common.MoneyUtils
import com.sipos.kebabsk.common.VariantDisplayUtils
import com.sipos.kebabsk.feature.cart.domain.model.CartItem

import com.sipos.kebabsk.ui.theme.*

@Composable
fun MenuListTab(
    menuItems: List<MenuVariantItem>,
    categories: List<String?>,
    selectedCategory: String?,
    cartItems: List<CartItem>,
    cartInteractionEnabled: Boolean = true,
    emptyStateMessage: String? = null,
    onCategorySelected: (String?) -> Unit,
    onAddVariant: (String, Long, String, Long) -> Unit,
    onRemoveVariant: (Long) -> Unit
) {
    val resolvedEmptyStateMessage = emptyStateMessage ?: stringResource(R.string.menu_empty_message)

    val cartQtyMap = remember(cartItems) {
        cartItems.associate { it.variantId to it.qty }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 100.dp, start = 24.dp, end = 24.dp, top = 16.dp)
    ) {
        // --- HEADER ---
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.menu_select_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = KebabTextDark
                )
            }
        }

        // --- CATEGORY CHIPS ---
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
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
        }

        if (menuItems.isEmpty()) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = KebabTextGray.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = resolvedEmptyStateMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = KebabTextGray,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // --- MENU GRID ---
            items(items = menuItems, key = { it.variantId }) { item ->
                MenuItemCard(
                    item = item,
                    qtyInCart = cartQtyMap[item.variantId] ?: 0,
                    cartInteractionEnabled = cartInteractionEnabled,
                    onAdd = { onAddVariant(item.menuName, item.variantId, item.variantName, item.price) },
                    onRemove = { onRemoveVariant(item.variantId) }
                )
            }
        }
    }
}

@Composable
fun CategoryChip(title: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) KebabPrimaryContainer else KebabChipInactiveBg
    val textColor = if (isSelected) Color.White else KebabTextGray
    val selectedState = stringResource(R.string.menu_category_selected)
    val notSelectedState = stringResource(R.string.menu_category_not_selected)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .minimumInteractiveComponentSize()
            .semantics { stateDescription = if (isSelected) selectedState else notSelectedState }
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
    cartInteractionEnabled: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    // insufficientStock = bahan kurang (tampil, tapi tidak bisa dipesan)
    // !isAvailable && !insufficientStock = dinonaktifkan admin (tersaring, tapi handle gracefully)
    val isInsufficientStock = item.insufficientStock && !item.isAvailable
    val isAdminDisabled = !item.isAvailable && !item.insufficientStock
    val isOrderable = item.isAvailable && !item.insufficientStock
    val canInteract = isOrderable && cartInteractionEnabled

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canInteract, onClick = onAdd),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInsufficientStock) KebabCardBg else KebabCardBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                maxLines = 2,
                lineHeight = 18.sp,
                overflow = TextOverflow.Ellipsis
            )

            // Variant name
            val displayVariantName = VariantDisplayUtils.formatVariantName(item.menuName, item.variantName)

            if (displayVariantName.isNotBlank() && displayVariantName != item.menuName) {
                Text(
                    text = displayVariantName,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isInsufficientStock) KebabTextGray.copy(alpha = 0.7f) else KebabTextGray
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom area: price first, quantity selector anchored at the lower-right.
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = MoneyUtils.formatRupiah(item.price),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isInsufficientStock) KebabPrimary.copy(alpha = 0.5f) else KebabPrimary,
                    textAlign = TextAlign.Start
                )

                if (qtyInCart > 0 && isOrderable) {
                    QuantitySelector(
                        qty = qtyInCart,
                        enabled = cartInteractionEnabled,
                        onAdd = onAdd,
                        onRemove = onRemove
                    )
                } else if (isOrderable) {
                    QuantityAddButton(enabled = cartInteractionEnabled, onAdd = onAdd)
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
                            text = stringResource(R.string.menu_stock_low),
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
                            text = stringResource(R.string.menu_inactive),
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
private fun QuantitySelector(qty: Int, enabled: Boolean, onAdd: () -> Unit, onRemove: () -> Unit) {
    val quantityState = stringResource(R.string.quantity_state, qty)
    val reduceDescription = stringResource(R.string.cd_reduce_quantity)
    val addDescription = stringResource(R.string.cd_add_quantity)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .semantics { stateDescription = quantityState },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3E8DD))
                .semantics { contentDescription = reduceDescription }
                .clickable(enabled = enabled, onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Remove, contentDescription = null, tint = KebabPrimary, modifier = Modifier.size(20.dp))
        }

        Text(
            text = "$qty",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = KebabTextDark,
            maxLines = 1
        )

        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(40.dp)
                .clip(CircleShape)
                .background(KebabPrimaryContainer)
                .semantics { contentDescription = addDescription }
                .clickable(enabled = enabled, onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun QuantityAddButton(enabled: Boolean, onAdd: () -> Unit) {
    val addToCartDescription = stringResource(R.string.cd_add_to_cart)

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(40.dp)
                .clip(CircleShape)
                .background(KebabPrimaryContainer)
                .semantics { contentDescription = addToCartDescription }
                .clickable(enabled = enabled, onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}
