package com.example.bluetoothchattingsystem

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.bluetoothchattingsystem.data.DataRepository
import com.example.bluetoothchattingsystem.data.bluetooth.AndroidBluetoothController
import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothController
import com.example.bluetoothchattingsystem.data.bluetooth.MockBluetoothController
import com.example.bluetoothchattingsystem.data.local.ChatDatabase
import com.example.bluetoothchattingsystem.theme.BluetoothChattingSystemTheme

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothController: BluetoothController
    private lateinit var repository: DataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Robust check for Emulator environment to switch mocked fallbacks
        val isEmulator = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.BOARD.contains("emulator")
                || Build.MANUFACTURER.contains("Genymotion")

        bluetoothController = if (isEmulator) {
            MockBluetoothController()
        } else {
            AndroidBluetoothController(applicationContext)
        }

        val database = ChatDatabase.getDatabase(applicationContext)
        repository = DataRepository(database.messageDao(), bluetoothController)

        enableEdgeToEdge()
        
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
        bluetoothController.release()
    }
}
