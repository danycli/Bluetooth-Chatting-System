package com.example.bluetoothchattingsystem.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE senderAddress = :senderAddress ORDER BY timestamp ASC")
    fun getMessagesForDevice(senderAddress: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id IN (SELECT MAX(id) FROM messages GROUP BY senderAddress) ORDER BY timestamp DESC")
    fun getAllLastMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages(): Int
    
    @Query("DELETE FROM messages WHERE senderAddress = :senderAddress")
    suspend fun deleteConversation(senderAddress: String): Int

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Int): Int

    @Query("UPDATE messages SET messageText = :newText WHERE id = :messageId")
    suspend fun updateMessageText(messageId: Int, newText: String): Int
}
