package com.example.katzen.Helper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.firebase.messaging.FirebaseMessaging

object PortalFcmHelper {
    private const val TAG = "PortalFcmHelper"

    fun initPortalMessaging(activity: FragmentActivity, clienteId: String) {
        if (clienteId.isBlank()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 9101)
            }
        }
        val topic = portalTopic(clienteId)
        FirebaseMessaging.getInstance().unsubscribeFromTopic("portal_staff")
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                Log.d(TAG, if (task.isSuccessful) "Suscrito a $topic" else "Error FCM portal: ${task.exception?.message}")
            }
    }

    fun portalTopic(clienteId: String): String = "portal_cliente_${clienteId.replace(".", "_")}"

    fun clearPortalSubscription(clienteId: String) {
        if (clienteId.isBlank()) return
        FirebaseMessaging.getInstance().unsubscribeFromTopic(portalTopic(clienteId))
    }
}
