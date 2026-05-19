package com.estudiante.techscoop

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

// Preferencias locales: último email y comprobación de sesión Firebase (no reemplaza a data.SessionManager del equipo).
object SessionManager {
    private const val PREFS = "techscoop_session"

    private const val KEY_EMAIL = "last_email"
    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        // true si Firebase aún tiene un usuario autenticado.
    fun isLoggedIn(): Boolean =
        FirebaseAuth.getInstance().currentUser != null

        // Recuerda el email en el formulario de login para la próxima vez.
    fun saveLastEmail(context: Context, email: String) {
        prefs(context).edit().putString(KEY_EMAIL, email).apply()
    }

    fun getLastEmail(context: Context): String =
        prefs(context).getString(KEY_EMAIL, "") ?: ""

        // Limpia preferencias al cerrar sesión.
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}



