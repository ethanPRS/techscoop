package com.estudiante.techscoop

import com.estudiante.techscoop.BuildConfig
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
                if (apiKey.isBlank() || apiKey == "TU_API_KEY_AQUI") {
                    return@withContext ApiResult.Error(
                        0,
                        "API Key no configurada. Agrega NEWS_API_KEY en gradle.properties"
                    )
                }

                val response = api.getTopHeadlines(
                    source = "techcrunch",
                    apiKey = apiKey
                )

                if (response.isSuccessful) {
                    val articles = response.body()?.articles ?: emptyList()
                    ApiResult.Success(articles)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Sin detalle"
                    ApiResult.Error(response.code(), "HTTP ${response.code()}: $errorBody")
                }
            } catch (e: java.net.UnknownHostException) {
                ApiResult.Exception("Sin conexión a internet")
            } catch (e: kotlin.Exception) {
                ApiResult.Exception(e.message ?: "Error desconocido")
            }
        }
    }
}
