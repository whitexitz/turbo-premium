package com.turbobooster.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.turbobooster.monitor.CpuMonitor
import com.turbobooster.monitor.FpsMonitor
import com.turbobooster.monitor.ThermalMonitor
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.combine

class OverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val cpuMonitor = CpuMonitor()
    private val fpsMonitor = FpsMonitor()
    private val thermalMonitor = ThermalMonitor()

    private val _fps = mutableStateOf(0)
    private val _cpu = mutableStateOf(0f)
    private val _temp = mutableStateOf(0f)

    private val escopo = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        iniciarComoForeground()
        iniciarMonitores()
        criarOverlay()

        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        escopo.cancel()
        cpuMonitor.parar()
        fpsMonitor.parar()
        thermalMonitor.parar()
        overlayView?.let { windowManager.removeView(it) }
        super.onDestroy()
    }

    private fun iniciarComoForeground() {
        val notificacao = NotificationCompat.Builder(this, "overlay_service")
            .setContentTitle("Overlay ativo")
            .setContentText("Monitorando desempenho")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
        startForeground(2, notificacao)
    }

    private fun iniciarMonitores() {
        cpuMonitor.iniciar()
        fpsMonitor.iniciar()
        thermalMonitor.iniciar()

        escopo.launch {
            combine(
                cpuMonitor.dadosCpu,
                fpsMonitor.fps,
                thermalMonitor.dadosThermal
            ) { cpu, fps, thermal ->
                Triple(cpu.usoTotal, fps, thermal.temperaturaCelsius)
            }.collect { (cpu, fps, temp) ->
                _cpu.value = cpu
                _fps.value = fps
                _temp.value = temp
            }
        }
    }

    private fun criarOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 80
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)

            setContent {
                val fps by _fps
                val cpu by _cpu
                val temp by _temp

                val corTemp = when {
                    temp > 50f -> Color(0xFFFF3B5C)
                    temp > 43f -> Color(0xFFFFAA00)
                    else -> Color(0xFF00FF88)
                }

                Row(
                    modifier = Modifier
                        .background(
                            Color(0xCC000000),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$fps",
                            color = Color(0xFF00FF88),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "FPS", color = Color(0xFF888888), fontSize = 8.sp)
                    }

                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFF333333)))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${cpu.toInt()}%",
                            color = Color(0xFF00AAFF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "CPU", color = Color(0xFF888888), fontSize = 8.sp)
                    }

                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFF333333)))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${temp.toInt()}°",
                            color = corTemp,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "TEMP", color = Color(0xFF888888), fontSize = 8.sp)
                    }
                }
            }
        }

        var inicioX = 0
        var inicioY = 0
        var posX = 0
        var posY = 0

        composeView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    inicioX = event.rawX.toInt()
                    inicioY = event.rawY.toInt()
                    posX = params.x
                    posY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = posX + (inicioX - event.rawX.toInt())
                    params.y = posY + (event.rawY.toInt() - inicioY)
                    windowManager.updateViewLayout(composeView, params)
                    true
                }
                else -> false
            }
        }

        windowManager.addView(composeView, params)
        overlayView = composeView
    }
}
