package com.nearchat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class NearbyUser(
    @PrimaryKey val id: String,
    val displayName: String,
    val avatarUri: String? = null,
    val medium: ConnectionMedium,
    val signalStrength: Int = 0,
    val lastSeen: Long = System.currentTimeMillis(),
)

enum class ConnectionMedium {
    BLUETOOTH,
    WIFI_DIRECT,
}
