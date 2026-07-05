package com.example.bluetoothchattingsystem.data.bluetooth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MockBluetoothController : BluetoothController {

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>> = _scannedDevices.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothDeviceDomain?>(null)
    override val connectedDevice: StateFlow<BluetoothDeviceDomain?> = _connectedDevice.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>()
    override val incomingMessages: Flow<String> = _incomingMessages.asSharedFlow()

    private val _isBluetoothEnabled = MutableStateFlow(true)
    override val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _localDeviceName = MutableStateFlow("Mock B-Chat Node")
    override val localDeviceName: StateFlow<String> = _localDeviceName.asStateFlow()

    override var onRequestBluetoothEnable: (() -> Unit)? = null

    override fun changeLocalDeviceName(name: String): Boolean {
        _localDeviceName.value = name
        return true
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private var scanJob: Job? = null
    private var connectionJob: Job? = null

    private val mockResponses = listOf(
        "Let's test file transfer next.",
        "Are you close by? Signal shows 4 bars.",
        "Got it. Bringing the supplies now.",
        "The connection is steady. 0% packet drop.",
        "Aether network is active!"
    )

    override fun startDiscovery() {
        if (!_isBluetoothEnabled.value) return
        
        scanJob?.cancel()
        _scannedDevices.value = emptyList()
        
        scanJob = scope.launch {
            delay(500)
            _scannedDevices.value = listOf(
                BluetoothDeviceDomain("Dave's Kindle", "AA:BB:CC:11:22:33", ConnectionState.DISCONNECTED),
                BluetoothDeviceDomain("Sarah's Pixel", "AA:BB:CC:44:55:66", ConnectionState.DISCONNECTED),
                BluetoothDeviceDomain("OnePlus 11", "AA:BB:CC:77:88:99", ConnectionState.DISCONNECTED)
            )
        }
    }

    override fun stopDiscovery() {
        scanJob?.cancel()
    }

    override fun connect(device: BluetoothDeviceDomain) {
        if (!_isBluetoothEnabled.value) return
        
        connectionJob?.cancel()
        _connectedDevice.value = device.copy(connectionState = ConnectionState.CONNECTING)
        
        connectionJob = scope.launch {
            delay(1500) // connection handshake simulation
            
            val connected = device.copy(connectionState = ConnectionState.CONNECTED)
            _connectedDevice.value = connected
            
            // Auto response after initial connection
            delay(1000)
            _incomingMessages.emit("Hello! I am connected via Aether. How is the signal?")
        }
    }

    override fun disconnect() {
        connectionJob?.cancel()
        _connectedDevice.value = null
    }

    override suspend fun sendMessage(message: String): Boolean {
        if (!_isBluetoothEnabled.value || _connectedDevice.value?.connectionState != ConnectionState.CONNECTED) {
            return false
        }
        
        // Simulate remote peer replying after a delay
        scope.launch {
            delay(1500)
            val reply = mockResponses.random()
            _incomingMessages.emit(reply)
        }
        return true
    }

    override fun setBluetoothEnabled(enabled: Boolean) {
        _isBluetoothEnabled.value = enabled
        if (!enabled) {
            disconnect()
            _scannedDevices.value = emptyList()
        }
    }

    override fun release() {
        scanJob?.cancel()
        connectionJob?.cancel()
    }
}
