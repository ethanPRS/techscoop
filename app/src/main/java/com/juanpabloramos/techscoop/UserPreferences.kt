package com.juanpabloramos.techscoop

import android.content.Context

object UserPreferences {
    private const val PREFS = "techscoop_prefs"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_NAME = "user_name"
    private const val KEY_AVATAR_URI = "avatar_uri"
    private const val KEY_INTERESTS = "interests_csv"
    private const val KEY_UID = "firebase_uid"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isLoggedIn(context: Context): Boolean =
        prefs(context).getBoolean(KEY_LOGGED_IN, false)

    fun setLoggedIn(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_LOGGED_IN, value).commit()
    }

    fun saveUser(context: Context, email: String, displayName: String) {
        prefs(context).edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_NAME, displayName.ifBlank { email.substringBefore("@") })
            .apply()
    }

    fun getEmail(context: Context): String =
        prefs(context).getString(KEY_EMAIL, "") ?: ""

    fun getDisplayName(context: Context): String =
        prefs(context).getString(KEY_NAME, "") ?: ""

    fun setDisplayName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_NAME, name).apply()
    }

    fun setEmail(context: Context, email: String) {
        prefs(context).edit().putString(KEY_EMAIL, email).apply()
    }

    fun getAvatarUriString(context: Context): String? =
        prefs(context).getString(KEY_AVATAR_URI, null)

    fun setAvatarUriString(context: Context, uri: String?) {
        prefs(context).edit().putString(KEY_AVATAR_URI, uri).apply()
    }

    fun getInterestKeys(context: Context): Set<String> {
        val raw = prefs(context).getString(KEY_INTERESTS, "") ?: ""
        if (raw.isBlank()) return emptySet()
        return raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun setInterestKeys(context: Context, keys: Collection<String>) {
        prefs(context).edit().putString(KEY_INTERESTS, keys.joinToString(",")).apply()
    }

    fun setFirebaseUid(context: Context, uid: String?) {
        prefs(context).edit().apply {
            if (uid.isNullOrBlank()) remove(KEY_UID) else putString(KEY_UID, uid)
        }.apply()
    }

    fun getFirebaseUid(context: Context): String? =
        prefs(context).getString(KEY_UID, null)

    fun clearSession(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .remove(KEY_EMAIL)
            .remove(KEY_NAME)
            .remove(KEY_AVATAR_URI)
            .remove(KEY_UID)
            .apply()
    }
}
