package com.turbobooster.monitor

import android.app.ActivityManager
import android.content.Context
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
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val info = ActivityManager.MemoryInfo()
                am.getMemoryInfo(info)
                val total = info.totalMem / (1024 * 1024)
                val livre = info.availMem / (1024 * 1024)
                val usada = total - livre
                _dados.value = DadosRam(total, usada, livre, (usada.toFloat() / total) * 100f)
                delay(2000)
            }
        }
    }

    fun parar() { job?.cancel() }
}
