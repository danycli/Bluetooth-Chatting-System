package com.example.bluetoothchattingsystem

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.bluetoothchattingsystem.data.DataRepository
import com.example.bluetoothchattingsystem.data.bluetooth.AndroidBluetoothController
import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothController
import com.example.bluetoothchattingsystem.theme.BluetoothChattingSystemTheme

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothController: BluetoothController
    private lateinit var repository: DataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve instances from BChatApplication
        val app = application as BChatApplication
        bluetoothController = app.bluetoothController
        repository = app.repository

        // Launch background Foreground listening Service
        val serviceIntent = Intent(this, ChatService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        // Configure edge-to-edge transparent system bars with dark icons (since base is light/latte background)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // Activity Result Launcher for In-App Bluetooth Enable requests
        val requestBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // If Bluetooth enabled successfully, start server and scanning
                (bluetoothController as? AndroidBluetoothController)?.let {
                    it.startServer()
                    it.startDiscovery()
                }
            }
        }

        // Assign the enable request callback to controller
        bluetoothController.onRequestBluetoothEnable = {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetoothLauncher.launch(enableBtIntent)
        }

        // Configure all permissions for both legacy and API 31+ / 33+ contexts
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            val connectGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                results[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true
            
            if (connectGranted) {
                (bluetoothController as? AndroidBluetoothController)?.startServer()
            }
        }

        val needsRequest = permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needsRequest) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }

        setContent {
            BluetoothChattingSystemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    MainNavigation(repository = repository)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Do not release the controller here so that the background ChatService stays active!
    }
}
