package com.nearchat.connectivity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.nearchat.data.model.ConnectionMedium
import com.nearchat.data.model.WireMessage
import com.nearchat.util.JsonCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class BluetoothTransportManager(
    private val context: Context,
) : TransportManager {
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _events = MutableSharedFlow<ConnectionEvent>(extraBufferCapacity = 32)
    override val events: SharedFlow<ConnectionEvent> = _events

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        scope.launch {
                            _events.emit(
                                ConnectionEvent.DeviceFound(
                                    DiscoveredDevice(
                                        id = it.address,
                                        name = it.name ?: "Bluetooth Device",
                                        medium = ConnectionMedium.BLUETOOTH
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
        if (adapter == null) {
            scope.launch { _events.emit(ConnectionEvent.Error("Bluetooth not supported")) }
            return
        }
        context.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        adapter.startDiscovery()
    }

    override fun stopDiscovery() {
        kotlin.runCatching { context.unregisterReceiver(receiver) }
        adapter?.cancelDiscovery()
    }

    override suspend fun connect(deviceId: String) {
        val device = adapter?.bondedDevices?.firstOrNull { it.address == deviceId }
        if (device == null) {
            _events.emit(ConnectionEvent.Error("Bluetooth device unavailable"))
            return
        }
        _events.emit(
            ConnectionEvent.Connected(
                DiscoveredDevice(device.address, device.name ?: "Bluetooth Device", ConnectionMedium.BLUETOOTH)
            )
        )
    }

    override suspend fun disconnect(deviceId: String) {
        _events.emit(ConnectionEvent.Disconnected(deviceId))
    }

    override suspend fun send(deviceId: String, payload: String): Boolean {
        // Socket channel implementation can be plugged in here for RFCOMM.
        return payload.isNotBlank()
    }

    suspend fun mockInbound(deviceId: String, payload: String) {
        _events.emit(ConnectionEvent.MessageReceived(deviceId, JsonCodec.decode(payload, WireMessage::class.java)))
    }
}
