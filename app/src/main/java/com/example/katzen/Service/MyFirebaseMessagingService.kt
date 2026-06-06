package com.example.katzen.Service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.katzen.MainActivity
import com.example.katzen.PortalMainActivity
import com.example.katzen.Helper.PortalDeepLinkHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ninodev.katzen.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        val data = remoteMessage.data
        val title = remoteMessage.notification?.title
            ?: data["title"]
            ?: getString(R.string.app_name)
        val body = remoteMessage.notification?.body
            ?: data["body"]
            ?: data["message"]

        if (body.isNullOrBlank()) {
            Log.w(TAG, "Mensaje FCM sin contenido visible")
            return
        }

        val target = data["target"]
        val openPortal = target == "portal" || remoteMessage.from?.contains("portal_cliente") == true
        sendNotification(title, body, openPortal, data)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "FCM token actualizado")
    }

    private fun sendNotification(
        title: String,
        messageBody: String,
        openPortal: Boolean,
        data: Map<String, String>
    ) {
        ensureNotificationChannel()

        val activityClass = if (openPortal) PortalMainActivity::class.java else MainActivity::class.java
        val intent = Intent(this, activityClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (openPortal) {
                PortalDeepLinkHelper.putExtras(
                    intent = this,
                    clienteId = data["clienteId"].orEmpty(),
                    tipo = data["tipo"].orEmpty(),
                    mascotaId = data["mascotaId"].orEmpty(),
                    referenciaId = data["referenciaId"].orEmpty()
                )
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            if (openPortal) 1 else 0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo_katzen)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channelId = getString(R.string.default_notification_channel_id)
        val channel = NotificationChannel(
            channelId,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val TAG = "KatzenFCM"
    }
}
