package com.example.bluetoothchattingsystem.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetoothchattingsystem.theme.AlertRed
import com.example.bluetoothchattingsystem.theme.IceLatte
import com.example.bluetoothchattingsystem.theme.LatteDark
import com.example.bluetoothchattingsystem.theme.NearBlack
import com.example.bluetoothchattingsystem.theme.TheMint
import com.example.bluetoothchattingsystem.ui.BluetoothViewModel

data class AvatarMockup(
    val id: Int,
    val icon: ImageVector,
    val backgroundColor: Color
)

fun getAvatarById(id: Int): AvatarMockup {
    val list = listOf(
        AvatarMockup(1, Icons.Default.Person, Color(0xFF00A198)),
        AvatarMockup(2, Icons.Default.Favorite, Color(0xFFFF5252)),
        AvatarMockup(3, Icons.Default.Star, Color(0xFFFFCA28)),
        AvatarMockup(4, Icons.Default.Face, Color(0xFF42A5F5)),
        AvatarMockup(5, Icons.Default.Build, Color(0xFF66BB6A)),
        AvatarMockup(6, Icons.Default.Lock, Color(0xFFAB47BC)),
        AvatarMockup(7, Icons.Default.Home, Color(0xFFFF7043)),
        AvatarMockup(8, Icons.Default.LocationOn, Color(0xFF5C6BC0)),
        AvatarMockup(9, Icons.Default.ThumbUp, Color(0xFFEC407A)),
        AvatarMockup(10, Icons.Default.Settings, Color(0xFF26A69A)),
        AvatarMockup(11, Icons.Default.Info, Color(0xFF26C6DA)),
        AvatarMockup(12, Icons.Default.ShoppingCart, Color(0xFF78909C)),
        AvatarMockup(13, Icons.Default.Email, Color(0xFF8D6E63)),
        AvatarMockup(14, Icons.Default.Call, Color(0xFFD4AF37)),
        AvatarMockup(15, Icons.Default.Check, Color(0xFF00796B)),
        AvatarMockup(16, Icons.Default.Warning, Color(0xFFD32F2F))
    )
    return list.firstOrNull { it.id == id } ?: list[0]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    bluetoothViewModel: BluetoothViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBluetoothEnabled by bluetoothViewModel.isBluetoothEnabled.collectAsState()
    val localDeviceName by bluetoothViewModel.localDeviceName.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("bchat_prefs", android.content.Context.MODE_PRIVATE) }
    
    val packageInfo = remember {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (e: Exception) {
            null
        }
    }
    val installedVersionCode = remember(packageInfo) {
        if (packageInfo != null) {
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } else {
            1
        }
    }

    var selectedAvatarId by remember { mutableStateOf(prefs.getInt("profile_avatar_id", 1)) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf("") }
    
    var soundAlertsEnabled by remember { mutableStateOf(prefs.getBoolean("settings_sound_alerts", true)) }
    var notificationsEnabled by remember { mutableStateOf(prefs.getBoolean("settings_push_notifications", true)) }
    var autoScanEnabled by remember { mutableStateOf(prefs.getBoolean("settings_auto_scan", false)) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    var manualCheckTriggered by remember { mutableStateOf(false) }

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
            
            // Section 1: Profile Info Card (Editable Device Name & Mockup Avatars chooser)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentAvatar = getAvatarById(selectedAvatarId)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(currentAvatar.backgroundColor)
                                .clickable { showAvatarDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = currentAvatar.icon,
                                contentDescription = "Profile Avatar Mockup",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    editNameInput = localDeviceName
                                    showEditNameDialog = true
                                }
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
                                text = "Discoverable: 3600s (Active Mesh Mode)",
                                color = NearBlack.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                editNameInput = localDeviceName
                                showEditNameDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Device Name",
                                tint = TheMint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
                        val localMacAddress = remember {
                            var mac = prefs.getString("local_mac_address", null)
                            if (mac == null) {
                                val random = java.util.Random()
                                mac = String.format(
                                    java.util.Locale.US,
                                    "02:%02X:%02X:%02X:%02X:%02X",
                                    random.nextInt(256),
                                    random.nextInt(256),
                                    random.nextInt(256),
                                    random.nextInt(256),
                                    random.nextInt(256)
                                )
                                prefs.edit().putString("local_mac_address", mac).apply()
                            }
                            mac
                        }
                        Text("MAC Address", fontSize = 12.sp, color = NearBlack.copy(alpha = 0.6f))
                        Text(localMacAddress, fontSize = 12.sp, color = NearBlack)
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
                            onCheckedChange = { 
                                notificationsEnabled = it 
                                prefs.edit().putBoolean("settings_push_notifications", it).apply()
                            },
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
                            onCheckedChange = { 
                                soundAlertsEnabled = it 
                                prefs.edit().putBoolean("settings_sound_alerts", it).apply()
                            },
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
                            onCheckedChange = { 
                                autoScanEnabled = it 
                                prefs.edit().putBoolean("settings_auto_scan", it).apply()
                            },
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
                        Text(bluetoothViewModel.installedVersionName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NearBlack)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Build Number", fontSize = 13.sp, color = NearBlack.copy(alpha = 0.6f))
                        Text(installedVersionCode.toString(), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NearBlack)
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

                    Spacer(modifier = Modifier.height(6.dp))

                    val isCheckingUpdate by bluetoothViewModel.isCheckingUpdate.collectAsState()
                    val updateCheckError by bluetoothViewModel.updateCheckError.collectAsState()
                    val updateInfo by bluetoothViewModel.updateInfo.collectAsState()

                    LaunchedEffect(updateCheckError) {
                        if (updateCheckError != null) {
                            android.widget.Toast.makeText(context, updateCheckError, android.widget.Toast.LENGTH_LONG).show()
                            bluetoothViewModel.clearUpdateCheckError()
                        }
                    }

                    LaunchedEffect(isCheckingUpdate) {
                        if (manualCheckTriggered && !isCheckingUpdate) {
                            manualCheckTriggered = false
                            if (updateInfo == null && updateCheckError == null) {
                                android.widget.Toast.makeText(context, "B-Chat is up to date", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    androidx.compose.material3.Button(
                        onClick = {
                            manualCheckTriggered = true
                            bluetoothViewModel.checkForUpdates(forceCheck = true)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCheckingUpdate,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = TheMint,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isCheckingUpdate) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Checking...", color = Color.White)
                        } else {
                            Text("Check for Updates", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Avatar chooser Grid dialog
    if (showAvatarDialog) {
        val mockups = remember {
            listOf(
                AvatarMockup(1, Icons.Default.Person, Color(0xFF00A198)),
                AvatarMockup(2, Icons.Default.Favorite, Color(0xFFFF5252)),
                AvatarMockup(3, Icons.Default.Star, Color(0xFFFFCA28)),
                AvatarMockup(4, Icons.Default.Face, Color(0xFF42A5F5)),
                AvatarMockup(5, Icons.Default.Build, Color(0xFF66BB6A)),
                AvatarMockup(6, Icons.Default.Lock, Color(0xFFAB47BC)),
                AvatarMockup(7, Icons.Default.Home, Color(0xFFFF7043)),
                AvatarMockup(8, Icons.Default.LocationOn, Color(0xFF5C6BC0)),
                AvatarMockup(9, Icons.Default.ThumbUp, Color(0xFFEC407A)),
                AvatarMockup(10, Icons.Default.Settings, Color(0xFF26A69A)),
                AvatarMockup(11, Icons.Default.Info, Color(0xFF26C6DA)),
                AvatarMockup(12, Icons.Default.ShoppingCart, Color(0xFF78909C)),
                AvatarMockup(13, Icons.Default.Email, Color(0xFF8D6E63)),
                AvatarMockup(14, Icons.Default.Call, Color(0xFFD4AF37)),
                AvatarMockup(15, Icons.Default.Check, Color(0xFF00796B)),
                AvatarMockup(16, Icons.Default.Warning, Color(0xFFD32F2F))
            )
        }

        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text("Choose Profile Avatar", fontWeight = FontWeight.Bold, color = NearBlack) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select from 16 default offline mockup icons to customize your profile card.",
                        fontSize = 12.sp,
                        color = NearBlack.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (i in 0 until 4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                            ) {
                                for (j in 0 until 4) {
                                    val index = i * 4 + j
                                    if (index < mockups.size) {
                                        val avatar = mockups[index]
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .background(avatar.backgroundColor)
                                                .border(
                                                    width = if (selectedAvatarId == avatar.id) 3.dp else 0.dp,
                                                    color = if (selectedAvatarId == avatar.id) NearBlack else Color.Transparent,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    prefs.edit().putInt("profile_avatar_id", avatar.id).apply()
                                                    selectedAvatarId = avatar.id
                                                    showAvatarDialog = false
                                                    bluetoothViewModel.changeLocalDeviceName(localDeviceName)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = avatar.icon,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarDialog = false }) {
                    Text("Close", color = LatteDark)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
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
