package com.turbobooster.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.turbobooster.ui.theme.*

@Composable
fun OtimizarScreen(
    onLimparCache: () -> Unit,
    onLiberarRam: () -> Unit,
    onMatar: () -> Unit,
    onRestaurar: () -> Unit,
    mensagem: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("OTIMIZAÇÕES", color = NeonGreen, fontSize = 14.sp,
            fontWeight = FontWeight.Black, letterSpacing = 3.sp)

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

        AcaoBtn("🧹", "Limpar Cache", "Remove arquivos temporários de todos os apps", NeonBlue, onLimparCache)
        AcaoBtn("💾", "Liberar RAM", "Fecha processos desnecessários em background", NeonPurple, onLiberarRam)
        AcaoBtn("⚡", "Matar Background", "Encerra todos os apps não essenciais via Shizuku", NeonGreen, onMatar)
        AcaoBtn("↩", "Restaurar Sistema", "Reativa animações e configurações padrão", NeonOrange, onRestaurar)
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
