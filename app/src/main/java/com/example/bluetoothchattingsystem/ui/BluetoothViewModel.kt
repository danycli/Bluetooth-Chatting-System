package com.example.bluetoothchattingsystem.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothchattingsystem.data.DataRepository
import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothDeviceDomain
import com.example.bluetoothchattingsystem.data.local.MessageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BluetoothViewModel(
    private val repository: DataRepository
) : ViewModel() {

    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>> = repository.scannedDevices
    val connectedDevice: StateFlow<BluetoothDeviceDomain?> = repository.connectedDevice
    val isBluetoothEnabled: StateFlow<Boolean> = repository.isBluetoothEnabled
    val localDeviceName: StateFlow<String> = repository.localDeviceName

    fun changeLocalDeviceName(name: String): Boolean {
        return repository.changeLocalDeviceName(name)
    }

    val lastConversations: StateFlow<List<MessageEntity>> = repository.lastMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _pairingDevice = MutableStateFlow<BluetoothDeviceDomain?>(null)
    val pairingDevice: StateFlow<BluetoothDeviceDomain?> = _pairingDevice.asStateFlow()

    fun startScan() {
        repository.startDiscovery()
    }

    fun stopScan() {
        repository.stopDiscovery()
    }

    fun showPairingDialog(device: BluetoothDeviceDomain) {
        _pairingDevice.value = device
    }

    fun dismissPairingDialog() {
        _pairingDevice.value = null
    }

    fun connectDevice(device: BluetoothDeviceDomain) {
        repository.connect(device)
    }

    fun disconnectDevice() {
        repository.disconnect()
    }

    fun setBluetoothEnabled(enabled: Boolean) {
        repository.setBluetoothEnabled(enabled)
    }

    fun requestBluetoothEnable() {
        repository.requestBluetoothEnable()
    }

    fun deleteConversation(address: String) {
        viewModelScope.launch {
            repository.deleteConversation(address)
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            repository.wipeLocalVault()
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopDiscovery()
    }
}
