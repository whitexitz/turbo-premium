package com.turbobooster.engine

import android.app.ActivityManager
import android.content.Context
import com.turbobooster.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OptimizationEngine(private val context: Context) {
    private val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    data class BoostResult(val ramMB: Long, val processos: Int, val animacoes: Boolean)

    suspend fun turboBoost(): BoostResult = withContext(Dispatchers.IO) {
        val ramAntes = getRamUsada()
        var processos = 0
        var animacoes = false

        am.runningAppProcesses?.filter {
            it.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE
        }?.forEach {
            try { am.killBackgroundProcesses(it.processName); processos++ } catch (e: Exception) {}
        }

        if (ShizukuManager.conectado) {
            ShizukuManager.cmd("am kill-all")
            ShizukuManager.cmd("settings put global window_animation_scale 0")
            ShizukuManager.cmd("settings put global transition_animation_scale 0")
            ShizukuManager.cmd("settings put global animator_duration_scale 0")
            animacoes = true
        }

        val ramDepois = getRamUsada()
        BoostResult(maxOf(0, ramAntes - ramDepois), processos, animacoes)
    }

    suspend fun restaurar() = withContext(Dispatchers.IO) {
        if (ShizukuManager.conectado) {
            ShizukuManager.cmd("settings put global window_animation_scale 1")
            ShizukuManager.cmd("settings put global transition_animation_scale 1")
            ShizukuManager.cmd("settings put global animator_duration_scale 1")
        }
    }

    suspend fun limparCache(): String = withContext(Dispatchers.IO) {
        if (!ShizukuManager.conectado) return@withContext "Shizuku necessário"
        val r = ShizukuManager.cmd("pm trim-caches 1000G")
        if (r.ok) "Cache limpo!" else "Falha: ${r.erro}"
    }

    private fun getRamUsada(): Long {
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        return (info.totalMem - info.availMem) / (1024 * 1024)
    }
}
