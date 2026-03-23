package com.nearchat.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nearchat.data.model.NearbyUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY lastSeen DESC")
    fun observeNearbyUsers(): Flow<List<NearbyUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: NearbyUser)

    @Query("DELETE FROM users")
    suspend fun clear()

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): NearbyUser?
}
