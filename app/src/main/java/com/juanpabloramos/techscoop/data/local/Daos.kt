package com.juanpabloramos.techscoop.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE uid = 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM user_profile")
    suspend fun clearAll()
}

@Dao
interface PreferenceDao {
    @Query("SELECT * FROM feed_preferences WHERE id = 1")
    suspend fun getPreferences(): FeedPreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(prefs: FeedPreferenceEntity)

    @Update
    suspend fun updatePreferences(prefs: FeedPreferenceEntity)
}
