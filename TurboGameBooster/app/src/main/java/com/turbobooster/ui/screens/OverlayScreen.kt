package com.turbobooster.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.turbobooster.data.AppState
import com.turbobooster.ui.theme.*

@Composable
fun OverlayScreen(
    state: AppState,
    overlayAtivo: Boolean,
    onToggleOverlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("OVERLAY EM JOGO", color = NeonGreen, fontSize = 14.sp,
            fontWeight = FontWeight.Black, letterSpacing = 3.sp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    androidx.compose.ui.graphics.Color(0xFF1a1a2e),
                    RoundedCornerShape(16.dp)
                )
                .border(1.dp, BorderNeon, RoundedCornerShape(16.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            androidx.compose.ui.graphics.Color(0xCC000000),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            NeonGreen.copy(alpha = 0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OverlayItem("${state.fps}", "FPS", NeonGreen)
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(BorderNeon))
                    OverlayItem("${state.cpu.toInt()}%", "CPU", NeonBlue)
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(BorderNeon))
                    OverlayItem("${state.temp.toInt()}°", "TEMP", when {
                        state.temp > 50f -> NeonRed
                        state.temp > 43f -> NeonOrange
                        else -> NeonGreen
                    })
                }
            }
            Box(
                modifier = Modifier.fillMaxSize().padding(10.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text("Preview do overlay durante o jogo",
                    color = TextDim, fontSize = 9.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }

        Button(
            onClick = onToggleOverlay,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (overlayAtivo) NeonRed.copy(alpha = 0.15f) else NeonGreen.copy(alpha = 0.15f)
            ),
            border = BorderStroke(
                1.dp,
                if (overlayAtivo) NeonRed.copy(alpha = 0.5f) else NeonGreen.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                if (overlayAtivo) "⏹ PARAR OVERLAY" else "▶ INICIAR OVERLAY",
                color = if (overlayAtivo) NeonRed else NeonGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgCard, RoundedCornerShape(12.dp))
                .border(1.dp, BorderNeon, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Como funciona", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    "O overlay usa SYSTEM_ALERT_WINDOW para aparecer sobre qualquer app. " +
                    "FPS medido via Choreographer. CPU via /proc/stat. " +
                    "Consome ~2MB RAM e ~0.5% CPU.",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun OverlayItem(valor: String, label: String, cor: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, color = cor, fontSize = 14.sp, fontWeight = FontWeight.Black)
        Text(label, color = TextSecondary, fontSize = 8.sp)
    }
}
