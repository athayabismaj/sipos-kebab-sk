import re

with open('app/src/main/java/com/sipos/kebabsk/feature/transactions/presentation/TransactionsScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

new_dialog = '''        Dialog(
            onDismissRequest = { if (!uiState.isVoiding) transactionToVoid = null },
            properties = DialogProperties(dismissOnBackPress = !uiState.isVoiding, dismissOnClickOutside = !uiState.isVoiding)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = KebabCardBg,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 320.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Alasan Pembatalan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KebabTextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tentukan perlakuan bahan baku dari transaksi ini.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = KebabTextGray
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (uiState.isVoiding) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = KebabPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Memproses...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = KebabTextDark
                            )
                        }
                    } else {
                        // Kembalikan ke Stok
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = KebabSuccess.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KebabSuccess.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (sessionId != null) {
                                        viewModel.voidTransaction(transactionToVoid!!, VoidReason.RESTOCK, sessionId)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Recycling,
                                    contentDescription = null,
                                    tint = KebabSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Kembalikan ke Stok",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KebabSuccess
                                    )
                                    Text(
                                        text = "Bahan masih layak pakai",
                                        fontSize = 12.sp,
                                        color = KebabTextGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Buang sebagai Sampah
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = KebabErrorBg.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KebabErrorText.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (sessionId != null) {
                                        viewModel.voidTransaction(transactionToVoid!!, VoidReason.WASTE, sessionId)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = null,
                                    tint = KebabErrorText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Buang sebagai Sampah",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KebabErrorText
                                    )
                                    Text(
                                        text = "Bahan rusak / tak layak",
                                        fontSize = 12.sp,
                                        color = KebabTextGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { transactionToVoid = null },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Batal",
                                    color = KebabTextGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }'''

import re
pattern = r"        Dialog\(\s*onDismissRequest = \{ if \(!uiState\.isVoiding\) transactionToVoid = null \},\s*properties = DialogProperties\(dismissOnBackPress = !uiState\.isVoiding, dismissOnClickOutside = !uiState\.isVoiding\)\s*\)\s*\{(?:.*?)(?=\n        \n    if \(uiState\.showRangePicker\))"
new_content, count = re.subn(pattern, new_dialog, content, flags=re.DOTALL)

print(f"Replacements made: {count}")

if count == 1:
    with open('app/src/main/java/com/sipos/kebabsk/feature/transactions/presentation/TransactionsScreen.kt', 'w', encoding='utf-8') as f:
        f.write(new_content)
