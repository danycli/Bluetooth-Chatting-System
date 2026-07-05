package com.example.bluetoothchattingsystem.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetoothchattingsystem.theme.AlertRed
import com.example.bluetoothchattingsystem.theme.IceLatte
import com.example.bluetoothchattingsystem.theme.LatteDark
import com.example.bluetoothchattingsystem.theme.NearBlack
import com.example.bluetoothchattingsystem.theme.SoftWhite
import com.example.bluetoothchattingsystem.theme.TheMint
import com.example.bluetoothchattingsystem.ui.BluetoothViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    bluetoothViewModel: BluetoothViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBluetoothEnabled by bluetoothViewModel.isBluetoothEnabled.collectAsState()
    val localDeviceName by bluetoothViewModel.localDeviceName.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf("") }
    
    var soundAlertsEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoScanEnabled by remember { mutableStateOf(false) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        color = NearBlack,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                            tint = NearBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IceLatte
                )
            )
        },
        containerColor = IceLatte
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Section 1: Profile Info Card (Editable Device Name)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .clickable {
                            editNameInput = localDeviceName
                            showEditNameDialog = true
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(TheMint.copy(alpha = 0.15f))
                                .border(1.dp, TheMint, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ME",
                                fontWeight = FontWeight.Bold,
                                color = TheMint,
                                fontSize = 20.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = localDeviceName,
                                color = NearBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Discoverable: 300s (Active Mesh Mode)",
                                color = NearBlack.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Device Name",
                            tint = TheMint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(IceLatte))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Detail items
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bluetooth Hardware", fontSize = 12.sp, color = NearBlack.copy(alpha = 0.6f))
                        Text(if (isBluetoothEnabled) "Active" else "Disabled", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isBluetoothEnabled) TheMint else AlertRed)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("MAC Address", fontSize = 12.sp, color = NearBlack.copy(alpha = 0.6f))
                        Text("02:00:00:00:00:00 (Randomized)", fontSize = 12.sp, color = NearBlack)
                    }
                }
            }

            // Section 2: Toggles Preference Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "PREFERENCES",
                        color = LatteDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )

                    // Notifications Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Push Notifications",
                                color = NearBlack,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Receive alerts on incoming messages",
                                color = NearBlack.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TheMint,
                                uncheckedThumbColor = LatteDark,
                                uncheckedTrackColor = IceLatte
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(IceLatte))

                    // Sound Alert Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Message Sound Alerts",
                                color = NearBlack,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Play alert sounds for packets",
                                color = NearBlack.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = soundAlertsEnabled,
                            onCheckedChange = { soundAlertsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TheMint,
                                uncheckedThumbColor = LatteDark,
                                uncheckedTrackColor = IceLatte
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(IceLatte))

                    // Auto Scan Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Auto Scan Nodes",
                                color = NearBlack,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Automatically discover nearby mesh nodes",
                                color = NearBlack.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = autoScanEnabled,
                            onCheckedChange = { autoScanEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TheMint,
                                uncheckedThumbColor = LatteDark,
                                uncheckedTrackColor = IceLatte
                            )
                        )
                    }
                }
            }

            // Section 3: Data Safety Card (Danger Zone)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, AlertRed.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "DANGER ZONE",
                        color = AlertRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showClearConfirmation = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Wipe Local Mesh Vault",
                                color = AlertRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Permanently clear all local messaging logs and database vaults.",
                                color = NearBlack.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = AlertRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Section 4: About B-Chat
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TheMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ABOUT B-CHAT",
                            color = TheMint,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Version", fontSize = 13.sp, color = NearBlack.copy(alpha = 0.6f))
                        Text("1.0.4", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NearBlack)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Build Number", fontSize = 13.sp, color = NearBlack.copy(alpha = 0.6f))
                        Text("104", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NearBlack)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Developer", fontSize = 13.sp, color = NearBlack.copy(alpha = 0.6f))
                        Text("DeepMind Antigravity", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NearBlack)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("GitHub Source", fontSize = 13.sp, color = NearBlack.copy(alpha = 0.6f))
                        Text("github.com/danycli/BChat", fontSize = 13.sp, color = TheMint, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("License", fontSize = 13.sp, color = NearBlack.copy(alpha = 0.6f))
                        Text("Apache License 2.0", fontSize = 13.sp, color = NearBlack)
                    }
                }
            }
        }
    }

    // Rename dialog overlay
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Change Device Name", fontWeight = FontWeight.Bold, color = NearBlack) },
            text = {
                Column {
                    Text(
                        text = "This updates your system Bluetooth friendly name. Other B-Chat nodes discover you by this name.",
                        fontSize = 12.sp,
                        color = NearBlack.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TheMint,
                            unfocusedBorderColor = LatteDark,
                            focusedTextColor = NearBlack,
                            unfocusedTextColor = NearBlack
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editNameInput.isNotBlank()) {
                            bluetoothViewModel.changeLocalDeviceName(editNameInput.trim())
                        }
                        showEditNameDialog = false
                    }
                ) {
                    Text("Save", color = TheMint, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = LatteDark)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Vault Wipe Confirmation
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Wipe Local Vault", color = NearBlack, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete all local messaging databases and keys? This cannot be undone.", color = NearBlack.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(onClick = {
                    bluetoothViewModel.clearAllConversations()
                    showClearConfirmation = false
                }) {
                    Text("Clear All", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel", color = LatteDark)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
