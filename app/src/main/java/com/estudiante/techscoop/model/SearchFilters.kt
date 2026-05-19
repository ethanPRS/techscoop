package com.estudiante.techscoop.model

data class SearchFilters(
    val query: String,
    val sources: String? = null,
    val language: String? = "en",
    val sortBy: String? = "publishedAt",
    val from: String? = null,
    val to: String? = null
)
