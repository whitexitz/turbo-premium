package com.turbobooster.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbobooster.shizuku.ShizukuManager
import com.turbobooster.shizuku.ShizukuStatus
import com.turbobooster.ui.theme.*
import com.turbobooster.viewmodel.MainViewModel

@Composable
fun ShizukuScreen(viewModel: MainViewModel) {
    val statusShizuku by viewModel.statusShizuku.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoPrincipal)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "SHIZUKU",
            color = VerdeNeon,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status visual
        val (cor, icone, titulo, descricao) = when (statusShizuku) {
            ShizukuStatus.CONECTADO -> StatusConfig(
                VerdeNeon, Icons.Default.CheckCircle,
                "Conectado", "Shizuku está ativo e com permissão concedida"
            )
            ShizukuStatus.SEM_PERMISSAO -> StatusConfig(
                AmareloWarning, Icons.Default.Warning,
                "Sem Permissão", "Shizuku está ativo, mas sem permissão. Clique em Solicitar Permissão"
            )
            ShizukuStatus.NAO_DISPONIVEL -> StatusConfig(
                VermelhoDanger, Icons.Default.Cancel,
                "Indisponível", "O serviço Shizuku não está em execução"
            )
            ShizukuStatus.VERIFICANDO -> StatusConfig(
                TextoSecundario, Icons.Default.Refresh,
                "Verificando...", "Aguardando resposta do Shizuku"
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = cor.copy(alpha = 0.08f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, cor.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(32.dp))
                Column {
                    Text(text = titulo, color = cor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = descricao, color = cor.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão solicitar permissão
        if (statusShizuku != ShizukuStatus.CONECTADO) {
            Button(
                onClick = { ShizukuManager.solicitarPermissao() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeNeon),
                shape = RoundedCornerShape(12.dp),
                enabled = statusShizuku == ShizukuStatus.SEM_PERMISSAO
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Solicitar Permissão", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Passo a passo
        Text(
            text = "COMO ATIVAR O SHIZUKU",
            color = TextoSecundario,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        PassoItem(
            numero = "1",
            titulo = "Android 11+ (Sem cabo USB)",
            descricao = "Vá em Configurações > Opções do desenvolvedor > Depuração sem fio. Ative e anote o endereço IP e porta."
        )
        Spacer(modifier = Modifier.height(6.dp))
        PassoItem(
            numero = "2",
            titulo = "Via ADB (com cabo USB)",
            descricao = "Conecte o celular ao PC, ative depuração USB nas opções do desenvolvedor e execute: adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh"
        )
        Spacer(modifier = Modifier.height(6.dp))
        PassoItem(
            numero = "3",
            titulo = "Instale o app Shizuku",
            descricao = "Baixe o app Shizuku na Play Store ou GitHub. Abra o app e siga as instruções de ativação."
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Com Shizuku ativo
        Text(
            text = "DISPONÍVEL COM SHIZUKU",
            color = VerdeNeon,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val comandosShizuku = listOf(
            "Desativar animações do sistema" to Icons.Default.Animation,
            "Matar processos em background (am kill-all)" to Icons.Default.Close,
            "Limpar cache de todos os apps (pm trim-caches)" to Icons.Default.CleaningServices,
            "Configurar modo de desempenho do sistema" to Icons.Default.Speed,
            "Alterar configurações globais (settings put global)" to Icons.Default.Tune
        )

        comandosShizuku.forEach { (texto, icon) ->
            Row(
                modifier = Modifier.padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = VerdeNeon, modifier = Modifier.size(14.dp))
                Text(text = texto, color = TextoPrincipal, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sem Shizuku
        Text(
            text = "MODO LIMITADO (SEM SHIZUKU)",
            color = AmareloWarning,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val semShizuku = listOf(
            "Monitor de CPU via /proc/stat",
            "Monitor de RAM via ActivityManager",
            "Monitor de temperatura via /sys/class/thermal",
            "Medição de FPS via Choreographer",
            "Matar processos próprios (limitado pelo Android)"
        )

        semShizuku.forEach { texto ->
            Row(
                modifier = Modifier.padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = AmareloWarning, modifier = Modifier.size(14.dp))
                Text(text = texto, color = TextoSecundario, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private data class StatusConfig(
    val cor: androidx.compose.ui.graphics.Color,
    val icone: androidx.compose.ui.graphics.vector.ImageVector,
    val titulo: String,
    val descricao: String
)

@Composable
private fun PassoItem(numero: String, titulo: String, descricao: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FundoCard,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Borda)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = VerdeNeon.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = numero, color = VerdeNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column {
                Text(text = titulo, color = TextoPrincipal, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = descricao, color = TextoSecundario, fontSize = 11.sp)
            }
        }
    }
}
