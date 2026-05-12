package com.turbobooster.monitor

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class CpuMonitor {
    private val _uso = MutableStateFlow(0f)
    val uso: StateFlow<Float> = _uso

    private val _frequencia = MutableStateFlow(0)
    val frequencia: StateFlow<Int> = _frequencia

    private var job: Job? = null
    private var idleAnterior = 0L
    private var totalAnterior = 0L

    fun iniciar() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                _uso.value = calcularUso()
                _frequencia.value = lerFrequencia()
                delay(1000)
            }
        }
    }

    fun parar() { job?.cancel() }

    private fun calcularUso(): Float {
        return try {
            val linha = File("/proc/stat").readLines().first()
            val p = linha.split("\\s+".toRegex()).drop(1).map { it.toLong() }
            if (p.size < 8) return 0f
            val idle = p[3] + p[4]
            val total = p.take(8).sum()
            val dIdle = idle - idleAnterior
            val dTotal = total - totalAnterior
            idleAnterior = idle
            totalAnterior = total
            if (dTotal <= 0) 0f else ((dTotal - dIdle).toFloat() / dTotal) * 100f
        } catch (e: Exception) { 0f }
    }

    private fun lerFrequencia(): Int {
        return try {
            File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
                .readText().trim().toInt() / 1000
        } catch (e: Exception) { 0 }
    }
}
