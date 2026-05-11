package com.turbobooster.service

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.turbobooster.engine.OptimizationEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

class GameDetectorService : Service() {

    companion object {
        val jogoAtivo = MutableStateFlow<String?>(null)

        val JOGOS_CONHECIDOS = mapOf(
            "com.dts.freefireth" to "Free Fire",
            "com.dts.freefiremax" to "Free Fire MAX",
            "com.tencent.ig" to "PUBG Mobile",
            "com.activision.callofduty.shooter" to "Call of Duty Mobile",
            "com.garena.game.codm" to "COD Mobile",
            "com.mobile.legends" to "Mobile Legends",
            "com.miHoYo.GenshinImpact" to "Genshin Impact",
            "com.supercell.clashofclans" to "Clash of Clans",
            "com.riotgames.league.wildrift" to "Wild Rift",
            "com.epicgames.fortnite" to "Fortnite"
        )
    }

    private val escopo = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var optimizationEngine: OptimizationEngine
    private var ultimoApp = ""

    override fun onCreate() {
        super.onCreate()
        optimizationEngine = OptimizationEngine(this)
        iniciarComoForeground()
        iniciarDeteccao()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        escopo.cancel()
        super.onDestroy()
    }

    private fun iniciarComoForeground() {
        val notificacao = NotificationCompat.Builder(this, "game_detector")
            .setContentTitle("Turbo Booster ativo")
            .setContentText("Monitorando jogos...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()

        startForeground(1, notificacao)
    }

    private fun iniciarDeteccao() {
        escopo.launch {
            val usageManager = getSystemService(USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return@launch

            while (isActive) {
                try {
                    val agora = System.currentTimeMillis()
                    val stats = usageManager.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        agora - 10000,
                        agora
                    )

                    val appAtual = stats
                        ?.maxByOrNull { it.lastTimeUsed }
                        ?.packageName ?: ""

                    if (appAtual != ultimoApp) {
                        val eraJogo = JOGOS_CONHECIDOS.containsKey(ultimoApp)
                        val eJogo = JOGOS_CONHECIDOS.containsKey(appAtual)

                        when {
                            eJogo && !eraJogo -> aoAbrirJogo(appAtual)
                            eraJogo && !eJogo -> aoFecharJogo(ultimoApp)
                        }

                        ultimoApp = appAtual
                    }
                } catch (e: Exception) {
                    Log.e("GameDetector", "Erro: ${e.message}")
                }
                delay(2000L)
            }
        }
    }

    private suspend fun aoAbrirJogo(pacote: String) {
        val nomeJogo = JOGOS_CONHECIDOS[pacote] ?: pacote
        Log.i("GameDetector", "Jogo detectado: $nomeJogo")
        jogoAtivo.value = pacote
        optimizationEngine.turboBoost()
    }

    private suspend fun aoFecharJogo(pacote: String) {
        Log.i("GameDetector", "Jogo fechado: $pacote")
        jogoAtivo.value = null
        optimizationEngine.restaurarAnimacoes()
    }
}
