package com.turbobooster.monitor

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class CpuMonitor {

    private val _dadosCpu = MutableStateFlow(DadosCpu())
    val dadosCpu: StateFlow<DadosCpu> = _dadosCpu

    private var job: Job? = null
    private var leituraAnterior: LeituraBruta? = null

    data class DadosCpu(
        val usoTotal: Float = 0f,
        val usoPorCore: List<Float> = emptyList(),
        val frequenciaAtualMHz: Int = 0,
        val frequenciaMaxMHz: Int = 0,
        val numeroCores: Int = Runtime.getRuntime().availableProcessors()
    )

    private data class LeituraBruta(val idle: Long, val total: Long)

    fun iniciar(intervaloMs: Long = 1000L) {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val uso = calcularUsoCpu()
                    val freq = lerFrequencia()
                    val freqMax = lerFrequenciaMaxima()

                    _dadosCpu.value = DadosCpu(
                        usoTotal = uso,
                        frequenciaAtualMHz = freq,
                        frequenciaMaxMHz = freqMax,
                        numeroCores = Runtime.getRuntime().availableProcessors()
                    )
                } catch (e: Exception) {
                    Log.e("CpuMonitor", "Erro: ${e.message}")
                }
                delay(intervaloMs)
            }
        }
    }

    fun parar() {
        job?.cancel()
        job = null
    }

    private fun calcularUsoCpu(): Float {
        return try {
            val linha = File("/proc/stat").readLines().firstOrNull() ?: return 0f
            val partes = linha.split("\\s+".toRegex()).drop(1).map { it.toLong() }
            if (partes.size < 8) return 0f

            val idle = partes[3] + partes[4]
            val total = partes.take(8).sum()

            val leituraAtual = LeituraBruta(idle, total)
            val anterior = leituraAnterior

            leituraAnterior = leituraAtual

            if (anterior == null) return 0f

            val deltaIdle = leituraAtual.idle - anterior.idle
            val deltaTotal = leituraAtual.total - anterior.total

            if (deltaTotal <= 0) return 0f
            ((deltaTotal - deltaIdle).toFloat() / deltaTotal) * 100f
        } catch (e: Exception) {
            0f
        }
    }

    private fun lerFrequencia(): Int {
        return try {
            val arquivo = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (arquivo.exists()) {
                arquivo.readText().trim().toInt() / 1000
            } else 0
        } catch (e: Exception) { 0 }
    }

    private fun lerFrequenciaMaxima(): Int {
        return try {
            val arquivo = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            if (arquivo.exists()) {
                arquivo.readText().trim().toInt() / 1000
            } else 0
        } catch (e: Exception) { 0 }
    }
}
