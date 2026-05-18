package com.juanpabloramos.techscoop

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

sealed class SaveArticleResult {
    data object Success : SaveArticleResult()
    data object AlreadySaved : SaveArticleResult()
    data class Failed(val reason: String) : SaveArticleResult()
}

object SavedArticlesStore {
    private const val PREFS = "techscoop_saved"
    private const val KEY_ITEMS = "items_json"
    private const val MAX_ITEMS = 80

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): List<NewsItem> {
        val raw = prefs(context).getString(KEY_ITEMS, "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    add(
                        NewsItem(
                            title = o.optString("title"),
                            source = o.optString("source"),
                            date = o.optString("date"),
                            imageUrl = o.optString("imageUrl").ifBlank { null },
                            articleUrl = o.optString("articleUrl").ifBlank { null },
                            description = o.optString("description").ifBlank { null },
                            contentPreview = o.optString("contentPreview").ifBlank { null }
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, item: NewsItem): SaveArticleResult {
        val url = item.articleUrl?.trim().orEmpty()
        if (url.isEmpty()) {
            return SaveArticleResult.Failed("La noticia no tiene enlace")
        }

        val key = url
        val existing = load(context)
        if (existing.any { it.articleUrl == key }) {
            return SaveArticleResult.AlreadySaved
        }

        val list = mutableListOf(item)
        list.addAll(existing)
        persist(context, list.take(MAX_ITEMS))
        return SaveArticleResult.Success
    }

    fun remove(context: Context, item: NewsItem) {
        val key = item.articleUrl ?: item.title
        val remaining = load(context).filter { (it.articleUrl ?: it.title) != key }
        persist(context, remaining)
    }

    fun isSaved(context: Context, item: NewsItem): Boolean {
        val url = item.articleUrl ?: return false
        return load(context).any { it.articleUrl == url }
    }

    private fun persist(context: Context, items: List<NewsItem>) {
        val arr = JSONArray()
        for (item in items) {
            val o = JSONObject()
            o.put("title", item.title)
            o.put("source", item.source)
            o.put("date", item.date)
            o.put("imageUrl", item.imageUrl ?: "")
            o.put("articleUrl", item.articleUrl ?: "")
            o.put("description", item.description ?: "")
            o.put("contentPreview", item.contentPreview ?: "")
            arr.put(o)
        }
        prefs(context).edit().putString(KEY_ITEMS, arr.toString()).commit()
    }
}
