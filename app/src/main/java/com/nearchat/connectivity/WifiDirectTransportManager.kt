package com.nearchat.connectivity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import com.nearchat.data.model.ConnectionMedium
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class WifiDirectTransportManager(
    private val context: Context,
) : TransportManager {
    private val manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = manager.initialize(context, context.mainLooper, null)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _events = MutableSharedFlow<ConnectionEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<ConnectionEvent> = _events

    private val peerReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.action == WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION) {
                manager.requestPeers(channel) { peers ->
                    peers.deviceList.forEach { device ->
                        scope.launch {
                            _events.emit(
                                ConnectionEvent.DeviceFound(
                                    DiscoveredDevice(
                                        id = device.deviceAddress,
                                        name = device.deviceName ?: "Wi-Fi Direct Device",
                                        medium = ConnectionMedium.WIFI_DIRECT
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    override fun startDiscovery() {
        context.registerReceiver(
            peerReceiver,
            IntentFilter(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        )
        manager.discoverPeers(channel, null)
    }

    override fun stopDiscovery() {
        kotlin.runCatching { context.unregisterReceiver(peerReceiver) }
        manager.stopPeerDiscovery(channel, null)
    }

    override suspend fun connect(deviceId: String) {
        val config = WifiP2pConfig().apply {
            this.deviceAddress = deviceId
        }
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                scope.launch {
                    _events.emit(
                        ConnectionEvent.Connected(
                            DiscoveredDevice(deviceId, "Wi-Fi Direct Device", ConnectionMedium.WIFI_DIRECT)
                        )
                    )
                }
            }

            override fun onFailure(reason: Int) {
                scope.launch { _events.emit(ConnectionEvent.Error("Wi-Fi connect failed: $reason")) }
            }
        })
    }

    override suspend fun disconnect(deviceId: String) {
        manager.removeGroup(channel, null)
        _events.emit(ConnectionEvent.Disconnected(deviceId))
    }

    override suspend fun send(deviceId: String, payload: String): Boolean {
        // Socket channel to group owner/client can dispatch payload here.
        return payload.isNotBlank()
    }
}
