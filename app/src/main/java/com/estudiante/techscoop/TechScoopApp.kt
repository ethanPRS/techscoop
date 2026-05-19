package com.estudiante.techscoop

import android.app.Application
import com.estudiante.techscoop.data.PreferencesManager
import com.estudiante.techscoop.data.SessionManager
import com.google.firebase.auth.FirebaseAuth

// Application: inicializa prefs de usuario/noticias y restaura sesión Firebase al arrancar.
class TechScoopApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.init(this)
        PreferencesManager.init(this)
        // Si Firebase ya tiene sesión (reinicio de app), sincroniza email para PreferencesManager.
        FirebaseAuth.getInstance().currentUser?.email?.let { SessionManager.loginUser(it) }
    }
}
