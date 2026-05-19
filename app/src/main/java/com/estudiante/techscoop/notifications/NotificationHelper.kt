package com.estudiante.techscoop.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.estudiante.techscoop.R
import com.estudiante.techscoop.ui.MainActivity

object NotificationHelper {

    private const val CHANNEL_NEWS = "techscoop_news"
    private const val CHANNEL_REMINDER = "techscoop_reminder"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_NEWS,
                context.getString(R.string.notif_channel_news),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDER,
                context.getString(R.string.notif_channel_reminder),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    fun showNews(context: Context, body: String) {
        notify(
            context,
            CHANNEL_NEWS,
            1001,
            context.getString(R.string.notif_news_title),
            body
        )
    }

    fun showReminder(context: Context) {
        notify(
            context,
            CHANNEL_REMINDER,
            1002,
            context.getString(R.string.notif_reminder_title),
            context.getString(R.string.notif_reminder_body)
        )
    }

    private fun notify(context: Context, channel: String, id: Int, title: String, body: String) {
        createChannels(context)
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(id, notification)
    }
}
