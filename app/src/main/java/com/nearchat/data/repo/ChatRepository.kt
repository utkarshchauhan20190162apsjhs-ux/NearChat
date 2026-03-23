package com.nearchat.data.repo

import com.nearchat.data.local.MessageDao
import com.nearchat.data.local.UserDao
import com.nearchat.data.model.ChatMessage
import com.nearchat.data.model.NearbyUser
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val messageDao: MessageDao,
    private val userDao: UserDao,
) {
    fun observeNearbyUsers(): Flow<List<NearbyUser>> = userDao.observeNearbyUsers()

    fun observeConversation(a: String, b: String): Flow<List<ChatMessage>> =
        messageDao.observeConversation(a, b)

    suspend fun saveNearbyUser(user: NearbyUser) = userDao.upsert(user)

    suspend fun saveMessage(message: ChatMessage): Long = messageDao.insert(message)

    suspend fun updateMessage(message: ChatMessage) = messageDao.update(message)

    suspend fun findUser(id: String): NearbyUser? = userDao.findById(id)
}
