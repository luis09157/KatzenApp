package com.example.katzen.Helper

import android.app.Application
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

object FirebaseMonitoringHelper {

    private var initialized = false

    fun init(application: Application) {
        if (initialized) return
        initialized = true

        Firebase.analytics.setAnalyticsCollectionEnabled(true)
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
        Firebase.crashlytics.log("KatzenApp iniciada")
    }

    fun setUserId(userId: String?) {
        if (userId.isNullOrBlank()) return
        Firebase.analytics.setUserId(userId)
        Firebase.crashlytics.setUserId(userId)
    }

    fun logScreen(screenName: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }

    fun logLogin(success: Boolean) {
        Firebase.analytics.logEvent(
            if (success) "login_success" else "login_failed",
            null
        )
        Firebase.crashlytics.log(if (success) "login_success" else "login_failed")
    }

    fun logLogout() {
        Firebase.analytics.logEvent("logout", null)
        Firebase.crashlytics.log("logout")
    }

    fun recordError(message: String, throwable: Throwable? = null) {
        Firebase.crashlytics.log(message)
        throwable?.let { Firebase.crashlytics.recordException(it) }
    }
}
