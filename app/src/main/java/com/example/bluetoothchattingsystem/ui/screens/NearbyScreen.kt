package com.example.bluetoothchattingsystem.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothDeviceDomain
import com.example.bluetoothchattingsystem.data.bluetooth.ConnectionState
import com.example.bluetoothchattingsystem.theme.AlertRed
import com.example.bluetoothchattingsystem.theme.IceLatte
import com.example.bluetoothchattingsystem.theme.LatteDark
import com.example.bluetoothchattingsystem.theme.NearBlack
import com.example.bluetoothchattingsystem.theme.SoftWhite
import com.example.bluetoothchattingsystem.theme.TheMint
import com.example.bluetoothchattingsystem.ui.BluetoothViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(
    viewModel: BluetoothViewModel,
    onDeviceClick: (BluetoothDeviceDomain) -> Unit,
    modifier: Modifier = Modifier
) {
    val scannedDevices by viewModel.scannedDevices.collectAsState()
    val connectedDevice by viewModel.connectedDevice.collectAsState()
    val isBluetoothEnabled by viewModel.isBluetoothEnabled.collectAsState()

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    var infoDevice by remember { mutableStateOf<BluetoothDeviceDomain?>(null) }
    var isFavorite by remember { mutableStateOf(false) }

    // Trigger scan on entering screen
    LaunchedEffect(isBluetoothEnabled) {
        if (isBluetoothEnabled) {
            viewModel.startScan()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            viewModel.startScan()
            kotlinx.coroutines.delay(1500)
            isRefreshing = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Radar Discovery",
                        color = NearBlack,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isBluetoothEnabled) TheMint else LatteDark)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = { viewModel.setBluetoothEnabled(!isBluetoothEnabled) }) {
                            Canvas(modifier = Modifier.size(20.dp)) {
                                val w = size.width
                                val h = size.height
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(w * 0.5f, h * 0.1f)
                                    lineTo(w * 0.5f, h * 0.9f)
                                    lineTo(w * 0.75f, h * 0.65f)
                                    lineTo(w * 0.25f, h * 0.35f)
                                    lineTo(w * 0.75f, h * 0.35f)
                                    lineTo(w * 0.5f, h * 0.1f)
                                }
                                drawPath(
                                    path,
                                    color = if (isBluetoothEnabled) TheMint else LatteDark,
                                    style = Stroke(width = 2.5f.dp.toPx())
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IceLatte
                )
            )
        },
        floatingActionButton = {
            if (isBluetoothEnabled) {
                FloatingActionButton(
                    onClick = { viewModel.startScan() },
                    containerColor = TheMint,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan Devices",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        containerColor = IceLatte
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            state = pullToRefreshState,
            onRefresh = { isRefreshing = true },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (!isBluetoothEnabled) {
                    BluetoothOffEmptyState(
                        onEnableClick = { viewModel.requestBluetoothEnable() }
                    )
                } else {
                    // Pulsing sweep rings
                    RadarScanSection(scannedCount = scannedDevices.size)

                    if (scannedDevices.isEmpty()) {
                        // Skeleton loader cards while list is scanning empty
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            repeat(3) {
                                ShimmerDeviceCard()
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(scannedDevices) { device ->
                                val currentConnected = connectedDevice
                                val isThisConnected = currentConnected != null && currentConnected.address == device.address
                                val deviceWithConnectionState = if (isThisConnected) currentConnected!! else device

                                DeviceListItemCard(
                                    device = deviceWithConnectionState,
                                    onClick = {
                                        if (deviceWithConnectionState.connectionState == ConnectionState.CONNECTED) {
                                            onDeviceClick(deviceWithConnectionState)
                                        } else {
                                            viewModel.showPairingDialog(deviceWithConnectionState)
                                        }
                                    },
                                    onLongClick = {
                                        infoDevice = deviceWithConnectionState
                                    }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Long Press Device Info Alert dialog
    if (infoDevice != null) {
        val dev = infoDevice!!
        val hash = remember(dev.address) { Math.abs(dev.address.hashCode()) }
        val rssi = -60 - (hash % 25)
        val distance = remember(rssi) {
            val calculated = Math.pow(10.0, (-69 - rssi) / 20.0)
            String.format(Locale.US, "%.1f meters", calculated)
        }

        AlertDialog(
            onDismissRequest = { infoDevice = null },
            title = { Text(dev.name ?: "Unnamed Device", fontWeight = FontWeight.Bold, color = NearBlack) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("MAC Address: ${dev.address}", fontSize = 13.sp, color = NearBlack)
                    Text("Signal Strength: ${rssi} dBm (RSSI)", fontSize = 13.sp, color = NearBlack)
                    Text("Estimated Distance: $distance", fontSize = 13.sp, color = NearBlack)
                    Text("Last Discovered: Just now (Active discovery scan)", fontSize = 13.sp, color = NearBlack.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add to trusted nodes", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        androidx.compose.material3.Switch(
                            checked = isFavorite,
                            onCheckedChange = { isFavorite = it },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TheMint
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { infoDevice = null }) {
                    Text("Done", color = TheMint, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun RadarScanSection(scannedCount: Int) {
    val transition = rememberInfiniteTransition(label = "radar")
    val radarSweep1 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep1"
    )
    val radarSweep2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, delayMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep2"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius1 = (size.width / 2) * radarSweep1
                val alpha1 = (1.0f - (radarSweep1 - 0.8f) / 1.4f).coerceIn(0.0f, 1.0f)
                drawCircle(
                    color = TheMint,
                    radius = radius1,
                    style = Stroke(width = 2.dp.toPx()),
                    alpha = alpha1 * 0.4f
                )

                val radius2 = (size.width / 2) * radarSweep2
                val alpha2 = (1.0f - (radarSweep2 - 0.8f) / 1.4f).coerceIn(0.0f, 1.0f)
                drawCircle(
                    color = TheMint,
                    radius = radius2,
                    style = Stroke(width = 2.dp.toPx()),
                    alpha = alpha2 * 0.4f
                )
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(TheMint.copy(alpha = 0.15f))
                    .border(2.dp, TheMint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Searching",
                    tint = TheMint,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        Text(
            text = if (scannedCount > 0) "Found $scannedCount devices nearby" else "Scanning B-Chat nodes...",
            color = NearBlack,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ShimmerDeviceCard() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(LatteDark.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(width = 120.dp, height = 16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(LatteDark.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(LatteDark.copy(alpha = alpha))
                )
            }
            Box(
                modifier = Modifier
                    .size(width = 70.dp, height = 28.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(LatteDark.copy(alpha = alpha))
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceListItemCard(
    device: BluetoothDeviceDomain,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hash = remember(device.address) { Math.abs(device.address.hashCode()) }
    val rssi = -60 - (hash % 25)
    val distance = remember(rssi) {
        val calculated = Math.pow(10.0, (-69 - rssi) / 20.0)
        String.format(Locale.US, "%.1f m", calculated)
    }
    val bars = when {
        rssi > -68 -> 4
        rssi > -76 -> 3
        else -> 2
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val initials = (device.name ?: "Unnamed").take(2).uppercase()
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (device.connectionState == ConnectionState.CONNECTED) TheMint.copy(alpha = 0.15f) else IceLatte)
                        .border(1.dp, if (device.connectionState == ConnectionState.CONNECTED) TheMint else LatteDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        color = if (device.connectionState == ConnectionState.CONNECTED) TheMint else NearBlack,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = device.name ?: "Unnamed Device",
                        color = NearBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "RSSI: ${rssi}dBm • Est: $distance",
                            color = NearBlack.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(4) { idx ->
                                Box(
                                    modifier = Modifier
                                        .size(width = 3.dp, height = (4 + idx * 3).dp)
                                        .background(if (idx < bars) TheMint else LatteDark.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            val buttonColor = if (device.connectionState == ConnectionState.CONNECTED) TheMint else Color.White
            val buttonText = when (device.connectionState) {
                ConnectionState.CONNECTED -> "Connected"
                ConnectionState.CONNECTING -> "Pairing..."
                ConnectionState.DISCONNECTED -> "Connect"
            }
            val borderStroke = if (device.connectionState == ConnectionState.CONNECTED) null else BorderStroke(1.dp, TheMint)
            
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = if (device.connectionState == ConnectionState.CONNECTED) Color.White else TheMint
                ),
                border = borderStroke,
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BluetoothOffEmptyState(onEnableClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Bluetooth Off",
            tint = AlertRed,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Bluetooth is disabled",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = NearBlack
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "B-Chat requires Bluetooth hardware to scan, pair, and swap offline message packets mesh-to-mesh.",
            color = NearBlack.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onEnableClick,
            colors = ButtonDefaults.buttonColors(containerColor = TheMint),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
        ) {
            Text("Enable Bluetooth", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
