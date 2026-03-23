package com.nearchat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearchat.data.model.Profile
import com.nearchat.data.repo.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    val profile = profileRepository.profile.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Profile(UUID.randomUUID().toString(), "You", null)
    )

    fun updateProfile(name: String, avatarUri: String?) {
        viewModelScope.launch {
            profileRepository.saveProfile(
                profile.value.copy(
                    name = name.trim().ifBlank { "You" },
                    avatarUri = avatarUri
                )
            )
        }
    }
}
