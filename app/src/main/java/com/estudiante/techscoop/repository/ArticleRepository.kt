package com.estudiante.techscoop.repository

import com.estudiante.techscoop.BuildConfig
import com.estudiante.techscoop.data.PreferencesManager
import com.estudiante.techscoop.data.remote.APIService
import com.estudiante.techscoop.model.Articles
import com.estudiante.techscoop.model.DataArticle
import com.estudiante.techscoop.model.SearchFilters
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

class ArticleRepository {

    companion object {
        private const val BASE_URL = "https://newsapi.org/"
    }

    val api: APIService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIService::class.java)
    }

    suspend fun getNews(): ApiResult {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.NEWS_API_KEY
                if (apiKey.isBlank() || apiKey == "TU_API_KEY_AQUI" || apiKey == "YOUR_API_KEY_HERE") {
                    return@withContext ApiResult.Error(
                        0,
                        "API Key not configured. Add NEWS_API_KEY in local.properties"
                    )
                }

                val query = PreferencesManager.getCategory()
                val language = PreferencesManager.getLanguage()
                val sortBy = PreferencesManager.getSortBy()

                val response = api.searchEverything(
                    query = query,
                    sources = null,
                    language = if (language.isEmpty()) null else language,
                    sortBy = sortBy,
                    from = null,
                    to = null,
                    apiKey = apiKey
                )

                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    ApiResult.Success(articles)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No details"
                    ApiResult.Error(response.code(), "HTTP ${response.code()}: $errorBody")
                }
            } catch (e: java.net.UnknownHostException) {
                ApiResult.Exception("No internet connection")
            } catch (e: kotlin.Exception) {
                ApiResult.Exception(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun searchNews(filters: SearchFilters): ApiResult {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.NEWS_API_KEY
                if (apiKey.isBlank() || apiKey == "TU_API_KEY_AQUI" || apiKey == "YOUR_API_KEY_HERE") {
                    return@withContext ApiResult.Error(
                        0,
                        "API Key not configured. Add NEWS_API_KEY in local.properties"
                    )
                }

                if (filters.query.isBlank()) {
                    return@withContext ApiResult.Error(0, "Enter a search query")
                }

                val response = api.searchEverything(
                    query = filters.query,
                    sources = filters.sources?.takeIf { it.isNotBlank() },
                    language = filters.language?.takeIf { it.isNotBlank() },
                    sortBy = filters.sortBy?.takeIf { it.isNotBlank() },
                    from = filters.from?.takeIf { it.isNotBlank() },
                    to = filters.to?.takeIf { it.isNotBlank() },
                    apiKey = apiKey
                )

                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    ApiResult.Success(articles)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No details"
                    ApiResult.Error(response.code(), "HTTP ${response.code()}: $errorBody")
                }
            } catch (e: java.net.UnknownHostException) {
                ApiResult.Exception("No internet connection")
            } catch (e: kotlin.Exception) {
                ApiResult.Exception(e.message ?: "Unknown error")
            }
        }
    }
}
