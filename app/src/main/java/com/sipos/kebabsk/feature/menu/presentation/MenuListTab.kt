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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.sipos.kebabsk.feature.cart.domain.model.CartItem
import com.sipos.kebabsk.feature.menu.domain.model.MenuCategory
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

import com.sipos.kebabsk.ui.theme.*

@Composable
fun MenuListTab(
    menuItems: List<MenuVariantItem>,
    categories: List<MenuCategory>,
    selectedCategoryId: Long?,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    loadMoreErrorMessage: String?,
    cartItems: List<CartItem>,
    cartInteractionEnabled: Boolean = true,
    emptyStateMessage: String? = null,
    onCategorySelected: (Long?) -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onAddVariant: (String, Long, String, Long) -> Unit,
    onRemoveVariant: (Long) -> Unit
) {
    val resolvedEmptyStateMessage = emptyStateMessage ?: stringResource(R.string.menu_empty_message)
    val gridState = rememberLazyGridState()

    val cartQtyMap = remember(cartItems) {
        cartItems.associate { it.variantId to it.qty }
    }

    LaunchedEffect(gridState, menuItems.size, hasMore, isLoadingMore) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisibleIndex to layoutInfo.totalItemsCount
        }
            .distinctUntilChanged()
            .collect { (lastVisibleIndex, totalItems) ->
                if (
                    totalItems > 0 &&
                    lastVisibleIndex >= totalItems - LOAD_MORE_THRESHOLD &&
                    hasMore &&
                    !isLoadingMore
                ) {
                    onLoadMore()
                }
            }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 100.dp, start = 12.dp, end = 12.dp, top = 6.dp)
    ) {
        // --- CATEGORY CHIPS ---
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip(
                        title = stringResource(R.string.menu_category_all),
                        isSelected = selectedCategoryId == null,
                        onClick = { onCategorySelected(null) }
                    )
                }
                items(categories, key = { it.id }) { category ->
                    CategoryChip(
                        title = category.name,
                        isSelected = category.id == selectedCategoryId,
                        onClick = { onCategorySelected(category.id) }
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

        if (isLoadingMore) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
        } else if (!loadMoreErrorMessage.isNullOrBlank()) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = loadMoreErrorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onRetryLoadMore,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }
        }
    }
}

private const val LOAD_MORE_THRESHOLD = 6

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
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
            // Gambar varian dari API. Ikon tetap terlihat saat URL kosong, memuat, atau gagal.
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

                item.imageUrl?.let { imageUrl ->
                    ReliableMenuImage(
                        imageUrl = imageUrl,
                        contentDescription = "${item.menuName} ${item.variantName}",
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(if (isInsufficientStock) 0.65f else 1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nama menu dan varian ditampilkan sebagai satu judul.
            Text(
                text = buildMenuVariantTitle(item.menuName, item.variantName),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isInsufficientStock) KebabTextDark.copy(alpha = 0.7f) else KebabTextDark,
                maxLines = 2,
                lineHeight = 18.sp,
                overflow = TextOverflow.Ellipsis
            )

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
private fun ReliableMenuImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var retryAttempt by remember(imageUrl) { mutableIntStateOf(0) }
    var retryRequested by remember(imageUrl) { mutableStateOf(false) }

    LaunchedEffect(imageUrl, retryRequested, retryAttempt) {
        if (retryRequested && retryAttempt < MAX_IMAGE_RETRIES) {
            delay(500L * (retryAttempt + 1))
            retryRequested = false
            retryAttempt++
        }
    }

    val requestUrl = remember(imageUrl, retryAttempt) {
        if (retryAttempt == 0) {
            imageUrl
        } else {
            val separator = if ('?' in imageUrl) '&' else '?'
            "$imageUrl${separator}image_retry=$retryAttempt"
        }
    }
    val request = remember(requestUrl, imageUrl) {
        ImageRequest.Builder(context)
            .data(requestUrl)
            .memoryCacheKey(imageUrl)
            .diskCacheKey(imageUrl)
            .crossfade(false)
            .build()
    }

    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier,
        onSuccess = { retryRequested = false },
        onError = {
            if (retryAttempt < MAX_IMAGE_RETRIES) {
                retryRequested = true
            }
        }
    )
}

private const val MAX_IMAGE_RETRIES = 2

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
                .background(KebabInputBg)
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
