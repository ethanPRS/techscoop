package com.estudiante.techscoop.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.estudiante.techscoop.model.DataArticle

object FavoritesManager {
    private val _favoriteArticlesList = mutableListOf<DataArticle>()
    
    private val _favoriteArticles = MutableLiveData<List<DataArticle>>(_favoriteArticlesList.toList())
    val favoriteArticles: LiveData<List<DataArticle>> get() = _favoriteArticles

    fun toggleFavorite(article: DataArticle): Boolean {
        // Asumiendo que el url es un identificador unico
        val exists = _favoriteArticlesList.find { it.url == article.url }
        val isNowFavorite = if (exists != null) {
            _favoriteArticlesList.remove(exists)
            false // Ya no es favorito
        } else {
            _favoriteArticlesList.add(article)
            true // Ahora es favorito
        }
        _favoriteArticles.postValue(_favoriteArticlesList.toList())
        return isNowFavorite
    }

    fun isFavorite(article: DataArticle): Boolean {
        return _favoriteArticlesList.any { it.url == article.url }
    }
}
