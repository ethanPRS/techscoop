package com.estudiante.techscoop

data class SearchFilters(
    val query: String,
    val sources: String? = null,
    val language: String? = "es",
    val sortBy: String? = "publishedAt",
    val from: String? = null,
    val to: String? = null
)
