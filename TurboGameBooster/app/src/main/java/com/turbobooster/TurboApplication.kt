package com.turbobooster

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class TurboApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(listOf(
                NotificationChannel("overlay", "Overlay", NotificationManager.IMPORTANCE_LOW).apply {
                    setShowBadge(false)
                },
                NotificationChannel("detector", "Detector", NotificationManager.IMPORTANCE_LOW).apply {
                    setShowBadge(false)
                }
            ))
        }
    }
}
