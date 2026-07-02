package com.example.bluetoothchattingsystem.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context
) : BluetoothController {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>> = _scannedDevices.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BluetoothDeviceDomain?>(null)
    override val connectedDevice: StateFlow<BluetoothDeviceDomain?> = _connectedDevice.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<String>()
    override val incomingMessages: Flow<String> = _incomingMessages.asSharedFlow()

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled == true)
    override val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID

    private var acceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var transferThread: MessageTransferThread? = null

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let { dev ->
                        try {
                            val name = dev.name
                            val address = dev.address
                            val exists = _scannedDevices.value.any { it.address == address }
                            if (!exists) {
                                val domainDevice = BluetoothDeviceDomain(
                                    name = name ?: "Unknown Device",
                                    address = address,
                                    connectionState = ConnectionState.DISCONNECTED
                                )
                                _scannedDevices.update { it + domainDevice }
                            }
                        } catch (e: SecurityException) {
                            Log.e("BluetoothController", "Security exception getting device properties", e)
                        }
                    }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    _isBluetoothEnabled.value = (state == BluetoothAdapter.STATE_ON)
                }
            }
        }
    }

    init {
        context.registerReceiver(
            bluetoothReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            }
        )
        startServer()
    }

    override fun startDiscovery() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        _scannedDevices.value = emptyList()
        startServer() // Ensure server is started once permissions are granted
        try {
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            bluetoothAdapter.startDiscovery()
        } catch (e: SecurityException) {
            Log.e("BluetoothController", "Missing scan permissions", e)
        }
    }

    override fun stopDiscovery() {
        try {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
        } catch (e: SecurityException) {
            Log.e("BluetoothController", "Missing cancel discovery permissions", e)
        }
    }

    override fun connect(device: BluetoothDeviceDomain) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        
        try {
            val remoteDevice = bluetoothAdapter.getRemoteDevice(device.address)
            
            // Set connecting state
            _connectedDevice.value = device.copy(connectionState = ConnectionState.CONNECTING)
            
            // Cancel discovery to free resources
            stopDiscovery()

            connectThread = ConnectThread(remoteDevice).apply {
                start()
            }
        } catch (e: IllegalArgumentException) {
            Log.e("BluetoothController", "Invalid MAC address", e)
            _connectedDevice.value = null
        }
    }

    override fun disconnect() {
        closeConnection()
        startServer() // Restart listening server
    }

    override suspend fun sendMessage(message: String): Boolean {
        val currentTransferThread = transferThread ?: return false
        val currentConnected = _connectedDevice.value ?: return false
        if (currentConnected.connectionState != ConnectionState.CONNECTED) return false
        
        return try {
            currentTransferThread.write(message.toByteArray(Charsets.UTF_8))
            true
        } catch (e: IOException) {
            Log.e("BluetoothController", "Failed to write message", e)
            closeConnection()
            false
        }
    }

    override fun setBluetoothEnabled(enabled: Boolean) {
        // Since enabling/disabling programmatically is deprecated on newer APIs,
        // we log or try, but fallback is to alert user. On mock layer it works instantly.
        try {
            if (enabled) {
                bluetoothAdapter?.enable()
            } else {
                bluetoothAdapter?.disable()
            }
        } catch (e: SecurityException) {
            Log.e("BluetoothController", "Cannot enable/disable Bluetooth directly", e)
        }
    }

    override fun release() {
        try {
            context.unregisterReceiver(bluetoothReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver already unregistered
        }
        closeConnection()
    }

    fun startServer() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        if (acceptThread == null) {
            try {
                acceptThread = AcceptThread().apply {
                    start()
                }
            } catch (e: SecurityException) {
                Log.e("BluetoothController", "Security exception creating/starting AcceptThread", e)
                acceptThread = null
            }
        }
    }

    private fun closeConnection() {
        connectThread?.cancel()
        connectThread = null
        
        acceptThread?.cancel()
        acceptThread = null
        
        transferThread?.cancel()
        transferThread = null

        _connectedDevice.value = null
    }

    private fun handleSuccessfulConnection(socket: BluetoothSocket) {
        val deviceName = try { socket.remoteDevice.name } catch (e: SecurityException) { "Connected Device" }
        val deviceAddress = socket.remoteDevice.address
        val domainDevice = BluetoothDeviceDomain(
            name = deviceName,
            address = deviceAddress,
            connectionState = ConnectionState.CONNECTED
        )
        _connectedDevice.value = domainDevice

        transferThread = MessageTransferThread(socket).apply {
            start()
        }
    }

    // Server socket thread
    private inner class AcceptThread : Thread() {
        private val serverSocket: BluetoothServerSocket? = try {
            bluetoothAdapter?.listenUsingRfcommWithServiceRecord("AetherChat", uuid)
        } catch (e: IOException) {
            Log.e("BluetoothController", "Accept socket failed to create", e)
            null
        } catch (e: SecurityException) {
            Log.e("BluetoothController", "Security exception creating server socket", e)
            null
        }

        override fun run() {
            if (serverSocket == null) {
                Log.w("BluetoothController", "AcceptThread terminating because serverSocket is null")
                synchronized(this@AndroidBluetoothController) {
                    if (acceptThread == this) {
                        acceptThread = null
                    }
                }
                return
            }
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    serverSocket.accept()
                } catch (e: IOException) {
                    Log.e("BluetoothController", "Socket accept failed or closed", e)
                    shouldLoop = false
                    null
                }

                socket?.let {
                    synchronized(this@AndroidBluetoothController) {
                        if (_connectedDevice.value?.connectionState != ConnectionState.CONNECTED) {
                            handleSuccessfulConnection(it)
                            shouldLoop = false
                        } else {
                            try {
                                it.close()
                            } catch (e: IOException) {
                                Log.e("BluetoothController", "Could not close redundant socket", e)
                            }
                        }
                    }
                }
            }
            synchronized(this@AndroidBluetoothController) {
                if (acceptThread == this) {
                    acceptThread = null
                }
            }
        }

        fun cancel() {
            try {
                serverSocket?.close()
            } catch (e: IOException) {
                Log.e("BluetoothController", "Could not close server socket", e)
            }
        }
    }

    // Client socket connection thread
    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private val socket: BluetoothSocket? = try {
            device.createRfcommSocketToServiceRecord(uuid)
        } catch (e: IOException) {
            Log.e("BluetoothController", "RFCOMM socket creation failed", e)
            null
        } catch (e: SecurityException) {
            Log.e("BluetoothController", "SecurityException creating client socket", e)
            null
        }

        override fun run() {
            if (socket == null) {
                _connectedDevice.value = null
                return
            }

            try {
                socket.connect()
                synchronized(this@AndroidBluetoothController) {
                    handleSuccessfulConnection(socket)
                }
            } catch (e: IOException) {
                Log.e("BluetoothController", "Socket connect failed", e)
                try {
                    socket.close()
                } catch (closeException: IOException) {
                    Log.e("BluetoothController", "Could not close client socket after fail", closeException)
                }
                _connectedDevice.value = null
                val name = try { device.name } catch (se: SecurityException) { null } ?: "Device"
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(
                        context,
                        "Unable to connect to $name. Ensure it is discoverable.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: SecurityException) {
                Log.e("BluetoothController", "SecurityException during client connect", e)
                try {
                    socket.close()
                } catch (closeException: IOException) {
                    Log.e("BluetoothController", "Could not close client socket after fail", closeException)
                }
                _connectedDevice.value = null
                val name = try { device.name } catch (se: SecurityException) { null } ?: "Device"
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(
                        context,
                        "Unable to connect to $name. Ensure it is discoverable.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                Log.e("BluetoothController", "Could not close client socket", e)
            }
        }
    }

    // Transmit data thread
    private inner class MessageTransferThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream = socket.inputStream
        private val outputStream = socket.outputStream

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int

            while (true) {
                bytes = try {
                    inputStream.read(buffer)
                } catch (e: IOException) {
                    Log.e("BluetoothController", "Input stream disconnected", e)
                    // Inform connection dropped
                    _connectedDevice.value = _connectedDevice.value?.copy(connectionState = ConnectionState.DISCONNECTED)
                    break
                }

                val message = String(buffer, 0, bytes, Charsets.UTF_8)
                scope.launch {
                    _incomingMessages.emit(message)
                }
            }
        }

        fun write(bytes: ByteArray) {
            try {
                outputStream.write(bytes)
            } catch (e: IOException) {
                Log.e("BluetoothController", "Error writing to socket", e)
                throw e
            }
        }

        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) {
                Log.e("BluetoothController", "Could not close transfer socket", e)
            }
        }
    }
}
