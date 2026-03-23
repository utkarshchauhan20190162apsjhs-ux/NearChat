package com.nearchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearchat.connectivity.ConnectionEvent
import com.nearchat.connectivity.ConnectivityCoordinator
import com.nearchat.data.model.ConnectionMedium
import com.nearchat.data.model.NearbyUser
import com.nearchat.data.repo.ChatRepository
import com.nearchat.data.repo.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val chatRepository: ChatRepository,
    private val profileRepository: ProfileRepository,
    private val coordinator: ConnectivityCoordinator,
) : ViewModel() {
    private val _statusText = MutableStateFlow("Ready")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    val users = chatRepository.observeNearbyUsers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val suggestedDevice = combine(users, profileRepository.lastDeviceId) { userList, lastId ->
        userList.firstOrNull { it.id == lastId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            coordinator.events.collect { event ->
                when (event) {
                    is ConnectionEvent.DeviceFound -> {
                        chatRepository.saveNearbyUser(
                            NearbyUser(
                                id = event.device.id,
                                displayName = event.device.name,
                                medium = event.device.medium,
                                signalStrength = event.device.rssi
                            )
                        )
                    }
                    is ConnectionEvent.Connected -> _statusText.value = "Connected to ${event.device.name}"
                    is ConnectionEvent.Disconnected -> _statusText.value = "Disconnected"
                    is ConnectionEvent.Error -> _statusText.value = event.reason
                    null, is ConnectionEvent.MessageReceived -> Unit
                }
            }
        }
        discover()
    }

    fun discover() = coordinator.startDiscovery()

    fun connect(user: NearbyUser) {
        coordinator.connect(user.id, user.medium)
        viewModelScope.launch { profileRepository.setLastDeviceId(user.id) }
    }

    fun disconnect(user: NearbyUser) = coordinator.disconnect(user.id)

    fun reconnectLast() {
        val suggested = suggestedDevice.value ?: return
        connect(suggested)
    }
}
