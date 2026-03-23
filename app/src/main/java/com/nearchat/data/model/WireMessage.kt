package com.nearchat.data.model

data class WireMessage(
    val senderId: String,
    val senderName: String,
    val body: String,
    val timestamp: Long,
)
