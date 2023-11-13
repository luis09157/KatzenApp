package com.example.katzen.Service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log

class DomiciliosPendientesService : Service() {
    val TIEMPO = 5000
    var handler = Handler()

    fun loop() {
        handler.postDelayed(object : Runnable {
            override fun run() {

                getDomicilios()
                handler.postDelayed(this, TIEMPO.toLong())
            }
        }, TIEMPO.toLong())
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Nah, not today. No binding here!
        return null
    }

    override fun onCreate() {
        super.onCreate()
        log("BackgroundTaskService is ready to conquer!")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        loop()
        return START_STICKY // If the service is killed, it will be automatically restarted
    }
    override fun onDestroy() {
        super.onDestroy()
        log("BackgroundTaskService says goodbye!")
    }

    fun log(str:String){
        Log.d("TAG", "log: $str")
    }

    fun getDomicilios(){
        log("Estamos en el loop")
    }
}