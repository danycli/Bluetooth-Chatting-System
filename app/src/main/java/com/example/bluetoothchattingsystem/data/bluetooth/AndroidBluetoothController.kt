package com.example.bluetoothchattingsystem.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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

    private val _localDeviceName = MutableStateFlow(
        context.getSharedPreferences("bchat_prefs", Context.MODE_PRIVATE)
            .getString("settings_display_name", null) ?: run {
                val adapterName = try {
                    bluetoothAdapter?.name
                } catch (e: SecurityException) {
                    null
                }
                if (adapterName != null && !adapterName.contains("|")) {
                    adapterName
                } else if (adapterName != null && adapterName.contains("|")) {
                    adapterName.substringBefore("|")
                } else {
                    "B-Chat Device"
                }
            }
    )
    override val localDeviceName: StateFlow<String> = _localDeviceName.asStateFlow()

    override var onRequestBluetoothEnable: (() -> Unit)? = null

    private val scope = CoroutineScope(Dispatchers.IO)
    private var sweeperJob: Job? = null
    private var isScanning = false

    // BLE Service and Characteristic UUIDs (SPP equivalent services)
    private val serviceUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val writeCharUuid = UUID.fromString("00001102-0000-1000-8000-00805F9B34FB")
    private val notifyCharUuid = UUID.fromString("00001103-0000-1000-8000-00805F9B34FB")
    private val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // BLE Components
    private var gattServer: BluetoothGattServer? = null
    private var activeGattClient: BluetoothGatt? = null
    private var activeGattDevice: BluetoothDevice? = null

    // Trackers for signal analysis and automatic sweeps
    private val lastSeenTimestamps = mutableMapOf<String, Long>()

    // Map to reassemble chunked BLE GATT transmissions: transferId -> array of chunks
    private val incomingTransfers = mutableMapOf<String, Array<String?>>()

    @Volatile
    private var currentWriteLimit = 20 // Default to 20 bytes (MTU 23) until negotiated

    private fun handleChunkedPacket(deviceAddress: String, rawText: String): Boolean {
        return when {
            rawText.startsWith("__CHUNK_START__|") -> {
                val parts = rawText.split("|")
                val transferId = parts.getOrNull(1)
                val totalChunks = parts.getOrNull(2)?.toIntOrNull()
                if (transferId != null && totalChunks != null) {
                    incomingTransfers[transferId] = arrayOfNulls(totalChunks)
                }
                true
            }
            rawText.startsWith("__CHUNK_DATA__|") -> {
                val parts = rawText.split("|", limit = 4)
                val transferId = parts.getOrNull(1)
                val chunkIndex = parts.getOrNull(2)?.toIntOrNull()
                val chunkData = parts.getOrNull(3)
                if (transferId != null && chunkIndex != null && chunkData != null) {
                    incomingTransfers[transferId]?.let { array ->
                        if (chunkIndex in array.indices) {
                            array[chunkIndex] = chunkData
                        }
                    }
                }
                true
            }
            rawText.startsWith("__CHUNK_END__|") -> {
                val parts = rawText.split("|")
                val transferId = parts.getOrNull(1)
                if (transferId != null) {
                    val array = incomingTransfers[transferId]
                    if (array != null && array.all { it != null }) {
                        val fullMessage = array.filterNotNull().joinToString("")
                        scope.launch {
                            val consumed = handleIncomingPacket(deviceAddress, fullMessage)
                            if (!consumed) {
                                _incomingMessages.emit(fullMessage)
                            }
                        }
                    }
                    incomingTransfers.remove(transferId)
                }
                true
            }
            else -> false
        }
    }

    // CALLBACKS DECLARATION - Keep them defined BEFORE any init block to avoid null initialization failures
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d("BleController", "BLE Advertising started successfully.")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.e("BleController", "BLE Advertising failed with error code: $errorCode")
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            val scanRecord = result.scanRecord
            val rawName = scanRecord?.deviceName ?: device.name ?: "Unnamed Device"
            val cleanName = if (rawName.contains("|")) rawName.split("|").firstOrNull() ?: "B-Chat Node" else rawName
            val avatarId = if (rawName.contains("|")) rawName.split("|").getOrNull(1)?.toIntOrNull() ?: 1 else 1
            val rssi = result.rssi
            val address = device.address.uppercase()

            val now = System.currentTimeMillis()
            lastSeenTimestamps[address] = now

            val existingIndex = _scannedDevices.value.indexOfFirst { it.address == address }
            val updatedDevice = BluetoothDeviceDomain(
                name = cleanName,
                address = address,
                connectionState = if (_connectedDevice.value?.address == address) _connectedDevice.value!!.connectionState else ConnectionState.DISCONNECTED,
                rssi = rssi,
                avatarId = avatarId
            )

            _scannedDevices.update { list ->
                val newList = list.toMutableList()
                if (existingIndex >= 0) {
                    newList[existingIndex] = updatedDevice
                } else {
                    newList.add(updatedDevice)
                }
                // Sort by RSSI strength (strongest signal first)
                newList.sortByDescending { it.rssi }
                newList
            }
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            val upperAddress = device.address.uppercase()
            Log.d("GattServer", "onConnectionStateChange: device=$upperAddress, newState=$newState, status=$status")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                val scannedInfo = _scannedDevices.value.find { it.address.equals(upperAddress, ignoreCase = true) }
                val domainDevice = BluetoothDeviceDomain(
                    name = scannedInfo?.name ?: device.name?.split("|")?.firstOrNull() ?: "B-Chat Node",
                    address = upperAddress,
                    connectionState = ConnectionState.CONNECTED,
                    avatarId = scannedInfo?.avatarId ?: device.name?.split("|")?.getOrNull(1)?.toIntOrNull() ?: 1
                )
                _connectedDevice.value = domainDevice
                activeGattDevice = device
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (_connectedDevice.value?.address?.equals(upperAddress, ignoreCase = true) == true) {
                    _connectedDevice.value = null
                    activeGattDevice = null
                }
            }
        }

        override fun onMtuChanged(device: BluetoothDevice, mtu: Int) {
            super.onMtuChanged(device, mtu)
            currentWriteLimit = (mtu - 3).coerceIn(20, 512)
            Log.d("GattServer", "Server MTU changed: $mtu for ${device.address}, writeLimit=$currentWriteLimit")
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            if (value != null) {
                val message = String(value, Charsets.UTF_8)
                scope.launch {
                    val consumedByChunking = handleChunkedPacket(device.address, message)
                    if (!consumedByChunking) {
                        val consumed = handleIncomingPacket(device.address, message)
                        if (!consumed) {
                            _incomingMessages.emit(message)
                        }
                    }
                }
            }
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }
            
            // Client registered for notifications. Send Server's profile sync payload immediately.
            val prefs = context.getSharedPreferences("bchat_prefs", Context.MODE_PRIVATE)
            val localName = _localDeviceName.value
            val localAvatarId = prefs.getInt("profile_avatar_id", 1)
            val profilePacket = "__PROFILE_SYNC__|$localName|$localAvatarId"
            scope.launch {
                delay(500)
                sendMessage(profilePacket)
            }
        }
    }

    private val gattClientCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val upperAddress = gatt.device.address.uppercase()
            Log.d("GattClient", "onConnectionStateChange: device=$upperAddress, newState=$newState, status=$status")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                activeGattClient = gatt
                Log.d("GattClient", "Connected. Requesting MTU of 517...")
                gatt.requestMtu(517)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                activeGattClient?.close()
                activeGattClient = null
                _connectedDevice.value = null
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                currentWriteLimit = (mtu - 3).coerceIn(20, 512)
                Log.d("BleController", "Client MTU negotiated: $mtu. Write limit=$currentWriteLimit")
            } else {
                Log.w("BleController", "Client MTU negotiation failed: status=$status")
            }
            Log.d("GattClient", "Discovering services...")
            gatt.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(serviceUuid)
                val notifyChar = service?.getCharacteristic(notifyCharUuid)
                if (notifyChar != null) {
                    gatt.setCharacteristicNotification(notifyChar, true)
                    val descriptor = notifyChar.getDescriptor(cccdUuid)
                    if (descriptor != null) {
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                    }
                }
 
                val upperAddress = gatt.device.address.uppercase()
                val scannedInfo = _scannedDevices.value.find { it.address.equals(upperAddress, ignoreCase = true) }
                val domainDevice = BluetoothDeviceDomain(
                    name = scannedInfo?.name ?: gatt.device.name?.split("|")?.firstOrNull() ?: "B-Chat Node",
                    address = upperAddress,
                    connectionState = ConnectionState.CONNECTED,
                    avatarId = scannedInfo?.avatarId ?: gatt.device.name?.split("|")?.getOrNull(1)?.toIntOrNull() ?: 1
                )
                _connectedDevice.value = domainDevice
                activeGattDevice = gatt.device
            } else {
                Log.e("GattClient", "Service discovery failed with status $status")
                disconnect()
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Client registered notifications. Send Client's profile sync payload immediately.
                val prefs = context.getSharedPreferences("bchat_prefs", Context.MODE_PRIVATE)
                val localName = _localDeviceName.value
                val localAvatarId = prefs.getInt("profile_avatar_id", 1)
                val profilePacket = "__PROFILE_SYNC__|$localName|$localAvatarId"
                scope.launch {
                    delay(500)
                    sendMessage(profilePacket)
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            val value = characteristic.value
            if (value != null) {
                val message = String(value, Charsets.UTF_8)
                scope.launch {
                    val consumedByChunking = handleChunkedPacket(gatt.device.address, message)
                    if (!consumedByChunking) {
                        val consumed = handleIncomingPacket(gatt.device.address, message)
                        if (!consumed) {
                            _incomingMessages.emit(message)
                        }
                    }
                }
            }
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    val isOn = (state == BluetoothAdapter.STATE_ON)
                    _isBluetoothEnabled.value = isOn
                    if (isOn) {
                        startServer()
                        startDiscovery()
                    } else {
                        stopDiscovery()
                        closeConnection()
                    }
                }
            }
        }
    }

    init {
        context.registerReceiver(
            bluetoothReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            }
        )
        // Automatically start GATT Server if Bluetooth is already enabled
        startServer()
    }

    override fun startDiscovery() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        _scannedDevices.value = emptyList()
        lastSeenTimestamps.clear()
        
        // Ensure server is active before advertising/scanning
        startServer() 

        startAdvertising()
        startScanning()
    }

    override fun stopDiscovery() {
        stopScanning()
        stopAdvertising()
    }

    override fun connect(device: BluetoothDeviceDomain) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        
        try {
            val upperAddress = device.address.uppercase()
            val remoteDevice = bluetoothAdapter.getRemoteDevice(upperAddress)
            
            // Set connecting state in domain model
            _connectedDevice.value = device.copy(address = upperAddress, connectionState = ConnectionState.CONNECTING)
            
            // Cancel discovery to free up BLE transceiver resources during connections
            stopDiscovery()

            activeGattClient = remoteDevice.connectGatt(context, false, gattClientCallback, BluetoothDevice.TRANSPORT_LE)
        } catch (e: Exception) {
            Log.e("BluetoothController", "BLE GATT client connection failed", e)
            _connectedDevice.value = null
        }
    }

    override fun disconnect() {
        closeConnection()
        startServer() // restart server listening services
        startDiscovery() // resume scanning and advertising automatically
    }

    override suspend fun sendMessage(message: String): Boolean {
        val limit = currentWriteLimit
        if (message.length <= limit) {
            return sendRawMessage(message)
        }

        val transferId = System.currentTimeMillis().toString()
        // Deduct envelope header overhead (approx 40 bytes) to fit within MTU limit
        val chunkLimit = (limit - 40).coerceAtLeast(10)
        val chunks = message.chunked(chunkLimit)
        val totalChunks = chunks.size

        // 1. Send START
        var success = sendRawMessage("__CHUNK_START__|$transferId|$totalChunks")
        if (!success) return false
        delay(60)

        // 2. Send DATA chunks
        for (i in chunks.indices) {
            success = sendRawMessage("__CHUNK_DATA__|$transferId|$i|${chunks[i]}")
            if (!success) return false
            delay(60)
        }

        // 3. Send END
        success = sendRawMessage("__CHUNK_END__|$transferId")
        return success
    }

    private suspend fun sendRawMessage(message: String): Boolean {
        val bytes = message.toByteArray(Charsets.UTF_8)

        // Case A: We are acting as a GATT Client (initiated connection)
        val client = activeGattClient
        if (client != null) {
            val service = client.getService(serviceUuid)
            val writeChar = service?.getCharacteristic(writeCharUuid)
            if (writeChar != null) {
                writeChar.value = bytes
                writeChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                val success = client.writeCharacteristic(writeChar)
                Log.d("BleController", "Client sendRawMessage write success=$success")
                return success
            }
        }

        // Case B: We are acting as a GATT Server (accepted connection)
        val server = gattServer
        val device = activeGattDevice
        if (server != null && device != null) {
            val service = server.getService(serviceUuid)
            val notifyChar = service?.getCharacteristic(notifyCharUuid)
            if (notifyChar != null) {
                notifyChar.value = bytes
                val success = server.notifyCharacteristicChanged(device, notifyChar, false)
                Log.d("BleController", "Server sendRawMessage notification success=$success")
                return success
            }
        }

        return false
    }

    override fun setBluetoothEnabled(enabled: Boolean) {
        try {
            if (enabled) {
                bluetoothAdapter?.enable()
            } else {
                bluetoothAdapter?.disable()
            }
        } catch (e: SecurityException) {
            Log.e("BluetoothController", "Cannot modify Bluetooth state directly", e)
        }
    }

    override fun changeLocalDeviceName(name: String): Boolean {
        val cleanName = name.replace("|", "").trim()
        if (cleanName.isEmpty()) return false

        val prefs = context.getSharedPreferences("bchat_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("settings_display_name", cleanName).apply()
        _localDeviceName.value = cleanName

        val avatarId = prefs.getInt("profile_avatar_id", 1)
        val rawName = "$cleanName|$avatarId"

        if (bluetoothAdapter != null) {
            try {
                bluetoothAdapter.name = rawName
            } catch (e: SecurityException) {
                Log.e("BluetoothController", "SecurityException changing adapter name", e)
            }
        }

        // Broadcast the profile update to the currently connected device in real-time
        val profilePacket = "__PROFILE_SYNC__|$cleanName|$avatarId"
        scope.launch {
            sendMessage(profilePacket)
        }

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
            stopAdvertising()
            startAdvertising()
        }
        return true
    }

    override fun updateConnectedDeviceProfile(address: String, name: String, avatarId: Int) {
        _connectedDevice.update { current ->
            if (current != null && current.address.equals(address, ignoreCase = true)) {
                current.copy(name = name, avatarId = avatarId)
            } else {
                BluetoothDeviceDomain(
                    name = name,
                    address = address,
                    connectionState = ConnectionState.CONNECTED,
                    avatarId = avatarId
                )
            }
        }
    }

    override fun release() {
        try {
            context.unregisterReceiver(bluetoothReceiver)
        } catch (e: IllegalArgumentException) {
            // Already unregistered
        }
        closeConnection()
    }

    // BLE GATT Server peripheral setups
    fun startServer() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        if (gattServer == null) {
            try {
                gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
                val service = BluetoothGattService(serviceUuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)

                val writeChar = BluetoothGattCharacteristic(
                    writeCharUuid,
                    BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE
                )

                val notifyChar = BluetoothGattCharacteristic(
                    notifyCharUuid,
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ
                )

                val cccd = BluetoothGattDescriptor(
                    cccdUuid,
                    BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
                )
                notifyChar.addDescriptor(cccd)

                service.addCharacteristic(writeChar)
                service.addCharacteristic(notifyChar)

                gattServer?.addService(service)
                Log.d("BleController", "GATT Server started and primary services added.")
            } catch (e: SecurityException) {
                Log.e("BleController", "Security exception starting GATT server", e)
            }
        }
    }

    private fun closeConnection() {
        activeGattClient?.disconnect()
        activeGattClient?.close()
        activeGattClient = null
        
        gattServer?.close()
        gattServer = null
        
        activeGattDevice = null
        _connectedDevice.value = null
    }

    private fun handleIncomingPacket(deviceAddress: String, rawText: String): Boolean {
        if (rawText.startsWith("__PROFILE_SYNC__|")) {
            val parts = rawText.split("|")
            val name = parts.getOrNull(1) ?: "B-Chat Node"
            val avatarId = parts.getOrNull(2)?.toIntOrNull() ?: 1
            
            Log.d("BleController", "Received Profile Sync Packet from $deviceAddress: Name=$name, Avatar=$avatarId")
            
            // 1. Update _connectedDevice if it matches
            _connectedDevice.update { current ->
                if (current != null && current.address.equals(deviceAddress, ignoreCase = true)) {
                    current.copy(name = name, avatarId = avatarId)
                } else {
                    BluetoothDeviceDomain(
                        name = name,
                        address = deviceAddress,
                        connectionState = ConnectionState.CONNECTED,
                        avatarId = avatarId
                    )
                }
            }
            
            // 2. Update _scannedDevices
            _scannedDevices.update { list ->
                list.map { dev ->
                    if (dev.address == deviceAddress) {
                        dev.copy(name = name, avatarId = avatarId)
                    } else {
                        dev
                    }
                }
            }
            return true
        }
        return false
    }

    // BLE Advertiser logic
    private fun startAdvertising() {
        val advertiser = bluetoothAdapter?.bluetoothLeAdvertiser ?: return
        
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false) // Save space inside the primary packet
            .addServiceUuid(ParcelUuid(serviceUuid))
            .build()

        val scanResponse = AdvertiseData.Builder()
            .setIncludeDeviceName(true) // Include the name inside scan response data
            .build()

        try {
            advertiser.startAdvertising(settings, data, scanResponse, advertiseCallback)
        } catch (e: SecurityException) {
            Log.e("BleController", "SecurityException starting advertising", e)
        }
    }

    private fun stopAdvertising() {
        val advertiser = bluetoothAdapter?.bluetoothLeAdvertiser ?: return
        try {
            advertiser.stopAdvertising(advertiseCallback)
            Log.d("BleController", "BLE Advertising stopped.")
        } catch (e: SecurityException) {
            Log.e("BleController", "SecurityException stopping advertising", e)
        }
    }

    // BLE Scanner logic
    private fun startScanning() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return
        
        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(serviceUuid))
                .build()
        )

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        try {
            scanner.startScan(filters, settings, scanCallback)
            isScanning = true
            startDeviceSweeper()
            Log.d("BleController", "BLE Scanning started successfully.")
        } catch (e: SecurityException) {
            Log.e("BleController", "SecurityException starting scan", e)
        }
    }

    private fun stopScanning() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner ?: return
        try {
            scanner.stopScan(scanCallback)
            isScanning = false
            sweeperJob?.cancel()
            Log.d("BleController", "BLE Scanning stopped.")
        } catch (e: SecurityException) {
            Log.e("BleController", "SecurityException stopping scan", e)
        }
    }

    private fun startDeviceSweeper() {
        sweeperJob?.cancel()
        sweeperJob = scope.launch {
            while (isScanning) {
                kotlinx.coroutines.delay(5000)
                val cutoff = System.currentTimeMillis() - 10000
                _scannedDevices.update { list ->
                    list.filter { device ->
                        val lastSeen = lastSeenTimestamps[device.address] ?: 0L
                        lastSeen >= cutoff
                    }
                }
            }
        }
    }
}
