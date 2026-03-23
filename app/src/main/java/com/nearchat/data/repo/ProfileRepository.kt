package com.nearchat.data.repo

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nearchat.data.model.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.profileStore by preferencesDataStore("profile_store")

class ProfileRepository(private val context: Context) {
    private object Keys {
        val id = stringPreferencesKey("profile_id")
        val name = stringPreferencesKey("profile_name")
        val avatar = stringPreferencesKey("profile_avatar")
        val lastDeviceId = stringPreferencesKey("last_device_id")
    }

    val profile: Flow<Profile> = context.profileStore.data.map {
        Profile(
            id = it[Keys.id] ?: UUID.randomUUID().toString(),
            name = it[Keys.name] ?: "You",
            avatarUri = it[Keys.avatar]
        )
    }

    val lastDeviceId: Flow<String?> = context.profileStore.data.map { it[Keys.lastDeviceId] }

    suspend fun saveProfile(profile: Profile) {
        context.profileStore.edit {
            it[Keys.id] = profile.id
            it[Keys.name] = profile.name
            profile.avatarUri?.let { avatar -> it[Keys.avatar] = avatar }
        }
    }

    suspend fun setLastDeviceId(deviceId: String) {
        context.profileStore.edit { it[Keys.lastDeviceId] = deviceId }
    }
}
