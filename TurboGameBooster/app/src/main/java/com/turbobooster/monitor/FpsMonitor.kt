package com.turbobooster.monitor

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FpsMonitor {
    private val _fps = MutableStateFlow(0)
    val fps: StateFlow<Int> = _fps

    private var count = 0
    private var ativo = false
    private val handler = Handler(Looper.getMainLooper())

    private val callback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!ativo) return
            count++
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    private val ticker = object : Runnable {
        override fun run() {
            if (!ativo) return
            _fps.value = count
            count = 0
            handler.postDelayed(this, 1000L)
        }
    }

    fun iniciar() {
        if (ativo) return
        ativo = true
        handler.post {
            Choreographer.getInstance().postFrameCallback(callback)
            handler.postDelayed(ticker, 1000L)
        }
    }

    fun parar() {
        ativo = false
        handler.removeCallbacks(ticker)
    }
}
