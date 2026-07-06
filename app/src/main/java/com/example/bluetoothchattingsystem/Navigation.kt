package com.example.bluetoothchattingsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.bluetoothchattingsystem.data.DataRepository
import com.example.bluetoothchattingsystem.data.bluetooth.ConnectionState
import com.example.bluetoothchattingsystem.theme.LatteDark
import com.example.bluetoothchattingsystem.theme.NearBlack
import com.example.bluetoothchattingsystem.theme.TheMint
import com.example.bluetoothchattingsystem.ui.BluetoothViewModel
import com.example.bluetoothchattingsystem.ui.ChatViewModel
import com.example.bluetoothchattingsystem.ui.screens.ChatDetailScreen
import com.example.bluetoothchattingsystem.ui.screens.ChatListScreen
import com.example.bluetoothchattingsystem.ui.screens.NearbyScreen
import com.example.bluetoothchattingsystem.ui.screens.OnboardingScreen
import com.example.bluetoothchattingsystem.ui.screens.PairingBottomSheet
import com.example.bluetoothchattingsystem.ui.screens.SettingsScreen
import com.example.bluetoothchattingsystem.ui.screens.SplashScreen

@Composable
fun MainNavigation(repository: DataRepository) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appUpdateManager = remember { com.example.bluetoothchattingsystem.data.update.AppUpdateManager(context) }
    val bluetoothViewModel: BluetoothViewModel = viewModel { BluetoothViewModel(repository, appUpdateManager) }
    val chatViewModel: ChatViewModel = viewModel { ChatViewModel(repository) }

    // Automatically check for updates on start (respects 24h cache limit)
    androidx.compose.runtime.LaunchedEffect(Unit) {
        bluetoothViewModel.checkForUpdates(forceCheck = false)
    }

    val backStack = rememberNavBackStack(Splash as NavKey)
    val currentKey = backStack.lastOrNull()

    // Determine if the current screen should show the bottom navigation bar
    val showBottomBar = currentKey == ChatList || currentKey == Nearby || currentKey == Settings

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = com.example.bluetoothchattingsystem.theme.IceLatte,
                    tonalElevation = 8.dp
                ) {
                    // Chat List Tab
                    NavigationBarItem(
                        selected = currentKey == ChatList,
                        onClick = {
                            if (currentKey != ChatList) {
                                backStack.removeLastOrNull()
                                backStack.add(ChatList)
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.List, contentDescription = "Chats") },
                        label = { Text("Chats") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TheMint,
                            selectedTextColor = TheMint,
                            unselectedIconColor = LatteDark,
                            unselectedTextColor = LatteDark,
                            indicatorColor = Color.White
                        )
                    )

                    // Nearby Radar Tab
                    NavigationBarItem(
                        selected = currentKey == Nearby,
                        onClick = {
                            if (currentKey != Nearby) {
                                backStack.removeLastOrNull()
                                backStack.add(Nearby)
                            }
                        },
                        icon = {
                            Canvas(modifier = Modifier.size(22.dp)) {
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
                                    color = if (currentKey == Nearby) TheMint else LatteDark,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        },
                        label = { Text("Nearby") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TheMint,
                            selectedTextColor = TheMint,
                            unselectedIconColor = LatteDark,
                            unselectedTextColor = LatteDark,
                            indicatorColor = Color.White
                        )
                    )

                    // Settings Tab
                    NavigationBarItem(
                        selected = currentKey == Settings,
                        onClick = {
                            if (currentKey != Settings) {
                                backStack.removeLastOrNull()
                                backStack.add(Settings)
                            }
                        },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TheMint,
                            selectedTextColor = TheMint,
                            unselectedIconColor = LatteDark,
                            unselectedTextColor = LatteDark,
                            indicatorColor = Color.White
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    
                    // 1. Splash Route
                    entry<Splash> { _ ->
                        val context = androidx.compose.ui.platform.LocalContext.current
                        SplashScreen(
                            onTimeout = {
                                backStack.removeLastOrNull()
                                val prefs = context.getSharedPreferences("bchat_prefs", android.content.Context.MODE_PRIVATE)
                                val hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false)
                                if (hasCompletedOnboarding) {
                                    backStack.add(ChatList)
                                } else {
                                    backStack.add(Onboarding)
                                }
                            }
                        )
                    }

                    // 2. Onboarding Route
                    entry<Onboarding> { _ ->
                        val context = androidx.compose.ui.platform.LocalContext.current
                        OnboardingScreen(
                            onFinished = {
                                val prefs = context.getSharedPreferences("bchat_prefs", android.content.Context.MODE_PRIVATE)
                                prefs.edit().putBoolean("has_completed_onboarding", true).apply()
                                backStack.removeLastOrNull()
                                backStack.add(ChatList)
                            }
                        )
                    }

                    entry<ChatList> { _ ->
                        ChatListScreen(
                            bluetoothViewModel = bluetoothViewModel,
                            onChatClick = { address, name ->
                                backStack.add(ChatDetail(address, name))
                            },
                            onSettingsClick = {
                                backStack.removeLastOrNull()
                                backStack.add(Settings)
                            },
                            onScanClick = {
                                backStack.removeLastOrNull()
                                backStack.add(Nearby)
                            }
                        )
                    }

                    // 4. Scan / Discover Route
                    entry<Nearby> { _ ->
                        NearbyScreen(
                            viewModel = bluetoothViewModel,
                            onDeviceClick = { device ->
                                backStack.add(ChatDetail(device.address, device.name ?: "Unknown"))
                            }
                        )
                    }

                    // 5. 1:1 Conversation Route
                    entry<ChatDetail> { key ->
                        ChatDetailScreen(
                            chatViewModel = chatViewModel,
                            peerAddress = key.peerAddress,
                            peerName = key.peerName,
                            onBackClick = {
                                backStack.removeLastOrNull()
                            }
                        )
                    }

                    // 6. Settings preferences Route
                    entry<Settings> { _ ->
                        SettingsScreen(
                            bluetoothViewModel = bluetoothViewModel,
                            onBackClick = {
                                backStack.removeLastOrNull()
                                backStack.add(ChatList)
                            }
                        )
                    }
                }
            )

            // Overlays & Sheets: Pairing Dialog overlays top of any active screen (e.g. Scanning)
            val pairingDevice by bluetoothViewModel.pairingDevice.collectAsState()
            val connectedDevice by bluetoothViewModel.connectedDevice.collectAsState()
            
            // Auto transition to Chat Screen once connected
            if (pairingDevice != null && connectedDevice != null && 
                connectedDevice!!.address == pairingDevice!!.address && 
                connectedDevice!!.connectionState == ConnectionState.CONNECTED) {
                
                val currentDeviceName = pairingDevice!!.name ?: "Unknown"
                val currentDeviceAddr = pairingDevice!!.address
                
                bluetoothViewModel.dismissPairingDialog()
                backStack.add(ChatDetail(currentDeviceAddr, currentDeviceName))
            }

            PairingBottomSheet(viewModel = bluetoothViewModel)

            // 7. Material 3 In-App Update Dialog
            val updateInfo by bluetoothViewModel.updateInfo.collectAsState()
            if (updateInfo != null) {
                val info = updateInfo!!
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { bluetoothViewModel.dismissUpdateDialog() },
                    title = {
                        Text(
                            text = "B-Chat Update Available",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = NearBlack
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Current: ${bluetoothViewModel.installedVersionName}",
                                    fontSize = 13.sp,
                                    color = NearBlack.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "→",
                                    fontSize = 13.sp,
                                    color = TheMint,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Latest: ${info.versionName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TheMint
                                )
                            }

                            if (info.releaseDate.isNotEmpty()) {
                                Text(
                                    text = "Released: ${info.releaseDate}",
                                    fontSize = 12.sp,
                                    color = NearBlack.copy(alpha = 0.5f)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Release Notes:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NearBlack
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 180.dp)
                                    .background(
                                        color = com.example.bluetoothchattingsystem.theme.IceLatte,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                    )
                                    .verticalScroll(rememberScrollState())
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = info.releaseNotes,
                                    fontSize = 13.sp,
                                    color = NearBlack
                                )
                            }
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl))
                                context.startActivity(browserIntent)
                                bluetoothViewModel.dismissUpdateDialog()
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = TheMint,
                                contentColor = Color.White
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text("Update Now", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = { bluetoothViewModel.dismissUpdateDialog() }
                        ) {
                            Text("Later", color = LatteDark)
                        }
                    },
                    containerColor = Color.White,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
            }
        }
    }
}
