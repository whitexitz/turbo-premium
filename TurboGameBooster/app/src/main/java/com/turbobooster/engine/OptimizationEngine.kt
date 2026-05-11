package com.turbobooster.engine

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.turbobooster.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OptimizationEngine(private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val TAG = "OptimizationEngine"

    enum class ModoDesempenho { BALANCEADO, DESEMPENHO, ECONOMIA }

    data class ResultadoOtimizacao(
        val sucesso: Boolean,
        val mensagem: String,
        val detalhes: String = ""
    )

    suspend fun turboBoost(): ResultadoOtimizacao = withContext(Dispatchers.IO) {
        val resultados = mutableListOf<String>()

        val ramLiberada = matarBackground()
        resultados.add("RAM liberada: ${ramLiberada}MB")

        if (ShizukuManager.estaConectado) {
            desativarAnimacoes()
            resultados.add("Animações desativadas")
        }

        aplicarModoDesempenho(ModoDesempenho.DESEMPENHO)
        resultados.add("Modo desempenho ativado")

        ResultadoOtimizacao(
            sucesso = true,
            mensagem = "Turbo Boost aplicado!",
            detalhes = resultados.joinToString(" • ")
        )
    }

    suspend fun matarBackground(): Long = withContext(Dispatchers.IO) {
        val ramAntes = getRamUsada()

        try {
            if (ShizukuManager.estaConectado) {
                ShizukuManager.executarComando("am kill-all")
            } else {
                activityManager.killBackgroundProcesses("com.example.fake")
                val pacotes = activityManager.runningAppProcesses
                    ?.filter { it.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE }
                    ?.map { it.processName }
                    ?: emptyList()

                pacotes.forEach { pacote ->
                    try {
                        activityManager.killBackgroundProcesses(pacote)
                    } catch (e: Exception) { /* ignorar */ }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao matar background: ${e.message}")
        }

        val ramDepois = getRamUsada()
        maxOf(0L, ramAntes - ramDepois)
    }

    suspend fun limparCache(pacote: String? = null): ResultadoOtimizacao = withContext(Dispatchers.IO) {
        if (!ShizukuManager.estaConectado) {
            return@withContext ResultadoOtimizacao(
                sucesso = false,
                mensagem = "Shizuku necessário para limpeza de cache"
            )
        }

        return@withContext try {
            val comando = if (pacote != null) {
                "pm clear $pacote"
            } else {
                "pm trim-caches 1000G"
            }
            val resultado = ShizukuManager.executarComando(comando)
            ResultadoOtimizacao(
                sucesso = resultado.sucesso,
                mensagem = if (resultado.sucesso) "Cache limpo!" else "Falha ao limpar cache",
                detalhes = resultado.saida
            )
        } catch (e: Exception) {
            ResultadoOtimizacao(sucesso = false, mensagem = "Erro: ${e.message}")
        }
    }

    suspend fun desativarAnimacoes(): Boolean = withContext(Dispatchers.IO) {
        if (!ShizukuManager.estaConectado) return@withContext false
        val comandos = listOf(
            "settings put global window_animation_scale 0",
            "settings put global transition_animation_scale 0",
            "settings put global animator_duration_scale 0"
        )
        val resultados = ShizukuManager.executarComandos(comandos)
        resultados.all { it.sucesso }
    }

    suspend fun restaurarAnimacoes(): Boolean = withContext(Dispatchers.IO) {
        if (!ShizukuManager.estaConectado) return@withContext false
        val comandos = listOf(
            "settings put global window_animation_scale 1",
            "settings put global transition_animation_scale 1",
            "settings put global animator_duration_scale 1"
        )
        ShizukuManager.executarComandos(comandos).all { it.sucesso }
    }

    suspend fun aplicarModoDesempenho(modo: ModoDesempenho) = withContext(Dispatchers.IO) {
        if (!ShizukuManager.estaConectado) return@withContext

        when (modo) {
            ModoDesempenho.DESEMPENHO -> {
                ShizukuManager.executarComandos(listOf(
                    "settings put global low_power 0",
                    "setprop debug.sf.hw 1",
                    "setprop debug.egl.hw 1"
                ))
            }
            ModoDesempenho.ECONOMIA -> {
                ShizukuManager.executarComando("settings put global low_power 1")
            }
            ModoDesempenho.BALANCEADO -> {
                ShizukuManager.executarComandos(listOf(
                    "settings put global low_power 0",
                    "settings put global window_animation_scale 1",
                    "settings put global transition_animation_scale 1"
                ))
            }
        }
    }

    suspend fun listarProcessos(): List<ProcessoInfo> = withContext(Dispatchers.IO) {
        activityManager.runningAppProcesses?.map { proc ->
            ProcessoInfo(
                nome = proc.processName,
                pid = proc.pid,
                importancia = proc.importance,
                emBackground = proc.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE
            )
        } ?: emptyList()
    }

    suspend fun matarProcesso(pacote: String) = withContext(Dispatchers.IO) {
        try {
            if (ShizukuManager.estaConectado) {
                ShizukuManager.executarComando("am kill $pacote")
            }
            activityManager.killBackgroundProcesses(pacote)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao matar $pacote: ${e.message}")
        }
    }

    private fun getRamUsada(): Long {
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        return (info.totalMem - info.availMem) / (1024 * 1024)
    }
}

data class ProcessoInfo(
    val nome: String,
    val pid: Int,
    val importancia: Int,
    val emBackground: Boolean
)
