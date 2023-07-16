package com.crazy_coder.everfit_wear.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.crazy_coder.everfit_wear.presentation.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val notification by lazy { AppNotificationManager(applicationContext) }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("AAAAAA", "onMessageReceived: $message")
        notification.showNotification(message.notification?.body ?: "Message null")
    }

    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d("AAAAAAAAA", "sendRegistrationTokenToServer($token)")
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
    }

    companion object {
        const val NOTIFICATION_ID = 100
    }
}