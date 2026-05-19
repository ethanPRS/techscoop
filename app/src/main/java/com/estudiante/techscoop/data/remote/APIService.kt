package com.estudiante.techscoop.data.remote

import com.estudiante.techscoop.model.Articles
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService {
    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("category") category: String?,
        @Query("language") language: String?,
        @Query("apiKey") apiKey: String
    ): Response<Articles>

    @GET("v2/everything")
    suspend fun searchEverything(
        @Query("q") query: String,
        @Query("sources") sources: String?,
        @Query("language") language: String?,
        @Query("sortBy") sortBy: String?,
        @Query("from") from: String?,
        @Query("to") to: String?,
        @Query("pageSize") pageSize: Int = 50,
        @Query("apiKey") apiKey: String
    ): Response<Articles>
}
