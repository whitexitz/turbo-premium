package com.turbobooster.shizuku

import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess

object ShizukuManager {

    private const val TAG = "ShizukuManager"
    private const val REQUEST_CODE = 100

    private val _status = MutableStateFlow(ShizukuStatus.VERIFICANDO)
    val status: StateFlow<ShizukuStatus> = _status

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        _status.value = if (grantResult == PackageManager.PERMISSION_GRANTED) {
            ShizukuStatus.CONECTADO
        } else {
            ShizukuStatus.SEM_PERMISSAO
        }
    }

    private val binderListener = object : Shizuku.OnBinderReceivedListener {
        override fun onBinderReceived() {
            verificarStatus()
        }
    }

    private val binderDeadListener = object : Shizuku.OnBinderDeadListener {
        override fun onBinderDead() {
            _status.value = ShizukuStatus.NAO_DISPONIVEL
        }
    }

    fun inicializar() {
        Shizuku.addBinderReceivedListenerSticky(binderListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionListener)
        verificarStatus()
    }

    fun destruir() {
        Shizuku.removeBinderReceivedListener(binderListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionListener)
    }

    fun verificarStatus() {
        _status.value = try {
            when {
                !Shizuku.pingBinder() -> ShizukuStatus.NAO_DISPONIVEL
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> ShizukuStatus.CONECTADO
                Shizuku.shouldShowRequestPermissionRationale() -> ShizukuStatus.SEM_PERMISSAO
                else -> ShizukuStatus.SEM_PERMISSAO
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar Shizuku: ${e.message}")
            ShizukuStatus.NAO_DISPONIVEL
        }
    }

    fun solicitarPermissao() {
        try {
            Shizuku.requestPermission(REQUEST_CODE)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao solicitar permissão: ${e.message}")
            _status.value = ShizukuStatus.NAO_DISPONIVEL
        }
    }

    val estaConectado: Boolean
        get() = _status.value == ShizukuStatus.CONECTADO

    suspend fun executarComando(comando: String): ResultadoComando = withContext(Dispatchers.IO) {
        if (!estaConectado) {
            return@withContext ResultadoComando(
                sucesso = false,
                saida = "",
                erro = "Shizuku não conectado"
            )
        }

        return@withContext try {
            val processo = ShizukuRemoteProcess.exec(arrayOf("sh", "-c", comando), null, null)
            val saida = processo.inputStream.bufferedReader().readText()
            val erro = processo.errorStream.bufferedReader().readText()
            val codigoSaida = processo.waitFor()

            ResultadoComando(
                sucesso = codigoSaida == 0,
                saida = saida.trim(),
                erro = erro.trim()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao executar '$comando': ${e.message}")
            ResultadoComando(sucesso = false, saida = "", erro = e.message ?: "Erro desconhecido")
        }
    }

    suspend fun executarComandos(comandos: List<String>): List<ResultadoComando> {
        return comandos.map { executarComando(it) }
    }
}

enum class ShizukuStatus {
    VERIFICANDO,
    CONECTADO,
    SEM_PERMISSAO,
    NAO_DISPONIVEL
}

data class ResultadoComando(
    val sucesso: Boolean,
    val saida: String,
    val erro: String
)
