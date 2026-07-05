package com.example.bluetoothchattingsystem.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothchattingsystem.data.DataRepository
import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothDeviceDomain
import com.example.bluetoothchattingsystem.data.bluetooth.ConnectionState
import com.example.bluetoothchattingsystem.data.local.MessageEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val repository: DataRepository
) : ViewModel() {

    private val _selectedAddress = MutableStateFlow<String?>(null)
    private var reconnectJob: Job? = null
    
    val connectedDevice: StateFlow<BluetoothDeviceDomain?> = repository.connectedDevice

    val messages: StateFlow<List<MessageEntity>> = _selectedAddress
        .filterNotNull()
        .flatMapLatest { address ->
            repository.getMessagesForDevice(address)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _typedText = MutableStateFlow("")
    val typedText: StateFlow<String> = _typedText.asStateFlow()

    fun setPeerAddress(address: String) {
        _selectedAddress.value = address
        viewModelScope.launch {
            repository.markConversationAsRead(address)
        }
    }

    fun updateTypedText(text: String) {
        _typedText.value = text
    }

    fun monitorAndReconnect(peerAddress: String, peerName: String) {
        reconnectJob?.cancel()
        reconnectJob = viewModelScope.launch {
            repository.connectedDevice.collect { device ->
                val isTargetDevice = device?.address == peerAddress
                val isConnected = isTargetDevice && device?.connectionState == ConnectionState.CONNECTED
                val isConnecting = isTargetDevice && device?.connectionState == ConnectionState.CONNECTING
                
                if (!isConnected && !isConnecting) {
                    // Back-off 3 seconds before attempting reconnect
                    delay(3000)
                    
                    val currentDevice = repository.connectedDevice.value
                    val stillDisconnected = currentDevice?.address != peerAddress || 
                            currentDevice.connectionState == ConnectionState.DISCONNECTED
                            
                    if (stillDisconnected) {
                        Log.d("ChatViewModel", "Auto-reconnecting to $peerAddress ($peerName)...")
                        val targetDomainDevice = BluetoothDeviceDomain(
                            name = peerName,
                            address = peerAddress,
                            connectionState = ConnectionState.DISCONNECTED
                        )
                        repository.connect(targetDomainDevice)
                    }
                }
            }
        }
    }

    fun sendCurrentMessage() {
        val text = _typedText.value.trim()
        if (text.isEmpty()) return
        
        viewModelScope.launch {
            val success = repository.sendChatMessage(text)
            if (success) {
                _typedText.value = ""
            }
        }
    }

    fun deleteMessage(messageId: Int) {
        viewModelScope.launch {
            repository.deleteMessageById(messageId)
        }
    }

    fun editMessage(messageId: Int, newText: String) {
        if (newText.trim().isEmpty()) return
        viewModelScope.launch {
            repository.updateMessageText(messageId, newText.trim())
        }
    }

    override fun onCleared() {
        super.onCleared()
        reconnectJob?.cancel()
    }
}
