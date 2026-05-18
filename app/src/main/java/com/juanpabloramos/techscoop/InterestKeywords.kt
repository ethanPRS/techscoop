package com.juanpabloramos.techscoop

object InterestKeywords {
    private val map = mapOf(
        "ia" to listOf("ai", "inteligencia artificial", "machine learning", "gpt", "openai", "llm"),
        "mobile" to listOf("mobile", "android", "ios", "iphone", "app", "smartphone"),
        "cloud" to listOf("cloud", "aws", "azure", "gcp", "kubernetes", "saas"),
        "security" to listOf("security", "ciberseguridad", "hack", "malware", "privacy", "encryption"),
        "data" to listOf("data", "database", "analytics", "sql", "big data", "dataset"),
        "gadgets" to listOf("gadget", "hardware", "device", "wearable", "review", "laptop")
    )

    fun allKeywordsForSelection(interestKeys: Set<String>): List<String> =
        interestKeys
            .map { it.lowercase().trim() }
            .flatMap { key -> map[key].orEmpty() }
            .distinct()
}
