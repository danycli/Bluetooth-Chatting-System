package com.example.bluetoothchattingsystem.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothDeviceDomain
import com.example.bluetoothchattingsystem.data.bluetooth.ConnectionState
import com.example.bluetoothchattingsystem.theme.IceLatte
import com.example.bluetoothchattingsystem.theme.LatteDark
import com.example.bluetoothchattingsystem.theme.NearBlack
import com.example.bluetoothchattingsystem.theme.TheMint
import com.example.bluetoothchattingsystem.ui.BluetoothViewModel

@Composable
fun PairingBottomSheet(
    viewModel: BluetoothViewModel,
    modifier: Modifier = Modifier
) {
    val pairingDevice by viewModel.pairingDevice.collectAsState()
    val connectedDevice by viewModel.connectedDevice.collectAsState()

    val isVisible = pairingDevice != null
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Translucent Dimmed Background Overlay
        if (isVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { 
                        // If not connecting, allow dismiss on outside click
                        val isConnecting = connectedDevice != null && connectedDevice!!.address == pairingDevice?.address && connectedDevice!!.connectionState == ConnectionState.CONNECTING
                        if (!isConnecting) {
                            viewModel.dismissPairingDialog() 
                        }
                    }
            )
        }

        // Bottom Sheet slide animation
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            pairingDevice?.let { device ->
                val currentConnectionState = if (connectedDevice != null && connectedDevice!!.address == device.address) {
                    connectedDevice!!.connectionState
                } else {
                    ConnectionState.DISCONNECTED
                }

                PairingSheetContent(
                    device = device,
                    connectionState = currentConnectionState,
                    onConnectClick = { viewModel.connectDevice(device) },
                    onCancelClick = { viewModel.dismissPairingDialog() }
                )
            }
        }
    }
}

@Composable
fun PairingSheetContent(
    device: BluetoothDeviceDomain,
    connectionState: ConnectionState,
    onConnectClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag Handle Indicator
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(CircleShape)
                .background(LatteDark)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Device Icon representation
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(IceLatte)
                .border(1.5.dp, LatteDark, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Device",
                tint = NearBlack,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = device.name ?: "Unknown Device",
            color = NearBlack,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Connect to this device to begin chatting? This will create a secure, direct local channel.",
            color = NearBlack.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Connect action button / Loading state
        if (connectionState == ConnectionState.CONNECTING) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                CircularProgressIndicator(
                    color = TheMint,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Connecting...",
                    color = TheMint,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TheMint,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Connect",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 15.sp
                    )
                }

                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = borderStroke(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NearBlack
                    )
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 15.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun borderStroke() = androidx.compose.foundation.BorderStroke(1.dp, LatteDark)
