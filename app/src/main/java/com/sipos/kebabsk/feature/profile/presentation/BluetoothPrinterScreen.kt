package com.sipos.kebabsk.feature.profile.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import kotlinx.coroutines.launch
import com.sipos.kebabsk.ui.theme.KebabBg
import com.sipos.kebabsk.ui.theme.KebabDivider
import com.sipos.kebabsk.ui.theme.KebabPrimary
import com.sipos.kebabsk.ui.theme.KebabPrimaryContainer
import com.sipos.kebabsk.ui.theme.KebabSuccess
import com.sipos.kebabsk.ui.theme.KebabSuccessBg
import com.sipos.kebabsk.ui.theme.KebabTextDark
import com.sipos.kebabsk.ui.theme.KebabTextGray

private data class SavedPrinter(val name: String, val address: String)

private data class PrinterDeviceItem(
    val name: String,
    val address: String,
    val bondState: Int,
    val device: BluetoothDevice
)

private val bluetoothPermissions: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN
    )
} else {
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
}

@SuppressLint("MissingPermission")
@Composable
fun BluetoothPrinterScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val bluetoothAdapter = remember(context) { getBluetoothAdapter(context) }
    var devices by remember { mutableStateOf<List<PrinterDeviceItem>>(emptyList()) }
    var selectedAddress by rememberSaveable { mutableStateOf(loadSavedPrinter(context)?.address) }
    var hasPermission by remember { mutableStateOf(false) }
    var infoMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isDiscovering by remember { mutableStateOf(false) }
    var pairingAddress by rememberSaveable { mutableStateOf<String?>(null) }
    var connectingAddress by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    /** Melakukan koneksi RFCOMM sesungguhnya ke perangkat printer */
    fun connectToDevice(device: PrinterDeviceItem) {
        connectingAddress = device.address
        infoMessage = "Menyambungkan ke ${device.name}..."
        bluetoothAdapter?.let(::safeCancelBluetoothDiscovery)
        scope.launch {
            val result = BluetoothPrinterConnection.connect(device.device)
            if (result.isSuccess) {
                savePrinter(context, device.name, device.address)
                selectedAddress = device.address
                connectingAddress = null
                infoMessage = "${device.name} berhasil tersambung."
            } else {
                connectingAddress = null
                infoMessage = "Gagal menyambungkan ke ${device.name}. Pastikan perangkat adalah printer thermal dan menyala."
            }
        }
    }

    fun syncSavedPrinterWithBondedDevices(currentDevices: List<PrinterDeviceItem>) {
        val savedAddress = selectedAddress ?: return
        val stillBonded = currentDevices.any { it.address == savedAddress && it.bondState == BluetoothDevice.BOND_BONDED }
        if (!stillBonded) {
            selectedAddress = null
            clearSavedPrinter(context)
        }
    }

    fun updateDevices(newDevices: List<PrinterDeviceItem>) {
        devices = mergeDevices(devices, newDevices)
        syncSavedPrinterWithBondedDevices(devices)
    }

    fun refreshDevices(startDiscovery: Boolean = false) {
        hasPermission = hasBluetoothPermissionGranted(context)
        if (!hasPermission) {
            devices = emptyList()
            isDiscovering = false
            infoMessage = "Izin Bluetooth belum aktif. Izinkan akses Bluetooth agar perangkat printer bisa dicari."
            return
        }

        val adapter = bluetoothAdapter
        if (adapter == null) {
            devices = emptyList()
            isDiscovering = false
            infoMessage = "Perangkat ini tidak mendukung Bluetooth."
            return
        }

        if (!isBluetoothEnabled(adapter)) {
            devices = emptyList()
            isDiscovering = false
            infoMessage = "Bluetooth masih mati. Nyalakan Bluetooth lalu coba lagi."
            return
        }

        devices = getKnownBluetoothDevices(context)
        syncSavedPrinterWithBondedDevices(devices)

        if (!startDiscovery) {
            infoMessage = if (devices.isEmpty()) {
                "Belum ada printer tersimpan. Tekan Cari Printer jika ingin mencari perangkat baru."
            } else {
                null
            }
            return
        }

        isDiscovering = startBluetoothDiscovery(adapter)
        infoMessage = when {
            isDiscovering -> "Mencari perangkat Bluetooth di sekitar. Ketuk card perangkat untuk pair dan sambungkan."
            devices.isEmpty() -> "Belum ada perangkat yang ditemukan. Pastikan printer menyala dan mode pairing aktif."
            else -> null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it }
        if (granted) {
            refreshDevices(startDiscovery = false)
        } else {
            hasPermission = false
            infoMessage = "Izin Bluetooth ditolak. Izinkan dulu agar aplikasi bisa pair printer langsung."
        }
    }

    DisposableEffect(context, bluetoothAdapter) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = intent.getBluetoothDeviceExtra()
                            ?: return
                        updateDevices(listOf(device.toPrinterDeviceItem()))
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        isDiscovering = false
                        if (devices.isEmpty()) {
                            infoMessage = "Belum ada perangkat yang ditemukan. Pastikan printer menyala dan mode pairing aktif."
                        } else if (pairingAddress == null) {
                            infoMessage = "Ketuk perangkat untuk pair atau pilih printer yang sudah paired."
                        }
                    }

                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                        val device = intent.getBluetoothDeviceExtra()
                            ?: return
                        val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, device.bondState)
                        val item = device.toPrinterDeviceItem(bondState)
                        updateDevices(listOf(item))

                        when (bondState) {
                            BluetoothDevice.BOND_BONDING -> {
                                pairingAddress = device.address
                                infoMessage = "Memulai pairing ke ${item.name}..."
                            }

                            BluetoothDevice.BOND_BONDED -> {
                                if (pairingAddress == item.address) {
                                    pairingAddress = null
                                    // Pairing berhasil — lanjut koneksi SPP sesungguhnya
                                    connectToDevice(item)
                                } else {
                                    infoMessage = "${item.name} berhasil dipasangkan."
                                }
                            }

                            BluetoothDevice.BOND_NONE -> {
                                if (pairingAddress == device.address) {
                                    pairingAddress = null
                                    infoMessage = "Pairing ${item.name} dibatalkan atau gagal. Coba lagi."
                                }
                                if (selectedAddress == device.address) {
                                    selectedAddress = null
                                    clearSavedPrinter(context)
                                }
                            }
                        }
                    }

                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                        when (state) {
                            BluetoothAdapter.STATE_OFF, BluetoothAdapter.STATE_TURNING_OFF -> {
                                BluetoothPrinterConnection.disconnect()
                                devices = emptyList()
                                isDiscovering = false
                                connectingAddress = null
                                selectedAddress = null
                                infoMessage = "Bluetooth dimatikan. Nyalakan Bluetooth lalu coba lagi."
                            }
                            BluetoothAdapter.STATE_ON -> {
                                refreshDevices(startDiscovery = false)
                            }
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }

        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
            bluetoothAdapter?.let(::safeCancelBluetoothDiscovery)
        }
    }

    LaunchedEffect(Unit) {
        if (hasBluetoothPermissionGranted(context)) {
            refreshDevices(startDiscovery = false)
        } else {
            permissionLauncher.launch(bluetoothPermissions)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KebabBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, KebabPrimary.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = KebabPrimary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Printer Struk",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = KebabTextDark
                )
                Text(
                    text = "Bluetooth thermal",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = KebabTextGray
                )
            }
            Spacer(modifier = Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(26.dp), spotColor = Color.Black.copy(alpha = 0.04f))
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White)
                    .border(1.dp, KebabDivider.copy(alpha = 0.18f), RoundedCornerShape(26.dp))
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(KebabPrimary, KebabPrimaryContainer))),
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KebabTextDark
                    )
                    Text(
                        text = if (selectedAddress != null) "Printer siap mencetak struk" else "Pilih printer thermal Bluetooth",
                        fontSize = 13.sp,
                        color = KebabTextGray,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!infoMessage.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFFF5EC))
                        .border(1.dp, Color(0xFFE8C9A0), RoundedCornerShape(18.dp))
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

            if (!hasPermission || !isBluetoothEnabled(bluetoothAdapter)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.horizontalGradient(listOf(KebabPrimary, KebabPrimaryContainer)))
                        .clickable {
                            if (!hasPermission) {
                                permissionLauncher.launch(bluetoothPermissions)
                            } else {
                                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                            }
                        }
                        .padding(horizontal = 16.dp),
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
                        text = if (!hasPermission) "Izinkan Akses Bluetooth" else "Buka Pengaturan Bluetooth",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isDiscovering) {
                                KebabPrimary.copy(alpha = 0.72f)
                            } else {
                                KebabPrimary
                            }
                        )
                        .clickable(enabled = !isDiscovering) {
                            refreshDevices(startDiscovery = true)
                        }
                        .padding(horizontal = 16.dp),
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
                        text = if (isDiscovering) "Mencari Printer..." else "Cari Printer",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (devices.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDiscovering) "Mencari Perangkat..." else "Perangkat Tersedia",
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (devices.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(3.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.04f))
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White)
                                .border(1.dp, KebabDivider.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 20.dp, vertical = 34.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(KebabPrimary.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = KebabPrimary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isDiscovering) "Mencari printer..." else "Belum ada printer",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = KebabTextDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pastikan printer menyala dan mode pairing aktif.",
                                fontSize = 13.sp,
                                color = KebabTextGray,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                items(devices, key = { it.address }) { device ->
                    val isBonded = device.bondState == BluetoothDevice.BOND_BONDED
                    val isBonding = device.bondState == BluetoothDevice.BOND_BONDING || pairingAddress == device.address
                    val isConnecting = connectingAddress == device.address
                    val isInProgress = isBonding || isConnecting
                    val selected = selectedAddress == device.address && !isConnecting
                    val cardBackground = when {
                        selected -> KebabSuccessBg.copy(alpha = 0.5f)
                        isInProgress -> Color(0xFFFFF5EC)
                        else -> Color.White
                    }
                    val borderColor = when {
                        selected -> KebabSuccess.copy(alpha = 0.6f)
                        isInProgress -> Color(0xFFE8C9A0)
                        else -> KebabDivider.copy(alpha = 0.18f)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.04f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardBackground)
                            .border(
                                width = if (selected || isInProgress) 1.5.dp else 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable(enabled = !isConnecting && !isBonding) {
                                when {
                                    !hasPermission -> {
                                        permissionLauncher.launch(bluetoothPermissions)
                                    }

                                    isBonded -> {
                                        // Sudah paired di OS → langsung coba koneksi SPP
                                        connectToDevice(device)
                                    }

                                    else -> {
                                        // Belum paired → pair dulu, nanti auto-connect setelah BOND_BONDED
                                        pairingAddress = device.address
                                        infoMessage = "Memulai pairing ke ${device.name}..."
                                        bluetoothAdapter?.let(::safeCancelBluetoothDiscovery)
                                        val started = runCatching { device.device.createBond() }.getOrDefault(false)
                                        if (!started) {
                                            pairingAddress = null
                                            infoMessage = "Pairing ${device.name} belum bisa dimulai. Pastikan printer siap dipair."
                                        }
                                    }
                                }
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when {
                                        selected -> KebabSuccess.copy(alpha = 0.15f)
                                        isInProgress -> Color(0xFFFFE2BF)
                                        else -> Color(0xFFFFEDD5)
                                    }
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

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(
                                text = device.name,
                                fontSize = 14.sp,
                                color = KebabTextDark,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = device.address,
                                fontSize = 11.sp,
                                color = KebabTextGray,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                text = when {
                                    selected -> "Printer Tersambung"
                                    isConnecting -> "Menyambungkan..."
                                    isBonding -> "Sedang pairing..."
                                    isBonded -> "Tersedia, ketuk untuk sambungkan"
                                    else -> "Belum tersambung, ketuk untuk pair"
                                },
                                fontSize = 11.sp,
                                color = when {
                                    selected -> KebabSuccess
                                    isInProgress -> Color(0xFFB86800)
                                    isBonded -> KebabPrimary
                                    else -> KebabTextGray
                                }
                            )
                        }

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

                item { Spacer(modifier = Modifier.height(116.dp)) }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun getKnownBluetoothDevices(context: Context): List<PrinterDeviceItem> {
    if (!hasBluetoothPermissionGranted(context)) return emptyList()
    val adapter = getBluetoothAdapter(context) ?: return emptyList()
    return runCatching {
        adapter.bondedDevices
            ?.map { it.toPrinterDeviceItem() }
            .orEmpty()
            .sortedWith(deviceComparator)
    }.getOrDefault(emptyList())
}

private fun getBluetoothAdapter(context: Context): BluetoothAdapter? {
    val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    return manager?.adapter
}

private fun isBluetoothEnabled(adapter: BluetoothAdapter?): Boolean {
    return runCatching { adapter?.isEnabled == true }.getOrDefault(false)
}

private fun Intent.getBluetoothDeviceExtra(): BluetoothDevice? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
    }
}

@SuppressLint("MissingPermission")
private fun startBluetoothDiscovery(adapter: BluetoothAdapter): Boolean {
    safeCancelBluetoothDiscovery(adapter)
    return runCatching { adapter.startDiscovery() }.getOrDefault(false)
}

@SuppressLint("MissingPermission")
private fun safeCancelBluetoothDiscovery(adapter: BluetoothAdapter) {
    runCatching {
        if (adapter.isDiscovering) {
            adapter.cancelDiscovery()
        }
    }
}

private fun hasBluetoothPermissionGranted(context: Context): Boolean {
    return bluetoothPermissions.all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

@SuppressLint("MissingPermission")
private fun BluetoothDevice.toPrinterDeviceItem(overrideBondState: Int = bondState): PrinterDeviceItem {
    val safeName = name?.takeIf { it.isNotBlank() } ?: "Perangkat Bluetooth"
    val safeAddress = address ?: safeName
    return PrinterDeviceItem(
        name = safeName,
        address = safeAddress,
        bondState = overrideBondState,
        device = this
    )
}

private fun mergeDevices(
    currentDevices: List<PrinterDeviceItem>,
    incomingDevices: List<PrinterDeviceItem>
): List<PrinterDeviceItem> {
    val merged = linkedMapOf<String, PrinterDeviceItem>()
    currentDevices.forEach { merged[it.address] = it }
    incomingDevices.forEach { merged[it.address] = it }
    return merged.values.sortedWith(deviceComparator)
}

private val deviceComparator = compareByDescending<PrinterDeviceItem> { it.bondState == BluetoothDevice.BOND_BONDED }
    .thenBy { it.name.lowercase() }
    .thenBy { it.address }

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

private fun clearSavedPrinter(context: Context) {
    context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)
        .edit {
            remove("printer_name")
            remove("printer_address")
        }
}
