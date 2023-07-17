package com.crazy_coder.everfit_wear.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.MainThread
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.crazy_coder.everfit_wear.R
import com.crazy_coder.everfit_wear.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@MainThread
@Singleton
class AppNotificationManager @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
) {
    private val notificationManager by lazy(LazyThreadSafetyMode.NONE) {
        NotificationManagerCompat.from(applicationContext)
    }

    fun showNotification(contentText: String) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(
                NOTIFICATION_ID,
                createNotification(contentText)
            )
        }
    }


    fun cancelNotification() = notificationManager.cancel(NOTIFICATION_ID)

    fun createNotification(contentText: String): Notification {
        fun createNotificationChannelIfAboveAndroidO() {
            NotificationChannel(
                applicationContext.getString(R.string.default_notification_channel_id),
                applicationContext.getString(R.string.notification_message_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    applicationContext.getString(R.string.notification_message_channel_description)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(null, null)
            }.let { notificationManager.createNotificationChannel(it) }
        }

        createNotificationChannelIfAboveAndroidO()

        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT.let {
            it or PendingIntent.FLAG_IMMUTABLE
        }
        return NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.default_notification_channel_id)
        )
            .setAutoCancel(true)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(null)
            .setSilent(true)
            .setVibrate(null)
            .setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    Intent(applicationContext, MainActivity::class.java),
                    pendingIntentFlags
                )
            )
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 10000
        const val NOTIFICATION_ACTION = "NOTIFICATION_ACTION"
        const val STOP_SERVICE = "STOP_SERVICE"
    }
}
