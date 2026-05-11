package com.turbobooster.monitor

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class RamMonitor(private val context: Context) {

    private val _dadosRam = MutableStateFlow(DadosRam())
    val dadosRam: StateFlow<DadosRam> = _dadosRam

    private var job: Job? = null
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    data class DadosRam(
        val totalMB: Long = 0,
        val usadaMB: Long = 0,
        val livresMB: Long = 0,
        val percentualUso: Float = 0f,
        val baixaMemoria: Boolean = false,
        val swapTotalMB: Long = 0,
        val swapUsadaMB: Long = 0
    )

    fun iniciar(intervaloMs: Long = 2000L) {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val info = ActivityManager.MemoryInfo()
                    activityManager.getMemoryInfo(info)

                    val totalMB = info.totalMem / (1024 * 1024)
                    val livresMB = info.availMem / (1024 * 1024)
                    val usadaMB = totalMB - livresMB
                    val percentual = (usadaMB.toFloat() / totalMB) * 100f

                    val (swapTotal, swapUsada) = lerSwap()

                    _dadosRam.value = DadosRam(
                        totalMB = totalMB,
                        usadaMB = usadaMB,
                        livresMB = livresMB,
                        percentualUso = percentual,
                        baixaMemoria = info.lowMemory,
                        swapTotalMB = swapTotal,
                        swapUsadaMB = swapUsada
                    )
                } catch (e: Exception) {
                    // Mantém último valor
                }
                delay(intervaloMs)
            }
        }
    }

    fun parar() {
        job?.cancel()
        job = null
    }

    private fun lerSwap(): Pair<Long, Long> {
        return try {
            var swapTotal = 0L
            var swapFree = 0L
            File("/proc/meminfo").readLines().forEach { linha ->
                when {
                    linha.startsWith("SwapTotal:") ->
                        swapTotal = linha.split("\\s+".toRegex())[1].toLong() / 1024
                    linha.startsWith("SwapFree:") ->
                        swapFree = linha.split("\\s+".toRegex())[1].toLong() / 1024
                }
            }
            Pair(swapTotal, swapTotal - swapFree)
        } catch (e: Exception) {
            Pair(0L, 0L)
        }
    }
}
