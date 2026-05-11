package com.turbobooster.monitor

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class ThermalMonitor {

    private val _dadosThermal = MutableStateFlow(DadosThermal())
    val dadosThermal: StateFlow<DadosThermal> = _dadosThermal

    private var job: Job? = null

    private val LIMITE_ATENCAO = 43f
    private val LIMITE_PERIGO = 50f

    data class DadosThermal(
        val temperaturaCelsius: Float = 0f,
        val status: StatusThermal = StatusThermal.NORMAL,
        val zonaUsada: String = ""
    )

    enum class StatusThermal { NORMAL, ATENCAO, PERIGO }

    fun iniciar(intervaloMs: Long = 3000L) {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                try {
                    val (temp, zona) = lerTemperatura()
                    val status = when {
                        temp >= LIMITE_PERIGO -> StatusThermal.PERIGO
                        temp >= LIMITE_ATENCAO -> StatusThermal.ATENCAO
                        else -> StatusThermal.NORMAL
                    }
                    _dadosThermal.value = DadosThermal(temp, status, zona)
                } catch (e: Exception) {
                    Log.e("ThermalMonitor", "Erro: ${e.message}")
                }
                delay(intervaloMs)
            }
        }
    }

    fun parar() {
        job?.cancel()
        job = null
    }

    private fun lerTemperatura(): Pair<Float, String> {
        val basePath = File("/sys/class/thermal")
        if (!basePath.exists()) return Pair(0f, "")

        val prioridadeZonas = listOf("cpu-thermal", "cpu", "soc_thermal", "tsens_tz_sensor0")

        basePath.listFiles()?.forEach { zona ->
            val tipoArquivo = File(zona, "type")
            if (tipoArquivo.exists()) {
                val tipo = tipoArquivo.readText().trim().lowercase()
                if (prioridadeZonas.any { tipo.contains(it) }) {
                    val temp = lerTempZona(zona)
                    if (temp > 0) return Pair(temp, tipo)
                }
            }
        }

        basePath.listFiles()?.sortedBy { it.name }?.forEach { zona ->
            val temp = lerTempZona(zona)
            if (temp in 20f..85f) return Pair(temp, zona.name)
        }

        return Pair(0f, "")
    }

    private fun lerTempZona(zona: File): Float {
        return try {
            val tempArquivo = File(zona, "temp")
            if (!tempArquivo.exists()) return 0f
            val valor = tempArquivo.readText().trim().toFloat()
            if (valor > 1000) valor / 1000f else valor
        } catch (e: Exception) { 0f }
    }
}
