package com.turbobooster.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbobooster.engine.OptimizationEngine
import com.turbobooster.shizuku.ShizukuStatus
import com.turbobooster.ui.theme.*
import com.turbobooster.viewmodel.MainViewModel
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.Canvas
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val cpuData by viewModel.cpuMonitor.dadosCpu.collectAsState()
    val ramData by viewModel.ramMonitor.dadosRam.collectAsState()
    val thermalData by viewModel.thermalMonitor.dadosThermal.collectAsState()
    val fps by viewModel.fpsMonitor.fps.collectAsState()
    val turboAtivo by viewModel.turboAtivo.collectAsState()
    val otimizando by viewModel.otimizando.collectAsState()
    val mensagem by viewModel.mensagemResultado.collectAsState()
    val modo by viewModel.modoDesempenho.collectAsState()
    val statusShizuku by viewModel.statusShizuku.collectAsState()
    val historicoCpu by viewModel.historicoCpu.collectAsState()
    val historicoRam by viewModel.historicoRam.collectAsState()
    val historicoFps by viewModel.historicoFps.collectAsState()
    val historicoTemp by viewModel.historicoTemp.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoPrincipal)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TURBO BOOSTER",
                color = VerdeNeon,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            // Badge Shizuku
            val (badgeColor, badgeText) = when (statusShizuku) {
                ShizukuStatus.CONECTADO -> Pair(VerdeNeon, "Shizuku ON")
                ShizukuStatus.SEM_PERMISSAO -> Pair(AmareloWarning, "Sem permissão")
                ShizukuStatus.NAO_DISPONIVEL -> Pair(VermelhoDanger, "Shizuku OFF")
                ShizukuStatus.VERIFICANDO -> Pair(TextoSecundario, "Verificando...")
            }
            Surface(
                color = badgeColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, badgeColor)
            ) {
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botão TURBO central
        BotaoTurbo(
            ativo = turboAtivo,
            otimizando = otimizando,
            onClick = { viewModel.aplicarTurboBoost() }
        )

        // Mensagem de resultado
        AnimatedVisibility(visible = mensagem != null) {
            mensagem?.let {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = VerdeNeon.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, VerdeNeon.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = it,
                        color = VerdeNeon,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4 Gauges circulares
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GaugeCircular(
                valor = cpuData.usoTotal,
                maximo = 100f,
                label = "CPU",
                unidade = "%",
                cor = AzulNeon
            )
            GaugeCircular(
                valor = fps.toFloat(),
                maximo = 120f,
                label = "FPS",
                unidade = "",
                cor = VerdeNeon
            )
            GaugeCircular(
                valor = ramData.percentualUso,
                maximo = 100f,
                label = "RAM",
                unidade = "%",
                cor = VioletaNeon
            )
            GaugeCircular(
                valor = thermalData.temperaturaCelsius,
                maximo = 60f,
                label = "TEMP",
                unidade = "°",
                cor = when (thermalData.status) {
                    com.turbobooster.monitor.ThermalMonitor.StatusThermal.PERIGO -> VermelhoDanger
                    com.turbobooster.monitor.ThermalMonitor.StatusThermal.ATENCAO -> AmareloWarning
                    else -> VerdeNeon
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Seletor de modo
        Text(
            text = "MODO DE DESEMPENHO",
            color = TextoSecundario,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OptimizationEngine.ModoDesempenho.values().forEach { m ->
                val selecionado = modo == m
                val (label, cor) = when (m) {
                    OptimizationEngine.ModoDesempenho.BALANCEADO -> Pair("Balanceado", AzulNeon)
                    OptimizationEngine.ModoDesempenho.DESEMPENHO -> Pair("Desempenho", VerdeNeon)
                    OptimizationEngine.ModoDesempenho.ECONOMIA -> Pair("Economia", AmareloWarning)
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.aplicarModo(m) },
                    color = if (selecionado) cor.copy(alpha = 0.2f) else FundoCard,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (selecionado) cor else Borda)
                ) {
                    Text(
                        text = label,
                        color = if (selecionado) cor else TextoSecundario,
                        fontSize = 11.sp,
                        fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mini gráficos de histórico
        Text(
            text = "HISTÓRICO",
            color = TextoSecundario,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CardGrafico(
                titulo = "CPU",
                valor = "${cpuData.usoTotal.toInt()}%",
                historico = historicoCpu,
                cor = AzulNeon,
                modifier = Modifier.weight(1f)
            )
            CardGrafico(
                titulo = "FPS",
                valor = "$fps",
                historico = historicoFps.map { it.toFloat() },
                cor = VerdeNeon,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CardGrafico(
                titulo = "RAM",
                valor = "${ramData.usadaMB}MB",
                historico = historicoRam,
                cor = VioletaNeon,
                modifier = Modifier.weight(1f)
            )
            CardGrafico(
                titulo = "TEMP",
                valor = "${thermalData.temperaturaCelsius.toInt()}°C",
                historico = historicoTemp,
                cor = AmareloWarning,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun BotaoTurbo(
    ativo: Boolean,
    otimizando: Boolean,
    onClick: () -> Unit
) {
    val escala by animateFloatAsState(
        targetValue = if (otimizando) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "escala"
    )

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulso by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (otimizando) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulso"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(160.dp)
            .scale(escala * pulso)
    ) {
        // Anel externo
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = if (ativo) VerdeNeon.copy(alpha = 0.15f) else Color(0xFF1A1A25),
                radius = size.minDimension / 2
            )
            drawCircle(
                color = if (ativo) VerdeNeon else Color(0xFF2A2A35),
                radius = size.minDimension / 2,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Botão central
        Surface(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable(enabled = !otimizando, onClick = onClick),
            shape = CircleShape,
            color = if (ativo) VerdeNeon.copy(alpha = 0.2f) else FundoCard
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (otimizando) Icons.Default.Refresh else Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = if (ativo) VerdeNeon else TextoSecundario,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = if (otimizando) "..." else if (ativo) "ATIVO" else "TURBO",
                        color = if (ativo) VerdeNeon else TextoPrincipal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GaugeCircular(
    valor: Float,
    maximo: Float,
    label: String,
    unidade: String,
    cor: Color
) {
    val progresso = (valor / maximo).coerceIn(0f, 1f)
    val animProgresso by animateFloatAsState(
        targetValue = progresso,
        animationSpec = tween(500),
        label = "gauge"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                val angulo = 270f * animProgresso
                // Fundo
                drawArc(
                    color = Color(0xFF1E1E2A),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = stroke
                )
                // Valor
                drawArc(
                    color = cor,
                    startAngle = 135f,
                    sweepAngle = angulo,
                    useCenter = false,
                    style = stroke
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${valor.toInt()}$unidade",
                    color = cor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(text = label, color = TextoSecundario, fontSize = 10.sp)
    }
}

@Composable
private fun CardGrafico(
    titulo: String,
    valor: String,
    historico: List<Float>,
    cor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = FundoCard,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Borda)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = titulo, color = TextoSecundario, fontSize = 11.sp)
                Text(text = valor, color = cor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Mini gráfico de linha
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                if (historico.size < 2) return@Canvas
                val max = historico.max().takeIf { it > 0f } ?: 1f
                val w = size.width
                val h = size.height
                val step = w / (historico.size - 1)

                val pontos = historico.mapIndexed { i, v ->
                    val x = i * step
                    val y = h - (v / max * h)
                    androidx.compose.ui.geometry.Offset(x, y)
                }

                for (i in 0 until pontos.size - 1) {
                    drawLine(
                        color = cor.copy(alpha = 0.7f),
                        start = pontos[i],
                        end = pontos[i + 1],
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                // Ponto atual
                pontos.lastOrNull()?.let {
                    drawCircle(color = cor, radius = 3.dp.toPx(), center = it)
                }
            }
        }
    }
}
