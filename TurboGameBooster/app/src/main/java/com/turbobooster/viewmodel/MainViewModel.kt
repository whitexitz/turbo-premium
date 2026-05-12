package com.turbobooster.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.turbobooster.data.AppState
import com.turbobooster.engine.OptimizationEngine
import com.turbobooster.monitor.*
import com.turbobooster.service.OverlayService
import com.turbobooster.shizuku.ShizukuManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val engine = OptimizationEngine(app)
    private val cpu = CpuMonitor()
    private val ram = RamMonitor(app)
    private val thermal = ThermalMonitor()
    private val fps = FpsMonitor()

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state

    private val _overlayAtivo = MutableStateFlow(false)
    val overlayAtivo: StateFlow<Boolean> = _overlayAtivo

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem: StateFlow<String?> = _mensagem

    init {
        cpu.iniciar(); ram.iniciar(); thermal.iniciar(); fps.iniciar()
        coletarDados()
    }

    private fun coletarDados() {
        viewModelScope.launch {
            combine(
                cpu.uso, ram.dados, thermal.temp, fps.fps, cpu.frequencia
            ) { c, r, t, f, freq ->
                _state.value.copy(
                    cpu = c, ram = r.percentual,
                    ramUsadaMB = r.usadaMB, ramTotalMB = r.totalMB,
                    temp = t, fps = f, cpuFreq = freq,
                    historicoCpu = (_state.value.historicoCpu + c).takeLast(30),
                    historicoRam = (_state.value.historicoRam + r.percentual).takeLast(30),
                    historicoFps = (_state.value.historicoFps + f).takeLast(30),
                    historicoTemp = (_state.value.historicoTemp + t).takeLast(30)
                )
            }.collect { _state.value = it }
        }
    }

    fun turboBoost() {
        viewModelScope.launch {
            _state.value = _state.value.copy(otimizando = true)
            val resultado = engine.turboBoost()
            _state.value = _state.value.copy(
                otimizando = false,
                turboAtivo = true,
                ultimoBoost = resultado
            )
            mostrarMensagem("RAM: +${resultado.ramMB}MB • ${resultado.processos} processos encerrados")
        }
    }

    fun limparCache() {
        viewModelScope.launch {
            val r = engine.limparCache()
            mostrarMensagem(r)
        }
    }

    fun liberarRam() {
        viewModelScope.launch {
            val r = engine.turboBoost()
            mostrarMensagem("RAM liberada: ${r.ramMB}MB")
        }
    }

    fun matarBackground() {
        viewModelScope.launch {
            val r = engine.turboBoost()
            mostrarMensagem("${r.processos} processos encerrados")
        }
    }

    fun restaurar() {
        viewModelScope.launch {
            engine.restaurar()
            _state.value = _state.value.copy(turboAtivo = false, ultimoBoost = null)
            mostrarMensagem("Sistema restaurado")
        }
    }

    fun toggleOverlay() {
        val ctx = getApplication<Application>()
        if (_overlayAtivo.value) {
            ctx.stopService(Intent(ctx, OverlayService::class.java))
            _overlayAtivo.value = false
        } else {
            ctx.startForegroundService(Intent(ctx, OverlayService::class.java))
            _overlayAtivo.value = true
        }
    }

    private suspend fun mostrarMensagem(msg: String) {
        _mensagem.value = msg
        delay(4000)
        _mensagem.value = null
    }

    override fun onCleared() {
        cpu.parar(); ram.parar(); thermal.parar(); fps.parar()
        super.onCleared()
    }
}
