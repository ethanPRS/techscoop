package com.juanpabloramos.techscoop

import com.google.gson.annotations.SerializedName

data class ArticleSource(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?
)

data class DataArticle(
    @SerializedName("source") val source: ArticleSource?,
    @SerializedName("author") val author: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("urlToImage") val urlToImage: String?,
    @SerializedName("publishedAt") val publishedAt: String?,
    @SerializedName("content") val content: String?
)

data class ArticlesResponse(
    @SerializedName("status") val status: String,
    @SerializedName("totalResults") val totalResults: Int,
    @SerializedName("articles") val articles: List<DataArticle>
)

fun DataArticle.toNewsItem(): NewsItem =
    NewsItem(
        title = title ?: "Sin titulo",
        source = source?.name ?: "Fuente desconocida",
        date = publishedAt?.take(10) ?: "",
        imageUrl = urlToImage,
        articleUrl = url,
        description = description?.trim()?.takeIf { it.isNotBlank() },
        contentPreview = content
            ?.replace(Regex("\\s*\\[\\+\\d+ chars]$"), "")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    )
