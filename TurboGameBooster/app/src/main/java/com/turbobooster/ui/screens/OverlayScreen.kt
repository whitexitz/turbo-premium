package com.turbobooster.ui.screens

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbobooster.service.OverlayService
import com.turbobooster.ui.theme.*
import com.turbobooster.viewmodel.MainViewModel

@Composable
fun OverlayScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var overlayAtivo by remember { mutableStateOf(false) }
    var mostrarFps by remember { mutableStateOf(true) }
    var mostrarCpu by remember { mutableStateOf(true) }
    var mostrarTemp by remember { mutableStateOf(true) }
    var mostrarRam by remember { mutableStateOf(false) }
    var opacidade by remember { mutableStateOf(0.85f) }

    val fps by viewModel.fpsMonitor.fps.collectAsState()
    val cpu by viewModel.cpuMonitor.dadosCpu.collectAsState()
    val temp by viewModel.thermalMonitor.dadosThermal.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoPrincipal)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "OVERLAY",
            color = VerdeNeon,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Preview do overlay
        Text(
            text = "PREVIEW",
            color = TextoSecundario,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FundoCard,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Borda)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Simula fundo de jogo
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF050510)),
                    contentAlignment = Alignment.TopEnd
                ) {
                    // Widget simulado
                    Surface(
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xCC000000).copy(alpha = opacidade),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (mostrarFps) {
                                OverlayMetrica("$fps", "FPS", VerdeNeon)
                                Divider(modifier = Modifier.width(1.dp).height(20.dp), color = Color(0xFF333333))
                            }
                            if (mostrarCpu) {
                                OverlayMetrica("${cpu.usoTotal.toInt()}%", "CPU", AzulNeon)
                                Divider(modifier = Modifier.width(1.dp).height(20.dp), color = Color(0xFF333333))
                            }
                            if (mostrarTemp) {
                                val corTemp = when {
                                    temp.temperaturaCelsius > 50 -> VermelhoDanger
                                    temp.temperaturaCelsius > 43 -> AmareloWarning
                                    else -> VerdeNeon
                                }
                                OverlayMetrica("${temp.temperaturaCelsius.toInt()}°", "TEMP", corTemp)
                            }
                            if (mostrarRam) {
                                if (mostrarFps || mostrarCpu || mostrarTemp) {
                                    Divider(modifier = Modifier.width(1.dp).height(20.dp), color = Color(0xFF333333))
                                }
                                OverlayMetrica("RAM", "MB", VioletaNeon)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Toggles
        Text(
            text = "MÉTRICAS VISÍVEIS",
            color = TextoSecundario,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        ToggleItem("FPS", mostrarFps, VerdeNeon) { mostrarFps = it }
        ToggleItem("CPU", mostrarCpu, AzulNeon) { mostrarCpu = it }
        ToggleItem("Temperatura", mostrarTemp, AmareloWarning) { mostrarTemp = it }
        ToggleItem("RAM", mostrarRam, VioletaNeon) { mostrarRam = it }

        Spacer(modifier = Modifier.height(20.dp))

        // Slider opacidade
        Text(
            text = "OPACIDADE: ${(opacidade * 100).toInt()}%",
            color = TextoSecundario,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = opacidade,
            onValueChange = { opacidade = it },
            valueRange = 0.3f..1f,
            colors = SliderDefaults.colors(
                thumbColor = VerdeNeon,
                activeTrackColor = VerdeNeon,
                inactiveTrackColor = Borda
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Aviso permissão
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AmareloWarning.copy(alpha = 0.08f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AmareloWarning.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = AmareloWarning, modifier = Modifier.size(16.dp))
                Text(
                    text = "Permissão SYSTEM_ALERT_WINDOW necessária. Acesse: Configurações > Apps > Turbo Booster > Exibir sobre outros apps",
                    color = AmareloWarning,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão iniciar/parar
        Button(
            onClick = {
                if (overlayAtivo) {
                    context.stopService(Intent(context, OverlayService::class.java))
                    overlayAtivo = false
                } else {
                    context.startForegroundService(Intent(context, OverlayService::class.java))
                    overlayAtivo = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (overlayAtivo) VermelhoDanger else VerdeNeon
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = if (overlayAtivo) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (overlayAtivo) "PARAR OVERLAY" else "INICIAR OVERLAY",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OverlayMetrica(valor: String, label: String, cor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = valor, color = cor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color(0xFF888888), fontSize = 8.sp)
    }
}

@Composable
private fun ToggleItem(
    titulo: String,
    ativo: Boolean,
    cor: androidx.compose.ui.graphics.Color,
    onChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        color = FundoCard,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Borda)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(cor, androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = titulo,
                color = TextoPrincipal,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = ativo,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = cor,
                    checkedTrackColor = cor.copy(alpha = 0.3f),
                    uncheckedThumbColor = TextoSecundario,
                    uncheckedTrackColor = Borda
                )
            )
        }
    }
}
