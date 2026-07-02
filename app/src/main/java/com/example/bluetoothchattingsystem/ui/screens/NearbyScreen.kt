package com.example.bluetoothchattingsystem.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothDeviceDomain
import com.example.bluetoothchattingsystem.data.bluetooth.ConnectionState
import com.example.bluetoothchattingsystem.theme.IceLatte
import com.example.bluetoothchattingsystem.theme.LatteDark
import com.example.bluetoothchattingsystem.theme.NearBlack
import com.example.bluetoothchattingsystem.theme.SoftWhite
import com.example.bluetoothchattingsystem.theme.TheMint
import com.example.bluetoothchattingsystem.ui.BluetoothViewModel

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

    // Scan for devices on entering the screen
    LaunchedEffect(isBluetoothEnabled) {
        if (isBluetoothEnabled) {
            viewModel.startScan()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nearby Devices",
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
                        // Bluetooth active status indicator dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isBluetoothEnabled) TheMint else LatteDark)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = { viewModel.setBluetoothEnabled(!isBluetoothEnabled) }) {
                            // custom bluetooth status icon
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
                    shape = CircleShape
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!isBluetoothEnabled) {
                BluetoothOffEmptyState()
            } else {
                // Radar Scan Section
                RadarScanSection()

                if (scannedDevices.isEmpty()) {
                    NoDevicesEmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                }
                            )
                        }
                        // Bottom spacing padding for FAB
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RadarScanSection() {
    val transition = rememberInfiniteTransition(label = "radar")
    val radarSweep by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "sweep"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            // Radar pulse ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = (size.width / 2) * radarSweep
                val alpha = (1.0f - (radarSweep - 1.0f) / 1.2f).coerceIn(0.0f, 1.0f)
                drawCircle(
                    color = TheMint,
                    radius = radius,
                    style = Stroke(width = 1.5f.dp.toPx()),
                    alpha = alpha * 0.6f
                )
            }

            // Radar static icon container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TheMint.copy(alpha = 0.15f))
                    .border(1.dp, TheMint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Searching",
                    tint = TheMint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Scanning for nearby devices...",
            color = NearBlack,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DeviceListItemCard(
    device: BluetoothDeviceDomain,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, LatteDark, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Device Avatar Badge
            val initials = (device.name ?: "U").take(2).uppercase()
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (device.connectionState == ConnectionState.CONNECTED) TheMint.copy(alpha = 0.15f) else IceLatte)
                    .border(
                        width = 1.5f.dp,
                        color = if (device.connectionState == ConnectionState.CONNECTED) TheMint else LatteDark,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontWeight = FontWeight.SemiBold,
                    color = if (device.connectionState == ConnectionState.CONNECTED) TheMint else NearBlack,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = device.name ?: "Unknown Device",
                    color = NearBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = when (device.connectionState) {
                        ConnectionState.CONNECTED -> "Connected"
                        ConnectionState.CONNECTING -> "Connecting..."
                        ConnectionState.DISCONNECTED -> "Available"
                    },
                    color = NearBlack.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        // Connection status pill
        val pillBg = when (device.connectionState) {
            ConnectionState.CONNECTED -> TheMint.copy(alpha = 0.15f)
            ConnectionState.CONNECTING -> SoftWhite
            ConnectionState.DISCONNECTED -> Color.Transparent
        }
        val pillBorderColor = when (device.connectionState) {
            ConnectionState.CONNECTED -> TheMint
            ConnectionState.CONNECTING -> TheMint
            ConnectionState.DISCONNECTED -> LatteDark
        }
        val pillTextColor = when (device.connectionState) {
            ConnectionState.CONNECTED -> TheMint
            ConnectionState.CONNECTING -> TheMint
            ConnectionState.DISCONNECTED -> NearBlack.copy(alpha = 0.6f)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(pillBg)
                .border(1.dp, pillBorderColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (device.connectionState != ConnectionState.DISCONNECTED) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(TheMint)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = when (device.connectionState) {
                        ConnectionState.CONNECTED -> "Connected"
                        ConnectionState.CONNECTING -> "Connecting..."
                        ConnectionState.DISCONNECTED -> "Available"
                    },
                    color = pillTextColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BluetoothOffEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Bluetooth Off",
            tint = LatteDark,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bluetooth is off",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = NearBlack
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Please turn on Bluetooth in settings to scan and connect to nearby friends.",
            color = NearBlack.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun NoDevicesEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "No Devices",
            tint = LatteDark,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No devices nearby",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = NearBlack
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Move closer or make sure your partner has discoverability enabled.",
            color = NearBlack.copy(alpha = 0.6f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
