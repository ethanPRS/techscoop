package com.juanpabloramos.techscoop

import android.content.Context

object ArticleLists {
    fun withoutDismissed(context: Context, items: List<NewsItem>): List<NewsItem> {
        val dismissed = DismissedArticlesStore.getKeys(context)
        if (dismissed.isEmpty()) return items
        return items.filter { it.stableKey() !in dismissed }
    }
}
