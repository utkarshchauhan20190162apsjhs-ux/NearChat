package com.nearchat.connectivity

import kotlinx.coroutines.flow.Flow

interface TransportManager {
    val events: Flow<ConnectionEvent>
    fun startDiscovery()
    fun stopDiscovery()
    suspend fun connect(deviceId: String)
    suspend fun disconnect(deviceId: String)
    suspend fun send(deviceId: String, payload: String): Boolean
}
