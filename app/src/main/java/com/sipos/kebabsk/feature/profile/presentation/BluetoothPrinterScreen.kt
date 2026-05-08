package com.sipos.kebabsk.feature.profile.presentation

import android.annotation.SuppressLint

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabDivider
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabPrimaryContainer
import com.sipos.kebabsk.ui.theme.KebabSuccess
import com.sipos.kebabsk.ui.theme.KebabSuccessBg
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray

private data class SavedPrinter(val name: String, val address: String)

@SuppressLint("MissingPermission")
@Composable
fun BluetoothPrinterScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var devices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var selectedAddress by rememberSaveable { mutableStateOf(loadSavedPrinter(context)?.address) }
    var hasPermission by remember { mutableStateOf(false) }
    var infoMessage by rememberSaveable { mutableStateOf<String?>(null) }

    fun refreshDevices() {
        hasPermission = isBluetoothPermissionGranted(context)
        devices = getBondedBluetoothDevices(context)
        infoMessage = when {
            !hasPermission -> "Izin Bluetooth belum aktif. Aktifkan izin agar daftar printer bisa dibaca."
            devices.isEmpty() -> "Belum ada printer yang dipasangkan. Pair dulu di pengaturan Bluetooth HP Anda."
            else -> null
        }
    }

    LaunchedEffect(Unit) { refreshDevices() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KebabBg)
    ) {
        // === TOP BAR ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = KebabPrimary
                )
            }
            Text(
                text = "Printer Struk",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = KebabPrimary
            )
            // Single refresh action in top bar only
            IconButton(onClick = { refreshDevices() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Muat Ulang",
                    tint = KebabPrimary
                )
            }
        }

        HorizontalDivider(color = KebabDivider.copy(alpha = 0.5f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // === HEADER SECTION ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(listOf(KebabPrimary, KebabPrimaryContainer))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column {
                    Text(
                        text = "Koneksi Printer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = KebabTextDark
                    )
                    Text(
                        text = "Pilih perangkat Bluetooth untuk cetak struk",
                        fontSize = 12.sp,
                        color = KebabTextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === STATUS / INFO BANNER ===
            if (!infoMessage.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF5EC))
                        .border(1.dp, Color(0xFFE8C9A0), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFB86800),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = infoMessage.orEmpty(),
                        color = Color(0xFF8A4B00),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // === OPEN BLUETOOTH ACTION ===
            if (!hasPermission || devices.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(KebabPrimary)
                        .clickable { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buka Pengaturan Bluetooth",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // === DEVICE LIST HEADER ===
            if (devices.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Perangkat Tersedia",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KebabTextGray,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${devices.size} perangkat",
                        fontSize = 11.sp,
                        color = KebabTextGray.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // === DEVICE LIST ===
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(devices, key = { it.address ?: it.name.orEmpty() }) { device ->
                    val name = device.name?.takeIf { it.isNotBlank() } ?: "Perangkat Bluetooth"
                    val address = device.address ?: "-"
                    val selected = selectedAddress == address

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) KebabSuccessBg.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (selected) 1.5.dp else 0.dp,
                                color = if (selected) KebabSuccess.copy(alpha = 0.6f) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                savePrinter(context, name, address)
                                selectedAddress = address
                                infoMessage = null
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Device icon
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) KebabSuccess.copy(alpha = 0.15f)
                                    else Color(0xFFFFEDD5)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selected) Icons.Default.BluetoothConnected else Icons.Default.Print,
                                contentDescription = null,
                                tint = if (selected) KebabSuccess else KebabPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Device info
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(
                                text = name,
                                fontSize = 14.sp,
                                color = if (selected) KebabTextDark else KebabTextDark,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = address,
                                fontSize = 11.sp,
                                color = KebabTextGray,
                                letterSpacing = 0.3.sp
                            )
                        }

                        // Selected checkmark
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(KebabSuccess),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Terpilih",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun getBondedBluetoothDevices(context: Context): List<BluetoothDevice> {
    if (!isBluetoothPermissionGranted(context)) return emptyList()
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
    val adapter = bluetoothManager?.adapter ?: return emptyList()
    return try {
        adapter.bondedDevices?.toList().orEmpty().sortedBy { it.name ?: it.address }
    } catch (_: SecurityException) {
        emptyList()
    }
}

private fun isBluetoothPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun loadSavedPrinter(context: Context): SavedPrinter? {
    val prefs = context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
    val name = prefs.getString("printer_name", null)
    val address = prefs.getString("printer_address", null)
    return if (!name.isNullOrBlank() && !address.isNullOrBlank()) {
        SavedPrinter(name, address)
    } else {
        null
    }
}

private fun savePrinter(context: Context, name: String, address: String) {
    context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
        .edit {
            putString("printer_name", name)
            putString("printer_address", address)
        }
}
