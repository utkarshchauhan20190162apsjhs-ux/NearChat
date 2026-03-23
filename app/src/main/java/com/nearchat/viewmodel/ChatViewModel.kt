package com.nearchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearchat.connectivity.ConnectionEvent
import com.nearchat.connectivity.ConnectivityCoordinator
import com.nearchat.data.model.ChatMessage
import com.nearchat.data.model.MessageStatus
import com.nearchat.data.model.Profile
import com.nearchat.data.model.WireMessage
import com.nearchat.data.repo.ChatRepository
import com.nearchat.data.repo.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val profileRepository: ProfileRepository,
    private val coordinator: ConnectivityCoordinator,
    private val remoteUserId: String,
) : ViewModel() {
    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    val messages = profileRepository.profile.filterNotNull().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Profile("", "", null)
    )

    val conversation = chatRepository.observeConversation("self", remoteUserId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        viewModelScope.launch {
            coordinator.events.collect { event ->
                if (event is ConnectionEvent.MessageReceived && event.deviceId == remoteUserId) {
                    chatRepository.saveMessage(
                        ChatMessage(
                            senderId = event.message.senderId,
                            receiverId = "self",
                            body = event.message.body,
                            timestamp = event.message.timestamp,
                            outgoing = false,
                            status = MessageStatus.DELIVERED
                        )
                    )
                }
            }
        }
    }

    fun updateInput(value: String) {
        _messageInput.value = value
    }

    fun sendMessage() {
        val text = _messageInput.value.trim()
        if (text.isEmpty()) return
        _messageInput.value = ""

        viewModelScope.launch {
            val profile = profileRepository.profile.first()
            val outgoing = ChatMessage(
                senderId = "self",
                receiverId = remoteUserId,
                body = text,
                timestamp = System.currentTimeMillis(),
                outgoing = true,
                status = MessageStatus.PENDING
            )
            val id = chatRepository.saveMessage(outgoing)

            val sent = coordinator.send(
                remoteUserId,
                WireMessage(
                    senderId = profile.id,
                    senderName = profile.name,
                    body = text,
                    timestamp = System.currentTimeMillis()
                )
            )

            chatRepository.updateMessage(
                outgoing.copy(
                    id = id,
                    status = if (sent) MessageStatus.SENT else MessageStatus.FAILED
                )
            )
        }
    }
}
