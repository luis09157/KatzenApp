package com.example.katzen

import android.app.Application
import com.example.katzen.Helper.FirebaseMonitoringHelper
import com.google.firebase.FirebaseApp

class KatzenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseMonitoringHelper.init(this)
    }
}
