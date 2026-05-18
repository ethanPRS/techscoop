package com.juanpabloramos.techscoop

import android.content.Context

object DismissedArticlesStore {
    private const val PREFS = "techscoop_dismissed"
    private const val KEY_IDS = "dismissed_ids"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun add(context: Context, item: NewsItem) {
        val set = getKeys(context).toMutableSet()
        set.add(item.stableKey())
        prefs(context).edit().putStringSet(KEY_IDS, set).commit()
    }

    fun getKeys(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_IDS, emptySet()) ?: emptySet()
}
