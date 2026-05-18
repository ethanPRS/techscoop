package com.juanpabloramos.techscoop

data class NewsItem(
    val title: String,
    val source: String,
    val date: String,
    val imageUrl: String? = null,
    val articleUrl: String? = null,
    val description: String? = null,
    val contentPreview: String? = null
)

fun NewsItem.stableKey(): String =
    articleUrl?.takeIf { it.isNotBlank() } ?: title
