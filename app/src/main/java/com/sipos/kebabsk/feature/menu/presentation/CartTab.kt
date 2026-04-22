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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Text(
                    text = "Keranjang Belanja",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KebabTextDark,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Belum ada item di keranjang",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = KebabTextGray
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Belum ada item di keranjang",
                        color = KebabTextGray
                    )
                }
            }
        } else {
            val formatPrice = { amount: Double ->
                toRupiah(amount).replace("Rp", "Rp ").replace(",00", "")
            }

            // Scrollable Content: Header + Items + Summary + Buttons
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header
                item {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = "Keranjang Belanja",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KebabTextDark,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Anda memiliki ${cartItems.sumOf { it.qty }} item dalam pesanan Anda.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = KebabTextGray
                        )
                    }
                }

                // 2. Cart Items
                items(count = cartItems.size, key = { cartItems[it].variantId }) { index ->
                    val item = cartItems[index]
                    val displayVariantName = if (item.variantName.startsWith(item.menuName, ignoreCase = true)) {
                        item.variantName.substring(item.menuName.length).trim()
                    } else {
                        item.variantName
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFDDC1AE).copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Row 1: Title and Delete Icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.menuName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KebabTextDark
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = displayVariantName,
                                        fontSize = 13.sp,
                                        color = KebabTextGray
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteVariant(item.variantId) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Hapus",
                                        tint = KebabTextGray
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Row 3: Quantity Control and Price
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Quantity Control Pill
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(KebabBg)
                                        .border(1.dp, Color(0xFFDDC1AE).copy(alpha = 0.5f), RoundedCornerShape(50))
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Decrease Button
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .clickable { onRemoveVariant(item.variantId) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KebabPrimary)
                                    }
                                    
                                    Text(
                                        text = "${item.qty}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KebabTextDark,
                                        modifier = Modifier.width(32.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    // Increase Button
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .clickable { 
                                                onAddVariant(item.menuName, item.variantId, item.variantName, item.price)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KebabPrimary)
                                    }
                                }
                                
                                // Price
                                Text(
                                    text = formatPrice(item.price * item.qty),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KebabTextDark
                                )
                            }
                        }
                    }
                }

                // 3. Summary Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFFF8F3EE))
                            .padding(32.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 14.sp, color = KebabTextGray)
                            Text(formatPrice(totalAmount), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = KebabTextGray)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("TOTAL BAYAR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KebabTextGray, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatPrice(totalAmount), fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = KebabPrimary, letterSpacing = (-2).sp)
                        }
                        // Added padding at the bottom of the summary card inside LazyColumn
                        // so that it doesn't get covered by the fixed buttons
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } // End of LazyColumn

            // Fixed Buttons Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KebabBg)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
            ) {
                Button(
                    onClick = onNavigateToPayment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KebabPrimary)
                ) {
                    Text("Lanjut ke Pembayaran", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onBackToMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color(0xFFD6C8B8))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = KebabTextGray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kembali ke Menu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KebabTextGray)
                    }
                }
            }
        }
    }
}
