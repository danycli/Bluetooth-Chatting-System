package com.example.bluetoothchattingsystem.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderAddress: String,
    val senderName: String,
    val messageText: String,
    val timestamp: Long,
    val isSent: Boolean,
    val isRead: Boolean = true
)
