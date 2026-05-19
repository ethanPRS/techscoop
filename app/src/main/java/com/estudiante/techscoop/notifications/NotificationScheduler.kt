package com.estudiante.techscoop.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun schedule(context: Context) {
        val newsWork = PeriodicWorkRequestBuilder<NewsCheckWorker>(12, TimeUnit.HOURS).build()
        val reminderWork = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "news_check",
            ExistingPeriodicWorkPolicy.KEEP,
            newsWork
        )
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "app_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWork
        )
    }
}
