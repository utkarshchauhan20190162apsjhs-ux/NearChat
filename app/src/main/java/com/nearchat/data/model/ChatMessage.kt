package com.nearchat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: String,
    val receiverId: String,
    val body: String,
    val timestamp: Long,
    val outgoing: Boolean,
    val status: MessageStatus = MessageStatus.PENDING,
)

enum class MessageStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED,
}
