package com.nearchat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nearchat.data.model.ChatMessage
import com.nearchat.data.model.NearbyUser

@Database(entities = [ChatMessage::class, NearbyUser::class], version = 1, exportSchema = false)
abstract class NearChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var instance: NearChatDatabase? = null

        fun get(context: Context): NearChatDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                NearChatDatabase::class.java,
                "nearchat.db"
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}
