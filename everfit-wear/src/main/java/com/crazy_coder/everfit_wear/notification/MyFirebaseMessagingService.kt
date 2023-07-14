package com.crazy_coder.everfit_wear.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.crazy_coder.everfit_wear.R
import com.crazy_coder.everfit_wear.presentation.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val notificationManager by lazy { NotificationManagerCompat.from(applicationContext) }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        sendNotification(message)
    }

    private fun sendNotification(message: RemoteMessage) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(
            this, NOTIFICATION_ID/* Request code */, intent,
            pendingIntentFlags
        )
        Log.e("AAAAAAAAAAAAA", "${message.notification?.title}---- ${message.notification?.body}")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("AAAAAAAAAAAAA", "GRANT PERMISSION NOTIFICATION")
            return
        }
        notificationManager.notify(
            NOTIFICATION_ID, createNotification(
                message.notification?.title, message.notification?.body,
                addBuilder = {
                    setContentIntent(
                        pendingIntent
                    )
                }
            )
        )
    }

    private fun createNotificationChannelIfAboveAndroidO() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                applicationContext.getString(R.string.notification_message_channel_id),
                applicationContext.getString(R.string.notification_message_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = applicationContext.getString(
                    R.string.notification_message_channel_description
                )
            }.let { notificationManager.createNotificationChannel(it) }
        }
    }

    private fun createNotification(
        title: String?,
        content: String?,
        addBuilder: NotificationCompat.Builder.() -> NotificationCompat.Builder,
    ): Notification {
        createNotificationChannelIfAboveAndroidO()
        return NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.notification_message_channel_id)
        ).setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_app_notification)
            .setColor(applicationContext.resources.getColor(R.color.colorPrimary))
            .setContentText(content)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .run(addBuilder)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 100
    }
}