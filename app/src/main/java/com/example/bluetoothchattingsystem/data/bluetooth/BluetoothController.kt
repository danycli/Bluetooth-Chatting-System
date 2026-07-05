package com.example.bluetoothchattingsystem.data.bluetooth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
    val connectedDevice: StateFlow<BluetoothDeviceDomain?>
    val incomingMessages: Flow<String>
    val isBluetoothEnabled: StateFlow<Boolean>
    val localDeviceName: StateFlow<String>
    var onRequestBluetoothEnable: (() -> Unit)?

    fun changeLocalDeviceName(name: String): Boolean

    fun startDiscovery()
    fun stopDiscovery()
    
    fun connect(device: BluetoothDeviceDomain)
    fun disconnect()
    
    suspend fun sendMessage(message: String): Boolean
    fun setBluetoothEnabled(enabled: Boolean)
    fun release()
}
