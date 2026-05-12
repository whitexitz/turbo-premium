package com.turbobooster.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class GameDetectorService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(3, NotificationCompat.Builder(this, "detector")
            .setContentTitle("Detector ativo")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true).build())
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
