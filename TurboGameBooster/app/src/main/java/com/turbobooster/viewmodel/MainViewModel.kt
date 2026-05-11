package com.turbobooster.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.turbobooster.engine.OptimizationEngine
import com.turbobooster.engine.ProcessoInfo
import com.turbobooster.monitor.*
import com.turbobooster.shizuku.ShizukuManager
import com.turbobooster.shizuku.ShizukuStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val engine = OptimizationEngine(app)

    val cpuMonitor = CpuMonitor()
    val ramMonitor = RamMonitor(app)
    val thermalMonitor = ThermalMonitor()
    val fpsMonitor = FpsMonitor()

    val statusShizuku: StateFlow<ShizukuStatus> = ShizukuManager.status

    private val _turboAtivo = MutableStateFlow(false)
    val turboAtivo: StateFlow<Boolean> = _turboAtivo

    private val _otimizando = MutableStateFlow(false)
    val otimizando: StateFlow<Boolean> = _otimizando

    private val _mensagemResultado = MutableStateFlow<String?>(null)
    val mensagemResultado: StateFlow<String?> = _mensagemResultado

    private val _modoDesempenho = MutableStateFlow(OptimizationEngine.ModoDesempenho.BALANCEADO)
    val modoDesempenho: StateFlow<OptimizationEngine.ModoDesempenho> = _modoDesempenho

    private val _processos = MutableStateFlow<List<ProcessoInfo>>(emptyList())
    val processos: StateFlow<List<ProcessoInfo>> = _processos

    private val _historicoCpu = MutableStateFlow(List(60) { 0f })
    val historicoCpu: StateFlow<List<Float>> = _historicoCpu

    private val _historicoRam = MutableStateFlow(List(60) { 0f })
    val historicoRam: StateFlow<List<Float>> = _historicoRam

    private val _historicoFps = MutableStateFlow(List(60) { 0 })
    val historicoFps: StateFlow<List<Int>> = _historicoFps

    private val _historicoTemp = MutableStateFlow(List(60) { 0f })
    val historicoTemp: StateFlow<List<Float>> = _historicoTemp

    init {
        iniciarMonitores()
        iniciarHistorico()
    }

    private fun iniciarMonitores() {
        cpuMonitor.iniciar()
        ramMonitor.iniciar()
        thermalMonitor.iniciar()
        fpsMonitor.iniciar()
    }

    private fun iniciarHistorico() {
        viewModelScope.launch {
            cpuMonitor.dadosCpu.collect { cpu ->
                _historicoCpu.value = (_historicoCpu.value + cpu.usoTotal).takeLast(60)
            }
        }
        viewModelScope.launch {
            ramMonitor.dadosRam.collect { ram ->
                _historicoRam.value = (_historicoRam.value + ram.percentualUso).takeLast(60)
            }
        }
        viewModelScope.launch {
            fpsMonitor.fps.collect { fps ->
                _historicoFps.value = (_historicoFps.value + fps).takeLast(60)
            }
        }
        viewModelScope.launch {
            thermalMonitor.dadosThermal.collect { thermal ->
                _historicoTemp.value = (_historicoTemp.value + thermal.temperaturaCelsius).takeLast(60)
            }
        }
    }

    fun aplicarTurboBoost() {
        viewModelScope.launch {
            _otimizando.value = true
            val resultado = engine.turboBoost()
            _turboAtivo.value = true
            _mensagemResultado.value = resultado.detalhes
            _otimizando.value = false
            delay(5000)
            _mensagemResultado.value = null
        }
    }

    fun aplicarModo(modo: OptimizationEngine.ModoDesempenho) {
        _modoDesempenho.value = modo
        viewModelScope.launch {
            engine.aplicarModoDesempenho(modo)
        }
    }

    fun limparCache() {
        viewModelScope.launch {
            val resultado = engine.limparCache()
            _mensagemResultado.value = resultado.mensagem
            delay(3000)
            _mensagemResultado.value = null
        }
    }

    fun liberarRam() {
        viewModelScope.launch {
            val liberada = engine.matarBackground()
            _mensagemResultado.value = "RAM liberada: ${liberada}MB"
            delay(3000)
            _mensagemResultado.value = null
        }
    }

    fun carregarProcessos() {
        viewModelScope.launch {
            _processos.value = engine.listarProcessos()
        }
    }

    fun matarProcesso(pacote: String) {
        viewModelScope.launch {
            engine.matarProcesso(pacote)
            delay(500)
            carregarProcessos()
        }
    }

    override fun onCleared() {
        cpuMonitor.parar()
        ramMonitor.parar()
        thermalMonitor.parar()
        fpsMonitor.parar()
        super.onCleared()
    }
}
