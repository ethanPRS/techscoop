package com.estudiante.techscoop.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * PreferencesManager guardar la configuración
 * del usuario (idioma, categoría y orden) de forma persistente en el dispositivo.
 * Utiliza SharedPreferences para que los datos no se borren al cerrar la aplicación.
 * Requiere init() en TechScoopApp.
 */
object PreferencesManager {
    // Nombre del archivo interno donde Android guardará estas preferencias
    private const val PREFS_NAME = "techscoop_user_prefs"
    
    // Constantes (llaves base) para identificar cada dato.
    // Usamos esto para construir llaves únicas por usuario
    private const val KEY_LANGUAGE = "pref_language_"
    private const val KEY_CATEGORY = "pref_category_"
    private const val KEY_SORT_BY = "pref_sort_by_"

    // Cada vez que el usuario cambie una preferencia, emitiremos una señal
    // para que la pantalla de HomeFragment sepa que debe recargar las noticias de nuevo.
    private val _preferencesChanged = MutableLiveData<Long>()
    val preferencesChanged: LiveData<Long> get() = _preferencesChanged

    private lateinit var prefs: SharedPreferences

    /**
     * Debe llamarse una sola vez al abrir la aplicación
     * para inicializar y conectar el archivo de preferencias con la app.
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Valores por defecto: language "es"|"en", category technology|business|..., sortBy publishedAt|relevancy|popularity
    
    /**
     * Guarda la selección del usuario (idioma, categoría y orden) en el teléfono.
     * Vincula los datos al correo actual para que si inicia sesión otra persona,
     * tenga sus propias preferencias separadas.
     */
    fun savePreferences(language: String, category: String, sortBy: String) {
        val userEmail = SessionManager.getCurrentUserEmail() ?: "default"
        
        // El .edit() abre el archivo, los putString preparan los datos, y apply() los guarda
        prefs.edit()
            .putString(KEY_LANGUAGE + userEmail, language)
            .putString(KEY_CATEGORY + userEmail, category)
            .putString(KEY_SORT_BY + userEmail, sortBy)
            .apply()
        
        // "Avisamos a toda la app que las preferencias acaban de cambiar
        _preferencesChanged.postValue(System.currentTimeMillis())
    }

    /**
     * Recupera el idioma seleccionado por el usuario actual.
     * Si nunca ha guardado uno, devolverá "en" (Inglés) por defecto.
     */
    fun getLanguage(): String {
        val userEmail = SessionManager.getCurrentUserEmail() ?: "default"
        return prefs.getString(KEY_LANGUAGE + userEmail, "en") ?: "en"
    }

    /**
     * Recupera la categoría seleccionada por el usuario actual.
     * Si nunca ha guardado una, devolverá "technology" por defecto.
     */
    fun getCategory(): String {
        val userEmail = SessionManager.getCurrentUserEmail() ?: "default"
        return prefs.getString(KEY_CATEGORY + userEmail, "technology") ?: "technology"
    }

    /**
     * Recupera el método de ordenamiento seleccionado por el usuario actual.
     * Si nunca ha guardado uno, devolverá "publishedAt" por defecto.
     */
    fun getSortBy(): String {
        val userEmail = SessionManager.getCurrentUserEmail() ?: "default"
        return prefs.getString(KEY_SORT_BY + userEmail, "publishedAt") ?: "publishedAt"
    }
}
