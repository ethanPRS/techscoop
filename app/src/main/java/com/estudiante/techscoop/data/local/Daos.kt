package com.estudiante.techscoop.data.local

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE uid = 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

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

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_articles WHERE userEmail = :email")
    suspend fun getFavoritesForUser(email: String): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite_articles WHERE url = :url AND userEmail = :email")
    suspend fun deleteFavorite(url: String, email: String)
    
    @Query("DELETE FROM favorite_articles WHERE userEmail = :email")
    suspend fun clearFavoritesForUser(email: String)
}
