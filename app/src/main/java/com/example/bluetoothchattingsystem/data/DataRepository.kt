package com.example.bluetoothchattingsystem.data

import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothController
import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothDeviceDomain
import com.example.bluetoothchattingsystem.data.local.MessageDao
import com.example.bluetoothchattingsystem.data.local.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DataRepository(
    private val messageDao: MessageDao,
    val bluetoothController: BluetoothController
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>> = bluetoothController.scannedDevices
    val connectedDevice: StateFlow<BluetoothDeviceDomain?> = bluetoothController.connectedDevice
    val isBluetoothEnabled: StateFlow<Boolean> = bluetoothController.isBluetoothEnabled

    val lastMessages: Flow<List<MessageEntity>> = messageDao.getAllLastMessages()

    init {
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
                        isSent = false
                    )
                    messageDao.insertMessage(message)
                }
            }
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

    fun setBluetoothEnabled(enabled: Boolean) {
        bluetoothController.setBluetoothEnabled(enabled)
    }

    suspend fun deleteConversation(address: String) {
        messageDao.deleteConversation(address)
    }

    suspend fun wipeLocalVault() {
        messageDao.clearAllMessages()
    }
}
