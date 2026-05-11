package com.turbobooster.monitor

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FpsMonitor {

    private val _fps = MutableStateFlow(0)
    val fps: StateFlow<Int> = _fps

    private val _frameDrops = MutableStateFlow(0)
    val frameDrops: StateFlow<Int> = _frameDrops

    private var frameCount = 0
    private var frameDropCount = 0
    private var ultimoTempoNs = 0L
    private var ativo = false

    private val handler = Handler(Looper.getMainLooper())

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!ativo) return

            if (ultimoTempoNs != 0L) {
                val delta = frameTimeNanos - ultimoTempoNs
                if (delta > 25_000_000L) {
                    frameDropCount++
                }
            }

            ultimoTempoNs = frameTimeNanos
            frameCount++

            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    private val calculadorFps = object : Runnable {
        override fun run() {
            if (!ativo) return
            _fps.value = frameCount
            _frameDrops.value = frameDropCount
            frameCount = 0
            frameDropCount = 0
            handler.postDelayed(this, 1000L)
        }
    }

    fun iniciar() {
        if (ativo) return
        ativo = true
        frameCount = 0
        frameDropCount = 0
        ultimoTempoNs = 0L
        handler.post {
            Choreographer.getInstance().postFrameCallback(frameCallback)
            handler.postDelayed(calculadorFps, 1000L)
        }
    }

    fun parar() {
        ativo = false
        handler.removeCallbacks(calculadorFps)
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        _fps.value = 0
    }
}
