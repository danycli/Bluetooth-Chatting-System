package com.example.bluetoothchattingsystem.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bluetoothchattingsystem.R
import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothController
import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothDeviceDomain
import com.example.bluetoothchattingsystem.data.bluetooth.ConnectionState
import com.example.bluetoothchattingsystem.data.local.MessageDao
import com.example.bluetoothchattingsystem.data.local.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DataRepository(
    private val context: Context,
    private val messageDao: MessageDao,
    val bluetoothController: BluetoothController
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val channelId = "aether_chat_channel"

    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>> = bluetoothController.scannedDevices
    val connectedDevice: StateFlow<BluetoothDeviceDomain?> = bluetoothController.connectedDevice
    val isBluetoothEnabled: StateFlow<Boolean> = bluetoothController.isBluetoothEnabled
    val localDeviceName: StateFlow<String> = bluetoothController.localDeviceName

    val lastMessages: Flow<List<MessageEntity>> = messageDao.getAllLastMessages()

    init {
        createNotificationChannel()
        
        // Collect incoming messages from Bluetooth controller and store them in Room
        repositoryScope.launch {
            bluetoothController.incomingMessages.collectLatest { messageText ->
                val currentDevice = connectedDevice.value
                if (currentDevice != null) {
                    val message = MessageEntity(
                        senderAddress = currentDevice.address,
                        senderName = currentDevice.name ?: "Unknown",
                        messageText = messageText,
                        timestamp = System.currentTimeMillis(),
                        isSent = false,
                        isRead = false // mark incoming messages as unread
                    )
                    messageDao.insertMessage(message)
                    showNotification(message.senderName, messageText)
                }
            }
        }

        // Collect connection state changes to notify and update database names
        repositoryScope.launch {
            var lastConnectedAddress: String? = null
            bluetoothController.connectedDevice.collect { device ->
                if (device != null && device.connectionState == ConnectionState.CONNECTED) {
                    if (lastConnectedAddress != device.address) {
                        lastConnectedAddress = device.address
                        
                        // 1. Show notification
                        showConnectionNotification(device.name ?: "Unknown Device")
                        
                        // 2. Update DB names
                        if (!device.name.isNullOrBlank()) {
                            messageDao.updateSenderName(device.address, device.name)
                        }
                    }
                } else if (device == null || device.connectionState == ConnectionState.DISCONNECTED) {
                    lastConnectedAddress = null
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Aether Chat Messages"
            val descriptionText = "Notifications for incoming offline chat messages"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(senderName: String, messageText: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create intent to open MainActivity when clicking notification
        val intent = Intent(context, com.example.bluetoothchattingsystem.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logoo)
            .setContentTitle(senderName)
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Unique ID per timestamp ensures notifications stack in system drawer
        val notificationId = System.currentTimeMillis().toInt()
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.e("DataRepository", "Missing notification posting permission", e)
        }
    }

    fun getMessagesForDevice(address: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForDevice(address)
    }

    fun startDiscovery() {
        bluetoothController.startDiscovery()
    }

    fun stopDiscovery() {
        bluetoothController.stopDiscovery()
    }

    fun connect(device: BluetoothDeviceDomain) {
        bluetoothController.connect(device)
    }

    fun disconnect() {
        bluetoothController.disconnect()
    }

    suspend fun sendChatMessage(text: String): Boolean {
        val currentDevice = connectedDevice.value ?: return false
        val success = bluetoothController.sendMessage(text)
        if (success) {
            val message = MessageEntity(
                senderAddress = currentDevice.address,
                senderName = currentDevice.name ?: "Unknown",
                messageText = text,
                timestamp = System.currentTimeMillis(),
                isSent = true
            )
            messageDao.insertMessage(message)
        }
        return success
    }

    fun changeLocalDeviceName(name: String): Boolean {
        return bluetoothController.changeLocalDeviceName(name)
    }

    fun requestBluetoothEnable() {
        bluetoothController.onRequestBluetoothEnable?.invoke()
    }

    fun setBluetoothEnabled(enabled: Boolean) {
        bluetoothController.setBluetoothEnabled(enabled)
    }

    suspend fun deleteConversation(address: String) {
        messageDao.deleteConversation(address)
    }

    suspend fun deleteMessageById(messageId: Int): Int {
        return messageDao.deleteMessageById(messageId)
    }

    suspend fun updateMessageText(messageId: Int, newText: String): Int {
        return messageDao.updateMessageText(messageId, newText)
    }

    suspend fun wipeLocalVault() {
        messageDao.clearAllMessages()
    }

    suspend fun markConversationAsRead(senderAddress: String) {
        messageDao.markConversationAsRead(senderAddress)
    }

    private fun showConnectionNotification(deviceName: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, com.example.bluetoothchattingsystem.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logoo)
            .setContentTitle("B-Chat Node Connected")
            .setContentText("Successfully connected to peer: $deviceName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationId = 10002
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.e("DataRepository", "Missing notification posting permission", e)
        }
    }
}
