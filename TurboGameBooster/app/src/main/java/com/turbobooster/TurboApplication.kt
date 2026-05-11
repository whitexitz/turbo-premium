package com.turbobooster

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class TurboApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        criarCanaisDeNotificacao()
    }

    private fun criarCanaisDeNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val canalDetector = NotificationChannel(
                "game_detector",
                "Detector de Jogos",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitora jogos abertos"
                setShowBadge(false)
            }

            val canalOverlay = NotificationChannel(
                "overlay_service",
                "Overlay em Jogo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Overlay de desempenho"
                setShowBadge(false)
            }

            manager.createNotificationChannels(listOf(canalDetector, canalOverlay))
        }
    }
}
