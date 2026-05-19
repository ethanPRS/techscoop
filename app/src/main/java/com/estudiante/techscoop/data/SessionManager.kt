package com.estudiante.techscoop.data

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "techscoop_session"
    private const val KEY_CURRENT_USER_EMAIL = "current_user_email"

    private lateinit var prefs: SharedPreferences
    
    var isWelcomeToastShown = false

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun loginUser(email: String) {
        prefs.edit().putString(KEY_CURRENT_USER_EMAIL, email).apply()
        isWelcomeToastShown = false
    }

    fun logoutUser() {
        prefs.edit().remove(KEY_CURRENT_USER_EMAIL).apply()
        isWelcomeToastShown = false
    }

    fun getCurrentUserEmail(): String? {
        return prefs.getString(KEY_CURRENT_USER_EMAIL, null)
    }
}
