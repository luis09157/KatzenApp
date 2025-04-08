package com.example.katzen.Service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ninodev.katzen.R

class Notificador(private val context: Context) {

    private val channelId = "canal_notificaciones"
    private val channelName = "Notificaciones Básicas"

    init {
        crearCanalDeNotificacion()
    }

    private fun crearCanalDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para notificaciones simples"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    fun mostrarNotificacion(titulo: String, mensaje: String) {
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.logo)
        val notificacion = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo_katzen)
            .setLargeIcon(largeIcon)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), notificacion)
            }
        } catch (e: SecurityException) {
            e.printStackTrace() // Maneja el caso si no hay permiso
        }
    }
}
