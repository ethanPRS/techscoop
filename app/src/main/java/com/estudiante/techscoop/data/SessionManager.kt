package com.estudiante.techscoop.data

import android.content.Context
import android.content.SharedPreferences

// Sesión del usuario logueado (email) para prefs por cuenta y toast de bienvenida.
// Distinto de com.estudiante.techscoop.SessionManager (último email en formulario de login).
object SessionManager {
    private const val PREFS_NAME = "techscoop_user_session"
    private const val KEY_CURRENT_USER_EMAIL = "current_user_email"

    private lateinit var prefs: SharedPreferences

    var isWelcomeToastShown = false

    // Debe llamarse desde TechScoopApp antes de cualquier getCurrentUserEmail().
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Guarda el email tras login/registro (email o Google).
    fun loginUser(email: String) {
        prefs.edit().putString(KEY_CURRENT_USER_EMAIL, email).apply()
        isWelcomeToastShown = false
    }

    // Limpia email al cerrar sesión (AuthRepository.signOut).
    fun logoutUser() {
        prefs.edit().remove(KEY_CURRENT_USER_EMAIL).apply()
        isWelcomeToastShown = false
    }

    fun getCurrentUserEmail(): String? {
        return prefs.getString(KEY_CURRENT_USER_EMAIL, null)
    }
}
