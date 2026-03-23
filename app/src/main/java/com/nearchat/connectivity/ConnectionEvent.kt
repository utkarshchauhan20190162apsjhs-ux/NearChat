package com.nearchat.connectivity

import com.nearchat.data.model.ConnectionMedium
import com.nearchat.data.model.WireMessage

data class DiscoveredDevice(
    val id: String,
    val name: String,
    val medium: ConnectionMedium,
    val rssi: Int = 0,
)

sealed interface ConnectionEvent {
    data class DeviceFound(val device: DiscoveredDevice) : ConnectionEvent
    data class Connected(val device: DiscoveredDevice) : ConnectionEvent
    data class Disconnected(val deviceId: String) : ConnectionEvent
    data class MessageReceived(val deviceId: String, val message: WireMessage) : ConnectionEvent
    data class Error(val reason: String) : ConnectionEvent
}
