package com.example.bluetoothchattingsystem

import android.app.Application
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.bluetoothchattingsystem.data.DataRepository
import com.example.bluetoothchattingsystem.data.bluetooth.AndroidBluetoothController
import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothController
import com.example.bluetoothchattingsystem.data.bluetooth.MockBluetoothController
import com.example.bluetoothchattingsystem.data.local.ChatDatabase
import com.example.bluetoothchattingsystem.data.update.UpdateCheckWorker
import java.util.concurrent.TimeUnit

class BChatApplication : Application() {

    lateinit var bluetoothController: BluetoothController
    lateinit var repository: DataRepository

    override fun onCreate() {
        super.onCreate()

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
        repository = DataRepository(applicationContext, database.messageDao(), bluetoothController)

        schedulePeriodicUpdateCheck()
    }

    private fun schedulePeriodicUpdateCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val updateWorkRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "bchat_periodic_update_check",
            ExistingPeriodicWorkPolicy.KEEP,
            updateWorkRequest
        )
    }
}
