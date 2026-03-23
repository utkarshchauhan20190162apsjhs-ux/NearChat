package com.nearchat.connectivity

import com.nearchat.data.model.ConnectionMedium
import com.nearchat.data.model.WireMessage
import com.nearchat.util.JsonCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ConnectivityCoordinator(
    private val bluetoothManager: BluetoothTransportManager,
    private val wifiManager: WifiDirectTransportManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _events = MutableStateFlow<ConnectionEvent?>(null)
    val events: StateFlow<ConnectionEvent?> = _events.asStateFlow()

    private val connectionMap = mutableMapOf<String, ConnectionMedium>()

    init {
        bluetoothManager.events.onEach { _events.value = it }.launchIn(scope)
        wifiManager.events.onEach { _events.value = it }.launchIn(scope)
    }

    fun startDiscovery() {
        bluetoothManager.startDiscovery()
        wifiManager.startDiscovery()
    }

    fun stopDiscovery() {
        bluetoothManager.stopDiscovery()
        wifiManager.stopDiscovery()
    }

    fun connect(deviceId: String, preferredMedium: ConnectionMedium) {
        scope.launch {
            when (preferredMedium) {
                ConnectionMedium.BLUETOOTH -> bluetoothManager.connect(deviceId)
                ConnectionMedium.WIFI_DIRECT -> wifiManager.connect(deviceId)
            }
            connectionMap[deviceId] = preferredMedium
        }
    }

    fun disconnect(deviceId: String) {
        scope.launch {
            when (connectionMap[deviceId]) {
                ConnectionMedium.BLUETOOTH -> bluetoothManager.disconnect(deviceId)
                ConnectionMedium.WIFI_DIRECT -> wifiManager.disconnect(deviceId)
                null -> bluetoothManager.disconnect(deviceId)
            }
            connectionMap.remove(deviceId)
        }
    }

    suspend fun send(deviceId: String, message: WireMessage): Boolean {
        val payload = JsonCodec.encode(message)
        return when (connectionMap[deviceId]) {
            ConnectionMedium.BLUETOOTH -> bluetoothManager.send(deviceId, payload)
            ConnectionMedium.WIFI_DIRECT -> wifiManager.send(deviceId, payload)
            null -> false
        }
    }
}
