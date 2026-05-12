package com.turbobooster.data

import com.turbobooster.engine.OptimizationEngine

data class AppState(
    val cpu: Float = 0f,
    val ram: Float = 0f,
    val ramUsadaMB: Long = 0,
    val ramTotalMB: Long = 0,
    val temp: Float = 0f,
    val fps: Int = 0,
    val cpuFreq: Int = 0,
    val turboAtivo: Boolean = false,
    val otimizando: Boolean = false,
    val ultimoBoost: OptimizationEngine.BoostResult? = null,
    val historicoCpu: List<Float> = List(30) { 0f },
    val historicoRam: List<Float> = List(30) { 0f },
    val historicoFps: List<Int> = List(30) { 0 },
    val historicoTemp: List<Float> = List(30) { 0f }
)
