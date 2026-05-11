package com.turbobooster.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbobooster.engine.ProcessoInfo
import com.turbobooster.ui.theme.*
import com.turbobooster.viewmodel.MainViewModel

@Composable
fun OtimizarScreen(viewModel: MainViewModel) {
    val mensagem by viewModel.mensagemResultado.collectAsState()
    val processos by viewModel.processos.collectAsState()
    val otimizando by viewModel.otimizando.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.carregarProcessos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoPrincipal)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "OTIMIZAÇÕES",
            color = VerdeNeon,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mensagem de resultado
        AnimatedVisibility(visible = mensagem != null) {
            mensagem?.let {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    color = VerdeNeon.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, VerdeNeon.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = it,
                        color = VerdeNeon,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Ações principais
        Text(
            text = "AÇÕES",
            color = TextoSecundario,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        AcaoItem(
            icone = Icons.Default.CleaningServices,
            titulo = "Limpar Cache",
            descricao = "Remove cache acumulado (requer Shizuku)",
            cor = AzulNeon,
            carregando = otimizando,
            onClick = { viewModel.limparCache() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        AcaoItem(
            icone = Icons.Default.Memory,
            titulo = "Liberar RAM",
            descricao = "Encerra apps em background para liberar memória",
            cor = VerdeNeon,
            carregando = false,
            onClick = { viewModel.liberarRam() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        AcaoItem(
            icone = Icons.Default.Close,
            titulo = "Matar Background",
            descricao = "Força encerramento de todos os processos em bg",
            cor = VermelhoDanger,
            carregando = false,
            onClick = { viewModel.liberarRam() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        AcaoItem(
            icone = Icons.Default.Refresh,
            titulo = "Restaurar Padrão",
            descricao = "Restaura animações e configurações originais",
            cor = AmareloWarning,
            carregando = false,
            onClick = { viewModel.aplicarModo(com.turbobooster.engine.OptimizationEngine.ModoDesempenho.BALANCEADO) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Lista de processos em background
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PROCESSOS EM BACKGROUND",
                color = TextoSecundario,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.carregarProcessos() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Atualizar", tint = TextoSecundario, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val background = processos.filter { it.emBackground }
        if (background.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = FundoCard,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Nenhum processo em background",
                    color = TextoSecundario,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            background.forEach { processo ->
                ProcessoItem(
                    processo = processo,
                    onMatar = { viewModel.matarProcesso(processo.nome) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AcaoItem(
    icone: ImageVector,
    titulo: String,
    descricao: String,
    cor: androidx.compose.ui.graphics.Color,
    carregando: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FundoCard,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Borda)
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = !carregando, onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = cor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (carregando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = cor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(22.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = titulo, color = TextoPrincipal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(text = descricao, color = TextoSecundario, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ProcessoItem(
    processo: ProcessoInfo,
    onMatar: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FundoCard,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Borda)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = processo.nome.substringAfterLast("."),
                    color = TextoPrincipal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = processo.nome,
                    color = TextoSecundario,
                    fontSize = 10.sp
                )
            }
            IconButton(
                onClick = onMatar,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Matar",
                    tint = VermelhoDanger,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
