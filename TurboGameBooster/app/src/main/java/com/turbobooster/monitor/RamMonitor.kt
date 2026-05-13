package com.turbobooster.monitor

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RamMonitor(private val context: Context) {
    data class DadosRam(
        val totalMB: Long = 0,
        val usadaMB: Long = 0,
        val livresMB: Long = 0,
        val percentual: Float = 0f
    )

    private val _dados = MutableStateFlow(DadosRam())
    val dados: StateFlow<DadosRam> = _dados

    private var job: Job? = null
    private val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun iniciar() {
        Log.d("RamMonitor", "iniciando monitor de RAM")
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val info = ActivityManager.MemoryInfo()
                    am.getMemoryInfo(info)
                    val total = info.totalMem / (1024 * 1024)
                    val livre = info.availMem / (1024 * 1024)
                    val usada = total - livre
                    val pct = if (total > 0) (usada.toFloat() / total) * 100f else 0f
                    _dados.value = DadosRam(total, usada, livre, pct)
                    Log.d("RamMonitor", "total=${total}MB usada=${usada}MB livre=${livre}MB pct=${"%.1f".format(pct)}%")
                } catch (e: Exception) {
                    Log.e("RamMonitor", "erro ao ler RAM: ${e.message}")
                }
                delay(2000)
            }
        }
    }

    fun parar() {
        Log.d("RamMonitor", "parando monitor de RAM")
        job?.cancel()
    }
}
