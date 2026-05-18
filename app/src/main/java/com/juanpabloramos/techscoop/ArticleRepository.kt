package com.juanpabloramos.techscoop

import android.content.Context
import com.juanpabloramos.techscoop.data.local.AppDatabase
import com.juanpabloramos.techscoop.data.local.FeedPreferenceEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed class ApiResult {
    data class Success(val articles: List<DataArticle>) : ApiResult()
    data class Error(val code: Int, val message: String) : ApiResult()
    data class Exception(val error: String) : ApiResult()
}

class ArticleRepository(private val appContext: Context) {

    companion object {
        private const val BASE_URL = "https://newsapi.org/"

        fun filterByInterests(
            articles: List<DataArticle>,
            interestKeys: Set<String>
        ): List<DataArticle> {
            val keywords = InterestKeywords.allKeywordsForSelection(interestKeys)
                .map { it.lowercase() }
            if (keywords.isEmpty()) return articles.take(24)
            val filtered = articles.filter { art ->
                val blob = "${art.title.orEmpty()} ${art.description.orEmpty()} ${art.content.orEmpty()}"
                    .lowercase()
                keywords.any { blob.contains(it) }
            }
            return if (filtered.isEmpty()) articles.take(16) else filtered
        }
    }

    private val preferenceDao by lazy { AppDatabase.get(appContext).preferenceDao() }

    private val api: ApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    suspend fun getNews(): ApiResult = fetchExploreFromNetwork()

    suspend fun getTechnologyNews(): ApiResult = fetchHomeFromNetwork()

    suspend fun searchNews(filters: SearchFilters): ApiResult = fetchSearchFromNetwork(filters)

    private suspend fun ensureOnline(): ApiResult.Exception? {
        if (!NetworkUtils.isOnline(appContext)) {
            return ApiResult.Exception("Sin conexion a internet")
        }
        return null
    }

    private suspend fun markSyncSuccess() {
        val prefs = preferenceDao.getPreferences() ?: FeedPreferenceEntity()
        val updated = prefs.copy(lastSyncTimestamp = System.currentTimeMillis())
        if (preferenceDao.getPreferences() == null) {
            preferenceDao.insertPreferences(updated)
        } else {
            preferenceDao.updatePreferences(updated)
        }
    }

    private suspend fun currentPreferences(): FeedPreferenceEntity {
        return preferenceDao.getPreferences() ?: FeedPreferenceEntity().also {
            preferenceDao.insertPreferences(it)
        }
    }

    private suspend fun fetchExploreFromNetwork(): ApiResult = withContext(Dispatchers.IO) {
        ensureOnline()?.let { return@withContext it }
        try {
            val apiKey = requireApiKey() ?: return@withContext apiKeyMissing()
            val prefs = currentPreferences()
            val response = api.getTopHeadlines(
                source = prefs.preferredSource?.takeIf { it.isNotBlank() } ?: "techcrunch",
                apiKey = apiKey
            )
            parseArticlesResponse(response).also {
                if (it is ApiResult.Success) markSyncSuccess()
            }
        } catch (e: java.net.UnknownHostException) {
            ApiResult.Exception("Sin conexion a internet")
        } catch (e: Exception) {
            ApiResult.Exception(e.message ?: "Error desconocido")
        }
    }

    private suspend fun fetchHomeFromNetwork(): ApiResult = withContext(Dispatchers.IO) {
        ensureOnline()?.let { return@withContext it }
        try {
            val apiKey = requireApiKey() ?: return@withContext apiKeyMissing()
            val response = api.getTopHeadlinesByCategory(
                country = "us",
                category = "technology",
                apiKey = apiKey
            )
            parseArticlesResponse(response).also {
                if (it is ApiResult.Success) markSyncSuccess()
            }
        } catch (e: java.net.UnknownHostException) {
            ApiResult.Exception("Sin conexion a internet")
        } catch (e: Exception) {
            ApiResult.Exception(e.message ?: "Error desconocido")
        }
    }

    private suspend fun fetchSearchFromNetwork(filters: SearchFilters): ApiResult =
        withContext(Dispatchers.IO) {
            ensureOnline()?.let { return@withContext it }
            try {
                val apiKey = requireApiKey() ?: return@withContext apiKeyMissing()
                if (filters.query.isBlank()) {
                    return@withContext ApiResult.Error(0, "Escribe algo para buscar")
                }
                val prefs = currentPreferences()
                val response = api.searchEverything(
                    query = filters.query,
                    sources = filters.sources?.takeIf { it.isNotBlank() }
                        ?: prefs.preferredSource?.takeIf { it.isNotBlank() },
                    language = filters.language?.takeIf { it.isNotBlank() }
                        ?: prefs.preferredLanguage,
                    sortBy = filters.sortBy?.takeIf { it.isNotBlank() } ?: prefs.sortBy,
                    from = filters.from?.takeIf { it.isNotBlank() },
                    to = filters.to?.takeIf { it.isNotBlank() },
                    apiKey = apiKey
                )
                parseArticlesResponse(response).also {
                    if (it is ApiResult.Success) markSyncSuccess()
                }
            } catch (e: java.net.UnknownHostException) {
                ApiResult.Exception("Sin conexion a internet")
            } catch (e: Exception) {
                ApiResult.Exception(e.message ?: "Error desconocido")
            }
        }

    private fun requireApiKey(): String? {
        val apiKey = BuildConfig.NEWS_API_KEY
        return apiKey.takeIf { it.isNotBlank() }
    }

    private fun apiKeyMissing(): ApiResult.Error =
        ApiResult.Error(0, "API Key no configurada. Agrega NEWS_API_KEY en gradle.properties")

    private fun parseArticlesResponse(response: retrofit2.Response<ArticlesResponse>): ApiResult {
        return if (response.isSuccessful) {
            ApiResult.Success(response.body()?.articles ?: emptyList())
        } else {
            val errorBody = response.errorBody()?.string() ?: "Sin detalle"
            ApiResult.Error(response.code(), "HTTP ${response.code()}: $errorBody")
        }
    }
}
