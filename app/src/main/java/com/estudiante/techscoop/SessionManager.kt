package com.estudiante.techscoop

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

object SessionManager {
    private const val PREFS = "techscoop_session"

    private const val KEY_EMAIL = "last_email"
    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean =
        FirebaseAuth.getInstance().currentUser != null

    fun saveLastEmail(context: Context, email: String) {
        prefs(context).edit().putString(KEY_EMAIL, email).apply()
    }

    fun getLastEmail(context: Context): String =
        prefs(context).getString(KEY_EMAIL, "") ?: ""

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}


