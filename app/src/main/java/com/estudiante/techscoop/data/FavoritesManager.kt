package com.estudiante.techscoop.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.estudiante.techscoop.model.DataArticle

object FavoritesManager {
    // Lista interna y privada que guarda los artículos favoritos
    private val _favoriteArticlesList = mutableListOf<DataArticle>()

    private val _favoriteArticles = MutableLiveData<List<DataArticle>>(_favoriteArticlesList.toList())
    
    // Esta es la lista pública que pueden observar otras partes de la app
    val favoriteArticles: LiveData<List<DataArticle>> get() = _favoriteArticles

    fun toggleFavorite(article: DataArticle): Boolean {
        // Buscamos si ya existe el artículo usando su URL como identificador único
        val exists = _favoriteArticlesList.find { it.url == article.url }
        
        val isNowFavorite = if (exists != null) {
            // Si existe, significa que el usuario quiere quitarlo de favoritos
            _favoriteArticlesList.remove(exists)
            false 
        } else {
            // Si no existe, lo agregamos a la lista
            _favoriteArticlesList.add(article)
            true 
        }
        
        // Avisamos al LiveData que la lista cambió, para que las pantallas se refresquen
        _favoriteArticles.postValue(_favoriteArticlesList.toList())
        return isNowFavorite
    }

    /**
     * Revisa si un artículo ya está guardado en favoritos para saber si debemos pintar
     * el ícono del corazón de color rojo o dejarlo vacío.
     */
    fun isFavorite(article: DataArticle): Boolean {
        // Devuelve verdadero si algún artículo en la lista tiene la misma URL
        return _favoriteArticlesList.any { it.url == article.url }
    }
}
