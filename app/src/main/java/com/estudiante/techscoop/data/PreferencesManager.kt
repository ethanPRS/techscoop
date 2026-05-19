package com.estudiante.techscoop.data

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object PreferencesManager {
    private const val PREFS_NAME = "techscoop_user_prefs"
    
    private const val KEY_LANGUAGE = "pref_language_"
    private const val KEY_CATEGORY = "pref_category_"
    private const val KEY_SORT_BY = "pref_sort_by_"

    private val _preferencesChanged = MutableLiveData<Long>()
    val preferencesChanged: LiveData<Long> get() = _preferencesChanged

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Default values
    // language: "es" (espa├▒ol), "en" (ingl├®s), "" (todos)
    // category: "technology", "business", "sports", "entertainment", "general"
    // sortBy: "publishedAt", "relevancy", "popularity"

    fun savePreferences(language: String, category: String, sortBy: String) {
        val userEmail = SessionManager.getCurrentUserEmail() ?: "default"
        prefs.edit()
            .putString(KEY_LANGUAGE + userEmail, language)
            .putString(KEY_CATEGORY + userEmail, category)
            .putString(KEY_SORT_BY + userEmail, sortBy)
            .apply()
        
        _preferencesChanged.postValue(System.currentTimeMillis())
    }

    fun getLanguage(): String {
        val userEmail = SessionManager.getCurrentUserEmail() ?: "default"
        return prefs.getString(KEY_LANGUAGE + userEmail, "en") ?: "en"
    }

    fun getCategory(): String {
        val userEmail = SessionManager.getCurrentUserEmail() ?: "default"
        return prefs.getString(KEY_CATEGORY + userEmail, "technology") ?: "technology"
    }

    fun getSortBy(): String {
        val userEmail = SessionManager.getCurrentUserEmail() ?: "default"
        return prefs.getString(KEY_SORT_BY + userEmail, "publishedAt") ?: "publishedAt"
    }
}
