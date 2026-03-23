package com.nearchat

import android.app.Application
import com.nearchat.data.local.NearChatDatabase
import com.nearchat.data.repo.ChatRepository
import com.nearchat.data.repo.ProfileRepository

class NearChatApp : Application() {
    lateinit var chatRepository: ChatRepository
        private set

    lateinit var profileRepository: ProfileRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = NearChatDatabase.get(this)
        chatRepository = ChatRepository(db.messageDao(), db.userDao())
        profileRepository = ProfileRepository(this)
    }
}
