package com.turbobooster.shizuku

import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

enum class ShizukuStatus { VERIFICANDO, CONECTADO, SEM_PERMISSAO, INDISPONIVEL }

data class CmdResult(val ok: Boolean, val saida: String = "", val erro: String = "")

object ShizukuManager {
    private val _status = MutableStateFlow(ShizukuStatus.VERIFICANDO)
    val status: StateFlow<ShizukuStatus> = _status

    private val binderListener = Shizuku.OnBinderReceivedListener { verificar() }
    private val binderDead = Shizuku.OnBinderDeadListener { _status.value = ShizukuStatus.INDISPONIVEL }
    private val permListener = Shizuku.OnRequestPermissionResultListener { _, result ->
        _status.value = if (result == PackageManager.PERMISSION_GRANTED)
            ShizukuStatus.CONECTADO else ShizukuStatus.SEM_PERMISSAO
    }

    fun init() {
        Shizuku.addBinderReceivedListenerSticky(binderListener)
        Shizuku.addBinderDeadListener(binderDead)
        Shizuku.addRequestPermissionResultListener(permListener)
        verificar()
    }

    fun destroy() {
        Shizuku.removeBinderReceivedListener(binderListener)
        Shizuku.removeBinderDeadListener(binderDead)
        Shizuku.removeRequestPermissionResultListener(permListener)
    }

    fun verificar() {
        _status.value = try {
            when {
                !Shizuku.pingBinder() -> ShizukuStatus.INDISPONIVEL
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> ShizukuStatus.CONECTADO
                else -> ShizukuStatus.SEM_PERMISSAO
            }
        } catch (e: Exception) { ShizukuStatus.INDISPONIVEL }
    }

    fun solicitarPermissao() = try { Shizuku.requestPermission(100) } catch (e: Exception) {}

    val conectado get() = _status.value == ShizukuStatus.CONECTADO

    suspend fun cmd(comando: String): CmdResult = withContext(Dispatchers.IO) {
        if (!conectado) return@withContext CmdResult(false, erro = "Shizuku não conectado")
        try {
            val p = Runtime.getRuntime().exec(arrayOf("sh", "-c", comando))
            val saida = p.inputStream.bufferedReader().readText()
            val erro = p.errorStream.bufferedReader().readText()
            CmdResult(p.waitFor() == 0, saida.trim(), erro.trim())
        } catch (e: Exception) {
            CmdResult(false, erro = e.message ?: "Erro")
        }
    }
}
