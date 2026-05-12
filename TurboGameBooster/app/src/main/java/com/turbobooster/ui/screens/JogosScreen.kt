package com.turbobooster.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.turbobooster.ui.theme.*

data class JogoPerfil(
    val id: String,
    val nome: String,
    val icone: String,
    val package_name: String,
    val ativo: Boolean = true
)

@Composable
fun JogosScreen() {
    var jogos by remember {
        mutableStateOf(listOf(
            JogoPerfil("ff", "Free Fire", "🔫", "com.dts.freefireth"),
            JogoPerfil("ffmax", "Free Fire MAX", "🔥", "com.dts.freefiremax"),
            JogoPerfil("pubg", "PUBG Mobile", "🪖", "com.tencent.ig"),
            JogoPerfil("codm", "Call of Duty Mobile", "💣", "com.activision.callofduty.shooter"),
            JogoPerfil("ml", "Mobile Legends", "⚔️", "com.mobile.legends"),
            JogoPerfil("genshin", "Genshin Impact", "🌙", "com.miHoYo.GenshinImpact"),
            JogoPerfil("wildrift", "Wild Rift", "⚡", "com.riotgames.league.wildrift")
        ))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "PERFIS DE JOGOS",
            color = NeonGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp
        )
        Text(
            "Otimizações aplicadas automaticamente ao detectar o jogo",
            color = TextSecondary,
            fontSize = 11.sp
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(jogos) { jogo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (jogo.ativo) NeonGreen.copy(alpha = 0.05f) else BgCard,
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            1.dp,
                            if (jogo.ativo) NeonGreen.copy(alpha = 0.3f) else BorderNeon,
                            RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(jogo.icone, fontSize = 28.sp)
                        Column {
                            Text(jogo.nome, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(jogo.package_name, color = TextDim, fontSize = 9.sp)
                        }
                    }
                    Switch(
                        checked = jogo.ativo,
                        onCheckedChange = { novo ->
                            jogos = jogos.map { if (it.id == jogo.id) it.copy(ativo = novo) else it }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BgDeep,
                            checkedTrackColor = NeonGreen,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = BorderNeon
                        )
                    )
                }
            }
        }
    }
}
