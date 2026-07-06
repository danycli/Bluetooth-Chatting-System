package com.example.bluetoothchattingsystem.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothchattingsystem.data.DataRepository
import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothDeviceDomain
import com.example.bluetoothchattingsystem.data.local.MessageEntity
import com.example.bluetoothchattingsystem.data.update.AppUpdateInfo
import com.example.bluetoothchattingsystem.data.update.AppUpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BluetoothViewModel(
    private val repository: DataRepository,
    private val appUpdateManager: AppUpdateManager
) : ViewModel() {

    private val _updateInfo = MutableStateFlow<AppUpdateInfo?>(null)
    val updateInfo: StateFlow<AppUpdateInfo?> = _updateInfo.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _updateCheckError = MutableStateFlow<String?>(null)
    val updateCheckError: StateFlow<String?> = _updateCheckError.asStateFlow()

    val installedVersionName: String get() = appUpdateManager.getInstalledVersionName()

    fun checkForUpdates(forceCheck: Boolean) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            _updateCheckError.value = null
            try {
                val info = appUpdateManager.checkForUpdates(force = forceCheck)
                _updateInfo.value = info
            } catch (e: Exception) {
                _updateCheckError.value = e.localizedMessage ?: "Failed to check for updates"
            } finally {
                _isCheckingUpdate.value = false
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
    }

    fun clearUpdateCheckError() {
        _updateCheckError.value = null
    }

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
