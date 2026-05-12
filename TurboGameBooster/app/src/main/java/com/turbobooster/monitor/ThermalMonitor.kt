package com.turbobooster.monitor

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class ThermalMonitor {
    private val _temp = MutableStateFlow(0f)
    val temp: StateFlow<Float> = _temp

    private var job: Job? = null

    fun iniciar() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                _temp.value = lerTemp()
                delay(3000)
            }
        }
    }

    fun parar() { job?.cancel() }

    private fun lerTemp(): Float {
        val base = File("/sys/class/thermal")
        if (!base.exists()) return 0f
        val prioridade = listOf("cpu-thermal", "cpu", "soc_thermal")
        base.listFiles()?.forEach { zona ->
            val tipo = try { File(zona, "type").readText().trim().lowercase() } catch (e: Exception) { "" }
            if (prioridade.any { tipo.contains(it) }) {
                val t = lerValor(zona)
                if (t > 0) return t
            }
        }
        base.listFiles()?.sortedBy { it.name }?.forEach { zona ->
            val t = lerValor(zona)
            if (t in 20f..85f) return t
        }
        return 0f
    }

    private fun lerValor(zona: File): Float {
        return try {
            val v = File(zona, "temp").readText().trim().toFloat()
            if (v > 1000) v / 1000f else v
        } catch (e: Exception) { 0f }
    }
}
