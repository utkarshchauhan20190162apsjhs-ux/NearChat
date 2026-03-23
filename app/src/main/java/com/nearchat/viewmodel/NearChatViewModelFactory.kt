package com.nearchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nearchat.connectivity.ConnectivityCoordinator
import com.nearchat.data.repo.ChatRepository
import com.nearchat.data.repo.ProfileRepository

class NearChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val profileRepository: ProfileRepository,
    private val coordinator: ConnectivityCoordinator,
    private val remoteUserId: String? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(chatRepository, profileRepository, coordinator) as T

            modelClass.isAssignableFrom(ChatViewModel::class.java) ->
                ChatViewModel(chatRepository, profileRepository, coordinator, remoteUserId.orEmpty()) as T

            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(profileRepository) as T

            else -> error("Unsupported ViewModel: ${modelClass.simpleName}")
        }
    }
}
