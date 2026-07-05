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
import kotlinx.coroutines.delay

class DataRepository(
    private val context: Context,
    private val messageDao: MessageDao,
    val bluetoothController: BluetoothController
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val channelId = "aether_chat_channel"

    @Volatile
    var activeChatAddress: String? = null

    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>> = bluetoothController.scannedDevices
    val connectedDevice: StateFlow<BluetoothDeviceDomain?> = bluetoothController.connectedDevice
    val isBluetoothEnabled: StateFlow<Boolean> = bluetoothController.isBluetoothEnabled
    val localDeviceName: StateFlow<String> = bluetoothController.localDeviceName

    val lastMessages: Flow<List<MessageEntity>> = messageDao.getAllLastMessages()

    private val prefs = context.getSharedPreferences("bchat_prefs", Context.MODE_PRIVATE)

    private val preferenceListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == "settings_auto_scan") {
            val autoScan = sharedPreferences.getBoolean(key, false)
            if (autoScan) {
                Log.d("DataRepository", "Auto Scan enabled: starting BLE scanning.")
                startDiscovery()
            } else {
                Log.d("DataRepository", "Auto Scan disabled: stopping BLE scanning.")
                stopDiscovery()
            }
        }
    }

    init {
        createNotificationChannel()
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)

        // Start scanning immediately if auto scan is enabled at start
        if (prefs.getBoolean("settings_auto_scan", false)) {
            startDiscovery()
        }
        
        // Collect incoming messages from Bluetooth controller and store them in Room
        repositoryScope.launch {
            bluetoothController.incomingMessages.collect { rawMessage ->
                val messageText = rawMessage.trim()
                if (messageText.isBlank()) return@collect
                val currentDevice = connectedDevice.value
                if (currentDevice != null) {
                    when {
                        messageText.startsWith("__MESSAGE_ACK__|") -> {
                            val parts = messageText.split("|")
                            val timestamp = parts.getOrNull(1)?.toLongOrNull()
                            if (timestamp != null) {
                                messageDao.markMessageAsDelivered(timestamp)
                            }
                        }
                        messageText.startsWith("__IMAGE_TRANSFER__|") -> {
                            val parts = messageText.split("|")
                            val timestamp = parts.getOrNull(1)?.toLongOrNull() ?: System.currentTimeMillis()
                            
                            val isNewFormat = parts.size >= 5
                            val senderName = if (isNewFormat) parts.getOrNull(2) ?: "Unknown" else currentDevice.name ?: "Unknown"
                            val senderAvatarId = if (isNewFormat) parts.getOrNull(3)?.toIntOrNull() ?: 1 else currentDevice.avatarId
                            val base64Data = if (isNewFormat) parts.getOrNull(4) else parts.getOrNull(2)
                            
                            if (base64Data != null) {
                                val filename = "bchat_img_${timestamp}.jpg"
                                val localFile = java.io.File(context.cacheDir, filename)
                                try {
                                    val bytes = android.util.Base64.decode(base64Data, android.util.Base64.NO_WRAP)
                                    localFile.writeBytes(bytes)
                                    
                                    val localImagePath = "image:file://${localFile.absolutePath}"
                                    
                                    // 1. Update database profile mapping
                                    messageDao.updateSenderProfile(currentDevice.address, senderName, senderAvatarId)
                                    
                                    // 2. Update active connection profile state
                                    bluetoothController.updateConnectedDeviceProfile(currentDevice.address, senderName, senderAvatarId)
                                    
                                    val isRead = (activeChatAddress == currentDevice.address)
                                    val message = MessageEntity(
                                        senderAddress = currentDevice.address,
                                        senderName = senderName,
                                        messageText = localImagePath,
                                        timestamp = timestamp,
                                        isSent = false,
                                        isRead = isRead,
                                        avatarId = senderAvatarId
                                    )
                                    messageDao.insertMessage(message)

                                    val ackPacket = "__MESSAGE_ACK__|$timestamp"
                                    bluetoothController.sendMessage(ackPacket)

                                    if (prefs.getBoolean("settings_sound_alerts", true)) {
                                        playSoundAlert()
                                    }

                                    if (prefs.getBoolean("settings_push_notifications", true)) {
                                        showNotification(message.senderName, "Sent an image attachment.")
                                    }
                                } catch (e: Exception) {
                                    Log.e("DataRepository", "Failed to save received image", e)
                                }
                            }
                        }
                        messageText.startsWith("__MESSAGE_DELETE__|") -> {
                            val parts = messageText.split("|")
                            val timestamp = parts.getOrNull(1)?.toLongOrNull()
                            if (timestamp != null) {
                                messageDao.deleteMessageByTimestamp(timestamp)
                            }
                        }
                        messageText.startsWith("__MESSAGE_EDIT__|") -> {
                            val parts = messageText.split("|")
                            val timestamp = parts.getOrNull(1)?.toLongOrNull()
                            val newText = parts.getOrNull(2)
                            if (timestamp != null && newText != null) {
                                messageDao.updateMessageByTimestamp(timestamp, newText)
                            }
                        }
                        messageText.startsWith("__MESSAGE_PAYLOAD__|") -> {
                            val parts = messageText.split("|")
                            val timestamp = parts.getOrNull(1)?.toLongOrNull() ?: System.currentTimeMillis()
                            
                            val isNewFormat = parts.size >= 5
                            val senderName = if (isNewFormat) parts.getOrNull(2) ?: "Unknown" else currentDevice.name ?: "Unknown"
                            val senderAvatarId = if (isNewFormat) parts.getOrNull(3)?.toIntOrNull() ?: 1 else currentDevice.avatarId
                            val parsedText = if (isNewFormat) parts.getOrNull(4) ?: "" else parts.getOrNull(2) ?: ""
                            val textContent = if (parsedText.isBlank()) messageText else parsedText
                            
                            // 1. Update database profile mapping
                            messageDao.updateSenderProfile(currentDevice.address, senderName, senderAvatarId)
                            
                            // 2. Update active connection profile state
                            bluetoothController.updateConnectedDeviceProfile(currentDevice.address, senderName, senderAvatarId)
                            
                            val isRead = (activeChatAddress == currentDevice.address)
                            val message = MessageEntity(
                                senderAddress = currentDevice.address,
                                senderName = senderName,
                                messageText = textContent,
                                timestamp = timestamp,
                                isSent = false,
                                isRead = isRead,
                                avatarId = senderAvatarId
                            )
                            messageDao.insertMessage(message)

                            // Send delivery acknowledgment back to peer
                            val ackPacket = "__MESSAGE_ACK__|$timestamp"
                            bluetoothController.sendMessage(ackPacket)

                            // 1. Play sound alert if enabled
                            if (prefs.getBoolean("settings_sound_alerts", true)) {
                                playSoundAlert()
                            }

                            // 2. Show notification popup if enabled
                            if (prefs.getBoolean("settings_push_notifications", true)) {
                                showNotification(message.senderName, textContent)
                            }
                        }
                        else -> {
                            // Fallback for legacy messages
                            val isRead = (activeChatAddress == currentDevice.address)
                            val message = MessageEntity(
                                senderAddress = currentDevice.address,
                                senderName = currentDevice.name ?: "Unknown",
                                messageText = messageText,
                                timestamp = System.currentTimeMillis(),
                                isSent = false,
                                isRead = isRead,
                                avatarId = currentDevice.avatarId
                            )
                            messageDao.insertMessage(message)

                            if (prefs.getBoolean("settings_sound_alerts", true)) {
                                playSoundAlert()
                            }

                            if (prefs.getBoolean("settings_push_notifications", true)) {
                                showNotification(message.senderName, messageText)
                            }
                        }
                    }
                }
            }
        }

        // Collect connection state changes to notify and update database names
        repositoryScope.launch {
            var lastConnectedAddress: String? = null
            bluetoothController.connectedDevice.collect { device ->
                if (device != null && device.connectionState == ConnectionState.CONNECTED) {
                    // Update database profiles on any connection emission to catch live name/avatar changes
                    if (!device.name.isNullOrBlank()) {
                        messageDao.updateSenderProfile(device.address, device.name, device.avatarId)
                    }
                    
                    if (lastConnectedAddress != device.address) {
                        lastConnectedAddress = device.address
                        showConnectionNotification(device.name ?: "Unknown Device")
                        
                        // Redundantly broadcast our profile sync payload to the peer upon connection establishment
                        val localName = prefs.getString("settings_display_name", "") ?: ""
                        val cleanName = if (localName.isBlank()) bluetoothController.localDeviceName.value else localName
                        val localAvatarId = prefs.getInt("profile_avatar_id", 1)
                        val profilePacket = "__PROFILE_SYNC__|$cleanName|$localAvatarId"
                        repositoryScope.launch {
                            delay(1000) // Give the connection stability time
                            bluetoothController.sendMessage(profilePacket)
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

        val largeIconBitmap = android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.logoo)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIconBitmap)
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
        val timestamp = System.currentTimeMillis()
        
        val prefs = context.getSharedPreferences("bchat_prefs", Context.MODE_PRIVATE)
        val localName = prefs.getString("settings_display_name", "") ?: ""
        val cleanName = if (localName.isBlank()) bluetoothController.localDeviceName.value else localName
        val localAvatarId = prefs.getInt("profile_avatar_id", 1)
        
        val payload = "__MESSAGE_PAYLOAD__|$timestamp|$cleanName|$localAvatarId|$text"
        val success = bluetoothController.sendMessage(payload)
        if (success) {
            val message = MessageEntity(
                senderAddress = currentDevice.address,
                senderName = currentDevice.name ?: "Unknown",
                messageText = text,
                timestamp = timestamp,
                isSent = true,
                avatarId = currentDevice.avatarId
            )
            messageDao.insertMessage(message)
        }
        return success
    }

    suspend fun deleteChatMessage(message: MessageEntity) {
        // 1. Delete locally
        messageDao.deleteMessageById(message.id)
        
        // 2. Broadcast delete packet to peer
        val deletePacket = "__MESSAGE_DELETE__|${message.timestamp}"
        bluetoothController.sendMessage(deletePacket)
    }

    suspend fun editChatMessage(message: MessageEntity, newText: String) {
        // 1. Edit locally
        messageDao.updateMessageText(message.id, newText)
        
        // 2. Broadcast edit packet to peer
        val editPacket = "__MESSAGE_EDIT__|${message.timestamp}|$newText"
        bluetoothController.sendMessage(editPacket)
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

        val largeIconBitmap = android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.logoo)
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIconBitmap)
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

    private fun playSoundAlert() {
        try {
            val notificationUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = android.media.RingtoneManager.getRingtone(context, notificationUri)
            ringtone?.play()
        } catch (e: Exception) {
            Log.e("DataRepository", "Failed to play notification sound", e)
        }
    }

    suspend fun sendImageChatMessage(base64Data: String): Boolean {
        val currentDevice = connectedDevice.value ?: return false
        val timestamp = System.currentTimeMillis()
        
        val filename = "bchat_img_${timestamp}.jpg"
        val localFile = java.io.File(context.cacheDir, filename)
        try {
            val bytes = android.util.Base64.decode(base64Data, android.util.Base64.NO_WRAP)
            localFile.writeBytes(bytes)
        } catch (e: Exception) {
            Log.e("DataRepository", "Failed to save outgoing image file", e)
            return false
        }
        
        val localImagePath = "image:file://${localFile.absolutePath}"
        
        val prefs = context.getSharedPreferences("bchat_prefs", Context.MODE_PRIVATE)
        val localName = prefs.getString("settings_display_name", "") ?: ""
        val cleanName = if (localName.isBlank()) bluetoothController.localDeviceName.value else localName
        val localAvatarId = prefs.getInt("profile_avatar_id", 1)
        
        val payload = "__IMAGE_TRANSFER__|$timestamp|$cleanName|$localAvatarId|$base64Data"
        val success = bluetoothController.sendMessage(payload)
        
        if (success) {
            val message = MessageEntity(
                senderAddress = currentDevice.address,
                senderName = currentDevice.name ?: "Unknown",
                messageText = localImagePath,
                timestamp = timestamp,
                isSent = true,
                avatarId = currentDevice.avatarId,
                isDelivered = false
            )
            messageDao.insertMessage(message)
        }
        return success
    }
}
