package com.turbobooster.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import androidx.savedstate.*
import com.turbobooster.monitor.*
import com.turbobooster.ui.theme.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine

class OverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private lateinit var wm: WindowManager
    private var view: ComposeView? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    private val cpuMon = CpuMonitor()
    private val fpsMon = FpsMonitor()
    private val thermalMon = ThermalMonitor()

    private val fpsState = mutableStateOf(0)
    private val cpuState = mutableStateOf(0f)
    private val tempState = mutableStateOf(0f)

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(2, NotificationCompat.Builder(this, "overlay")
            .setContentTitle("Overlay ativo")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true).build())

        cpuMon.iniciar(); fpsMon.iniciar(); thermalMon.iniciar()

        scope.launch {
            combine(cpuMon.uso, fpsMon.fps, thermalMon.temp) { c, f, t -> Triple(c, f, t) }
                .collect { (c, f, t) ->
                    cpuState.value = c; fpsState.value = f; tempState.value = t
                }
        }

        criarOverlay()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        scope.cancel()
        cpuMon.parar(); fpsMon.parar(); thermalMon.parar()
        view?.let { wm.removeView(it) }
        super.onDestroy()
    }

    private fun criarOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.END; x = 16; y = 80 }

        val cv = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            setContent {
                val fps by fpsState
                val cpu by cpuState
                val temp by tempState
                val corTemp = when {
                    temp > 50f -> NeonRed
                    temp > 43f -> NeonOrange
                    else -> NeonGreen
                }
                Row(
                    modifier = Modifier
                        .background(Color(0xE6080810), RoundedCornerShape(8.dp))
                        .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OverlayVal("$fps", "FPS", NeonGreen)
                    Box(Modifier.width(1.dp).height(20.dp).background(BorderNeon))
                    OverlayVal("${cpu.toInt()}%", "CPU", NeonBlue)
                    Box(Modifier.width(1.dp).height(20.dp).background(BorderNeon))
                    OverlayVal("${temp.toInt()}°", "TEMP", corTemp)
                }
            }
        }

        var ix = 0; var iy = 0; var px = 0; var py = 0
        cv.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { ix = e.rawX.toInt(); iy = e.rawY.toInt(); px = params.x; py = params.y; true }
                MotionEvent.ACTION_MOVE -> { params.x = px + (ix - e.rawX.toInt()); params.y = py + (e.rawY.toInt() - iy); wm.updateViewLayout(cv, params); true }
                else -> false
            }
        }

        wm.addView(cv, params)
        view = cv
    }
}

@androidx.compose.runtime.Composable
private fun OverlayVal(valor: String, label: String, cor: Color) {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(valor, color = cor, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Text(label, color = TextSecondary, fontSize = 7.sp)
    }
}
