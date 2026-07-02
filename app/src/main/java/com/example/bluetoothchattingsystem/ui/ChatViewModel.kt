package com.example.bluetoothchattingsystem.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothchattingsystem.data.DataRepository
import com.example.bluetoothchattingsystem.data.bluetooth.BluetoothDeviceDomain
import com.example.bluetoothchattingsystem.data.local.MessageEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    }

    fun updateTypedText(text: String) {
        _typedText.value = text
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
}
