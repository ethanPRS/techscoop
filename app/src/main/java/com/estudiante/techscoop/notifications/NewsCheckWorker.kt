package com.estudiante.techscoop.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.estudiante.techscoop.repository.ApiResult
import com.estudiante.techscoop.repository.ArticleRepository

class NewsCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)
        val lastTitle = prefs.getString("last_news_title", null)

        when (val result = ArticleRepository().getNews()) {
            is ApiResult.Success -> {
                val first = result.articles.firstOrNull()?.title ?: return Result.success()
                if (lastTitle != null && first != lastTitle) {
                    NotificationHelper.showNews(applicationContext, first)
                }
                prefs.edit().putString("last_news_title", first).apply()
            }
            else -> Unit
        }
        return Result.success()
    }
}
