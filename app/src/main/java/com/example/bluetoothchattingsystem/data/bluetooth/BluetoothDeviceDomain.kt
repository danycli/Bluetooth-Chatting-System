package com.example.bluetoothchattingsystem.data.bluetooth

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

data class BluetoothDeviceDomain(
    val name: String?,
    val address: String,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val isAvailable: Boolean = true,
    val rssi: Int = -70,
    val avatarId: Int = 1
)
