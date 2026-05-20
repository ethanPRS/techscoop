package com.estudiante.techscoop.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.estudiante.techscoop.data.local.AppDatabase
import com.estudiante.techscoop.data.local.FavoriteDao
import com.estudiante.techscoop.data.local.FavoriteEntity
import com.estudiante.techscoop.model.DataArticle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FavoritesManager {
    // Lista interna y privada que guarda los artículos favoritos
    private val _favoriteArticlesList = mutableListOf<DataArticle>()

    private val _favoriteArticles = MutableLiveData<List<DataArticle>>(_favoriteArticlesList.toList())
    
    // Esta es la lista pública que pueden observar otras partes de la app
    val favoriteArticles: LiveData<List<DataArticle>> get() = _favoriteArticles

    private var favoriteDao: FavoriteDao? = null
    private var currentUserEmail: String? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun init(context: Context, email: String) {
        favoriteDao = AppDatabase.getInstance(context.applicationContext).favoriteDao()
        currentUserEmail = email
        loadFavoritesFromDb()
    }
    
    private fun loadFavoritesFromDb() {
        val email = currentUserEmail ?: return
        val dao = favoriteDao ?: return
        
        coroutineScope.launch {
            val entities = dao.getFavoritesForUser(email)
            val articles = entities.map { 
                DataArticle(
                    source = null, // Podríamos guardar el sourceName si fuera necesario reconstruirlo complejo
                    author = null,
                    title = it.title,
                    description = it.description,
                    url = it.url,
                    urlToImage = it.urlToImage,
                    publishedAt = it.publishedAt,
                    content = null
                )
            }
            
            withContext(Dispatchers.Main) {
                _favoriteArticlesList.clear()
                _favoriteArticlesList.addAll(articles)
                _favoriteArticles.postValue(_favoriteArticlesList.toList())
            }
        }
    }

    fun toggleFavorite(article: DataArticle): Boolean {
        val articleUrl = article.url ?: return false
        
        // Buscamos si ya existe el artículo usando su URL como identificador único
        val exists = _favoriteArticlesList.find { it.url == articleUrl }
        val email = currentUserEmail
        val dao = favoriteDao
        
        val isNowFavorite = if (exists != null) {
            // Si existe, significa que el usuario quiere quitarlo de favoritos
            _favoriteArticlesList.remove(exists)
            
            if (email != null && dao != null) {
                coroutineScope.launch { dao.deleteFavorite(articleUrl, email) }
            }
            
            false 
        } else {
            // Si no existe, lo agregamos a la lista
            _favoriteArticlesList.add(article)
            
            if (email != null && dao != null) {
                coroutineScope.launch {
                    dao.insertFavorite(
                        FavoriteEntity(
                            url = articleUrl,
                            userEmail = email,
                            title = article.title,
                            description = article.description,
                            urlToImage = article.urlToImage,
                            sourceName = article.source?.name,
                            publishedAt = article.publishedAt
                        )
                    )
                }
            }
            
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

    /**
     * Limpia la memoria local (útil al cerrar sesión)
     */
    fun clearMemory() {
        _favoriteArticlesList.clear()
        _favoriteArticles.postValue(_favoriteArticlesList.toList())
        currentUserEmail = null
        favoriteDao = null
    }
}
