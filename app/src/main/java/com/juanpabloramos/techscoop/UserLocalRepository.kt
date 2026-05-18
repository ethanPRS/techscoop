package com.juanpabloramos.techscoop

import android.content.Context
import com.juanpabloramos.techscoop.data.local.AppDatabase
import com.juanpabloramos.techscoop.data.local.FeedPreferenceEntity
import com.juanpabloramos.techscoop.data.local.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserLocalRepository {

    suspend fun ensureDefaults(context: Context) = withContext(Dispatchers.IO) {
        val db = AppDatabase.get(context)
        if (db.preferenceDao().getPreferences() == null) {
            db.preferenceDao().insertPreferences(FeedPreferenceEntity())
        }
    }

    suspend fun saveSessionUser(
        context: Context,
        email: String,
        displayName: String,
        profileImageUri: String? = null
    ) = withContext(Dispatchers.IO) {
        ensureDefaults(context)
        AppDatabase.get(context).userDao().insertUser(
            UserEntity(
                name = displayName.ifBlank { email.substringBefore("@") },
                email = email,
                profileImageUri = profileImageUri
            )
        )
    }

    suspend fun updateProfile(
        context: Context,
        name: String,
        email: String,
        profileImageUri: String?
    ) = withContext(Dispatchers.IO) {
        val dao = AppDatabase.get(context).userDao()
        val current = dao.getUser()
        val entity = UserEntity(
            name = name,
            email = email,
            bio = current?.bio,
            profileImageUri = profileImageUri
        )
        if (current == null) dao.insertUser(entity) else dao.updateUser(entity)
    }

    suspend fun loadIntoPreferences(context: Context) = withContext(Dispatchers.IO) {
        val user = AppDatabase.get(context).userDao().getUser() ?: return@withContext
        UserPreferences.saveUser(context, user.email, user.name)
        UserPreferences.setDisplayName(context, user.name)
        UserPreferences.setEmail(context, user.email)
        user.profileImageUri?.let { UserPreferences.setAvatarUriString(context, it) }
    }

    suspend fun clearAll(context: Context) = withContext(Dispatchers.IO) {
        AppDatabase.get(context).userDao().clearAll()
    }
}
