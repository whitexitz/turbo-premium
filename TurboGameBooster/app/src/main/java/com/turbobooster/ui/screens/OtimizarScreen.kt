package com.turbobooster.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.turbobooster.data.AppState
import com.turbobooster.ui.theme.*

@Composable
fun OtimizarScreen(
    state: AppState,
    onLimparCache: () -> Unit,
    onLiberarRam: () -> Unit,
    onMatar: () -> Unit,
    onRestaurar: () -> Unit,
    onConectarShizuku: () -> Unit,
    mensagem: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "OTIMIZAÇÕES", color = NeonGreen, fontSize = 14.sp,
            fontWeight = FontWeight.Black, letterSpacing = 3.sp
        )

        // Shizuku status badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (state.shizukuConectado) NeonGreen.copy(alpha = 0.08f)
                    else NeonRed.copy(alpha = 0.08f),
                    RoundedCornerShape(10.dp)
                )
                .border(
                    1.dp,
                    if (state.shizukuConectado) NeonGreen.copy(alpha = 0.3f)
                    else NeonRed.copy(alpha = 0.3f),
                    RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "●",
                    color = if (state.shizukuConectado) NeonGreen else NeonRed,
                    fontSize = 10.sp
                )
                Column {
                    Text(
                        "Shizuku: ${if (state.shizukuConectado) "Conectado" else "Desconectado"}",
                        color = if (state.shizukuConectado) NeonGreen else NeonRed,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (state.shizukuConectado) "Comandos privilegiados disponíveis"
                        else "Sem Shizuku: otimizações limitadas",
                        color = TextSecondary, fontSize = 10.sp
                    )
                }
            }
            if (!state.shizukuConectado) {
                TextButton(
                    onClick = onConectarShizuku,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        "CONECTAR", color = NeonBlue, fontSize = 10.sp,
                        fontWeight = FontWeight.Black, letterSpacing = 1.sp
                    )
                }
            }
        }

        // RAM info card
        if (state.ramTotalMB > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgCard, RoundedCornerShape(10.dp))
                    .border(1.dp, BorderNeon, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RamStat("TOTAL", "${state.ramTotalMB} MB", TextSecondary)
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(BorderNeon))
                RamStat("USADA", "${state.ramUsadaMB} MB", NeonPurple)
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(BorderNeon))
                RamStat("LIVRE", "${state.ramTotalMB - state.ramUsadaMB} MB", NeonGreen)
            }
        }

        // Message feedback
        AnimatedVisibility(visible = mensagem != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeonGreen.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(mensagem ?: "", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Action buttons
        AcaoBtn("⚡", "Turbo Boost", "Encerra todos os apps não essenciais via Shizuku", NeonGreen, onMatar)
        AcaoBtn("💾", "Liberar RAM", "Fecha processos desnecessários em background", NeonPurple, onLiberarRam)
        AcaoBtn("🧹", "Limpar Cache", "Remove arquivos temporários de todos os apps", NeonBlue, onLimparCache)
        AcaoBtn("↩", "Restaurar", "Reativa animações e configurações padrão do sistema", NeonOrange, onRestaurar)

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun RamStat(label: String, valor: String, cor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(valor, color = cor, fontSize = 13.sp, fontWeight = FontWeight.Black)
        Text(label, color = TextSecondary, fontSize = 9.sp, letterSpacing = 1.sp)
    }
}

@Composable
fun AcaoBtn(icone: String, titulo: String, desc: String, cor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgCard, RoundedCornerShape(14.dp))
            .border(1.dp, cor.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icone, fontSize = 28.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = TextSecondary, fontSize = 11.sp)
        }
        Text("›", color = cor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
