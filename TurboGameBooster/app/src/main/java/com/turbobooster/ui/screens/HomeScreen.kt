package com.turbobooster.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.turbobooster.data.AppState
import com.turbobooster.ui.theme.*
import kotlin.math.*

@Composable
fun HomeScreen(
    state: AppState,
    onTurboClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-50).dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonGreen.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "TURBO",
                        color = NeonGreen,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                    Text(
                        "GAME BOOSTER",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        letterSpacing = 3.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            if (state.turboAtivo) NeonGreen.copy(alpha = 0.15f)
                            else BgCardBright,
                            RoundedCornerShape(20.dp)
                        )
                        .border(
                            1.dp,
                            if (state.turboAtivo) NeonGreen.copy(alpha = 0.5f) else BorderNeon,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (state.turboAtivo) "● ATIVO" else "○ STANDBY",
                        color = if (state.turboAtivo) NeonGreen else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgCard, RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        if (state.fps > 50) NeonGreen.copy(alpha = 0.3f) else BorderNeon,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${state.fps}",
                        color = if (state.fps > 50) NeonGreen else NeonOrange,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 72.sp
                    )
                    Text(
                        "FRAMES POR SEGUNDO",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        letterSpacing = 3.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CyberGauge(
                    modifier = Modifier.weight(1f),
                    label = "CPU",
                    valor = state.cpu,
                    max = 100f,
                    cor = NeonBlue,
                    unidade = "%",
                    sub = "${state.cpuFreq} MHz"
                )
                CyberGauge(
                    modifier = Modifier.weight(1f),
                    label = "RAM",
                    valor = state.ram,
                    max = 100f,
                    cor = NeonPurple,
                    unidade = "%",
                    sub = "${state.ramUsadaMB}MB"
                )
                CyberGauge(
                    modifier = Modifier.weight(1f),
                    label = "TEMP",
                    valor = state.temp,
                    max = 70f,
                    cor = when {
                        state.temp > 50f -> NeonRed
                        state.temp > 43f -> NeonOrange
                        else -> NeonGreen
                    },
                    unidade = "°C",
                    sub = if (state.temp > 50f) "QUENTE" else "OK"
                )
            }

            TurboButton(
                ativo = state.turboAtivo,
                carregando = state.otimizando,
                onClick = onTurboClick
            )

            AnimatedVisibility(
                visible = state.ultimoBoost != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                state.ultimoBoost?.let { boost ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NeonGreen.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                            .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            BoostStat("RAM", "+${boost.ramMB}MB", NeonGreen)
                            BoostStat("PROCS", "${boost.processos}", NeonBlue)
                            BoostStat("ANIM", if (boost.animacoes) "OFF" else "—", NeonPurple)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniChart(
                    modifier = Modifier.weight(1f),
                    label = "CPU %",
                    dados = state.historicoCpu,
                    cor = NeonBlue
                )
                MiniChart(
                    modifier = Modifier.weight(1f),
                    label = "RAM %",
                    dados = state.historicoRam,
                    cor = NeonPurple
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun CyberGauge(
    modifier: Modifier = Modifier,
    label: String,
    valor: Float,
    max: Float,
    cor: Color,
    unidade: String,
    sub: String
) {
    val animValor by animateFloatAsState(
        targetValue = valor,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "gauge"
    )

    Box(
        modifier = modifier
            .background(BgCard, RoundedCornerShape(16.dp))
            .border(1.dp, cor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .drawBehind {
                        val stroke = 5.dp.toPx()
                        val sweep = (animValor / max) * 300f
                        drawArc(
                            color = cor.copy(alpha = 0.15f),
                            startAngle = 120f,
                            sweepAngle = 300f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = stroke,
                                cap = StrokeCap.Round
                            )
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(cor.copy(alpha = 0.5f), cor),
                                center = Offset(size.width / 2, size.height / 2)
                            ),
                            startAngle = 120f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = stroke,
                                cap = StrokeCap.Round
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${animValor.toInt()}",
                        color = cor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 16.sp
                    )
                    Text(
                        unidade,
                        color = TextSecondary,
                        fontSize = 8.sp
                    )
                }
            }

            Text(
                label,
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                sub,
                color = TextDim,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
fun TurboButton(
    ativo: Boolean,
    carregando: Boolean,
    onClick: () -> Unit
) {
    val pulso by rememberInfiniteTransition(label = "pulso").animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(160.dp)
            .graphicsLayer {
                scaleX = if (carregando) pulso else 1f
                scaleY = if (carregando) pulso else 1f
            },
        contentAlignment = Alignment.Center
    ) {
        if (ativo) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .blur(20.dp)
                    .background(
                        NeonGreen.copy(alpha = 0.3f),
                        CircleShape
                    )
            )
        }

        Box(
            modifier = Modifier
                .size(140.dp)
                .background(
                    Brush.radialGradient(
                        colors = if (ativo) listOf(
                            NeonGreen.copy(alpha = 0.2f),
                            BgCard
                        ) else listOf(BgCardBright, BgCard)
                    ),
                    CircleShape
                )
                .border(
                    2.dp,
                    if (ativo) NeonGreen else BorderNeon,
                    CircleShape
                )
                .clickable(enabled = !carregando, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    if (carregando) "..." else if (ativo) "✓" else "▶",
                    color = if (ativo) NeonGreen else TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    when {
                        carregando -> "BOOST"
                        ativo -> "ATIVO"
                        else -> "TURBO"
                    },
                    color = if (ativo) NeonGreen else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}

@Composable
fun MiniChart(
    modifier: Modifier = Modifier,
    label: String,
    dados: List<Float>,
    cor: Color
) {
    Box(
        modifier = modifier
            .background(BgCard, RoundedCornerShape(12.dp))
            .border(1.dp, BorderNeon, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, color = TextSecondary, fontSize = 9.sp, letterSpacing = 1.sp)
                Text(
                    "${dados.lastOrNull()?.toInt() ?: 0}%",
                    color = cor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            ) {
                if (dados.size < 2) return@Canvas
                val max = dados.maxOrNull()?.coerceAtLeast(1f) ?: 1f
                val w = size.width
                val h = size.height
                val pts = dados.mapIndexed { i, v ->
                    Offset(
                        (i.toFloat() / (dados.size - 1)) * w,
                        h - (v / max) * (h - 4f) - 2f
                    )
                }
                val path = Path().apply {
                    moveTo(0f, h)
                    pts.forEach { lineTo(it.x, it.y) }
                    lineTo(w, h)
                    close()
                }
                drawPath(path, Brush.verticalGradient(
                    listOf(cor.copy(alpha = 0.3f), Color.Transparent)
                ))
                for (i in 0 until pts.size - 1) {
                    drawLine(cor, pts[i], pts[i + 1], strokeWidth = 1.5f, cap = StrokeCap.Round)
                }
            }
        }
    }
}

@Composable
fun BoostStat(label: String, valor: String, cor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, color = cor, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(label, color = TextSecondary, fontSize = 8.sp, letterSpacing = 1.sp)
    }
}
