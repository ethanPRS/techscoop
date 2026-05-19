package com.estudiante.techscoop.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val uid: Int = 1, // Usamos un ID fijo para el perfil del usuario local
    val name: String,
    val email: String,
    val password: String,
    val bio: String? = null,
    val status: String,
    val profileImageUri: String? = null
)

@Entity(tableName = "feed_preferences")
data class FeedPreferenceEntity(
    @PrimaryKey val id: Int = 1, // ID fijo para tener una única configuración
    val preferredSource: String? = null,
    val sortBy: String = "publishedAt",
    val lastSyncTimestamp: Long = 0L
)
