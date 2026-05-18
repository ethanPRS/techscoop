package com.juanpabloramos.techscoop.data.local

import androidx.room.Entity

@Entity(tableName = "user_profile")
data class UserEntity(
    @androidx.room.PrimaryKey val uid: Int = 1,
    val name: String,
    val email: String,
    val bio: String? = null,
    val profileImageUri: String? = null
)

@Entity(tableName = "feed_preferences")
data class FeedPreferenceEntity(
    @androidx.room.PrimaryKey val id: Int = 1,
    val preferredLanguage: String = "es",
    val preferredSource: String? = null,
    val sortBy: String = "publishedAt",
    val lastSyncTimestamp: Long = 0L
)
