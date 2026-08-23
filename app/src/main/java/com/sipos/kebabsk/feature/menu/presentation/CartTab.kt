package com.sipos.kebabsk.feature.menu.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.R
import com.sipos.kebabsk.common.MoneyUtils
import com.sipos.kebabsk.feature.cart.domain.model.CartItem
import com.sipos.kebabsk.ui.theme.*

@Composable
fun CartTab(
    cartItems: List<CartItem>,
    totalAmount: Long,
    onAddVariant: (menuName: String, variantId: Long, variantName: String, price: Long) -> Unit,
    onRemoveVariant: (Long) -> Unit,
    onDeleteVariant: (Long) -> Unit,
    cartInteractionEnabled: Boolean = true,
    onNavigateToPayment: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KebabBg)
    ) {
        if (cartItems.isEmpty()) {
            // Empty State
            Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.cart_title),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KebabTextDark,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.cart_subtitle),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = KebabTextGray
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            tint = KebabTextGray.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.cart_empty_title),
                            color = KebabTextGray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.cart_empty_message),
                            color = KebabTextGray.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val formatPrice = { amount: Long ->
                MoneyUtils.formatRupiah(amount).replace("Rp", "Rp ").replace(",00", "")
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Scrollable Items
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pluralStringResource(
                                    R.plurals.cart_order_count,
                                    cartItems.sumOf { it.qty },
                                    cartItems.sumOf { it.qty }
                                ),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KebabTextGray
                            )
                        }
                    }

                    items(count = cartItems.size, key = { cartItems[it].variantId }) { index ->
                        val item = cartItems[index]

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            border = BorderStroke(1.dp, Color(0xFFF0E4DB))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 14.dp, top = 10.dp, end = 8.dp, bottom = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = buildMenuVariantTitle(item.menuName, item.variantName),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KebabTextDark,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onDeleteVariant(item.variantId) },
                                        enabled = cartInteractionEnabled,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = stringResource(R.string.cd_delete_cart_item),
                                            tint = KebabTextGray.copy(alpha = 0.55f),
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = formatPrice(item.price * item.qty),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = KebabPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${formatPrice(item.price)} / item",
                                            fontSize = 11.sp,
                                            color = KebabTextGray.copy(alpha = 0.78f),
                                            maxLines = 1
                                        )
                                    }
                                    CartQuantitySelector(
                                        qty = item.qty,
                                        enabled = cartInteractionEnabled,
                                        onRemove = { onRemoveVariant(item.variantId) },
                                        onAdd = {
                                            onAddVariant(item.menuName, item.variantId, item.variantName, item.price)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Modern Persistent Bottom Checkout Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 100.dp),
                    color = Color.White,
                    shadowElevation = 5.dp,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Total Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.cart_total_payment),
                                fontSize = 13.sp,
                                color = KebabTextGray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatPrice(totalAmount),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KebabTextDark
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onBackToMenu,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(11.dp),
                                border = BorderStroke(1.dp, KebabPrimary),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.action_add_more),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KebabPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Button(
                                onClick = onNavigateToPayment,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(11.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KebabPrimary),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.action_payment),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartQuantitySelector(
    qty: Int,
    enabled: Boolean,
    onRemove: () -> Unit,
    onAdd: () -> Unit
) {
    val quantityState = stringResource(R.string.quantity_state, qty)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KebabInputBg)
            .semantics { stateDescription = quantityState }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        CartQuantityButton(
            onClick = onRemove,
            enabled = enabled,
            containerColor = Color.White,
            contentColor = KebabPrimary,
            icon = Icons.Outlined.Remove,
            contentDescription = stringResource(R.string.cd_reduce_quantity)
        )

        Text(
            text = "$qty",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = KebabTextDark,
            modifier = Modifier.widthIn(min = 24.dp),
            textAlign = TextAlign.Center
        )

        CartQuantityButton(
            onClick = onAdd,
            enabled = enabled,
            containerColor = KebabPrimary,
            contentColor = Color.White,
            icon = Icons.Outlined.Add,
            contentDescription = stringResource(R.string.cd_add_quantity)
        )
    }
}

@Composable
private fun CartQuantityButton(
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(containerColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
    }
}
