package com.sipos.kebabsk.feature.menu.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sipos.kebabsk.feature.checkout.domain.model.CheckoutCartItem
import com.sipos.kebabsk.ui.theme.*
import com.sipos.kebabsk.feature.menu.presentation.toRupiah

@Composable
fun CartTab(
    cartItems: List<CheckoutCartItem>,
    totalAmount: Double,
    onAddVariant: (menuName: String, variantId: Long, variantName: String, price: Double) -> Unit,
    onRemoveVariant: (Long) -> Unit,
    onDeleteVariant: (Long) -> Unit,
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
                modifier = Modifier.padding(24.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Keranjang Belanja",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KebabTextDark,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Daftar pesanan Anda saat ini",
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
                            text = "Keranjang masih kosong",
                            color = KebabTextGray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Silakan pilih menu terlebih dahulu\nuntuk mulai memesan",
                            color = KebabTextGray.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val formatPrice = { amount: Double ->
                toRupiah(amount).replace("Rp", "Rp ").replace(",00", "")
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // Scrollable Items
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Keranjang Belanja",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KebabTextDark
                                )
                                Text(
                                    text = "${cartItems.sumOf { it.qty }} item pesanan",
                                    fontSize = 14.sp,
                                    color = KebabTextGray
                                )
                            }
                        }
                    }

                    items(count = cartItems.size, key = { cartItems[it].variantId }) { index ->
                        val item = cartItems[index]
                        val displayVariantName = if (item.variantName.startsWith(item.menuName, ignoreCase = true)) {
                            item.variantName.substring(item.menuName.length).trim()
                        } else {
                            item.variantName
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Side: Info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.menuName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KebabTextDark,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (displayVariantName.isNotBlank() && displayVariantName != item.menuName) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = displayVariantName,
                                            fontSize = 13.sp,
                                            color = KebabTextGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Right Side: Controls
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatPrice(item.price * item.qty),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = KebabPrimary,
                                        textAlign = TextAlign.End,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    IconButton(
                                        onClick = { onDeleteVariant(item.variantId) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Hapus",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    CartQuantitySelector(
                                        qty = item.qty,
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
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 108.dp),
                    color = Color.White,
                    shadowElevation = 10.dp,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Total Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Pembayaran",
                                fontSize = 13.sp,
                                color = KebabTextGray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatPrice(totalAmount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KebabTextDark
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onBackToMenu,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, KebabPrimary),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "+ Tambah",
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
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KebabPrimary),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "Pembayaran",
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
    onRemove: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8F3EE))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CartQuantityButton(
            onClick = onRemove,
            containerColor = Color.White,
            contentColor = KebabPrimary,
            icon = Icons.Outlined.Remove,
            contentDescription = "Kurangi jumlah"
        )

        Text(
            text = "$qty",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = KebabTextDark,
            modifier = Modifier.widthIn(min = 32.dp),
            textAlign = TextAlign.Center
        )

        CartQuantityButton(
            onClick = onAdd,
            containerColor = KebabPrimary,
            contentColor = Color.White,
            icon = Icons.Outlined.Add,
            contentDescription = "Tambah jumlah"
        )
    }
}

@Composable
private fun CartQuantityButton(
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(containerColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
