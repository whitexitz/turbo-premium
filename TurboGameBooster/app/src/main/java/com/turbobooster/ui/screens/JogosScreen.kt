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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbobooster.service.GameDetectorService
import com.turbobooster.ui.theme.*
import com.turbobooster.viewmodel.MainViewModel

@Composable
fun JogosScreen(viewModel: MainViewModel) {
    val jogoAtivo by GameDetectorService.jogoAtivo.collectAsState()
    var jogosAtivos by remember { mutableStateOf(GameDetectorService.JOGOS_CONHECIDOS.keys.toMutableSet()) }
    var novoPackage by remember { mutableStateOf("") }
    var jogosCustom by remember { mutableStateOf(mapOf<String, String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FundoPrincipal)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "JOGOS",
            color = VerdeNeon,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Status: jogo ativo agora
        AnimatedVisibility(visible = jogoAtivo != null) {
            jogoAtivo?.let { pkg ->
                val nome = GameDetectorService.JOGOS_CONHECIDOS[pkg] ?: jogosCustom[pkg] ?: pkg
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = VerdeNeon.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, VerdeNeon.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = VerdeNeon, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Jogando agora: $nome",
                            color = VerdeNeon,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Adicionar jogo customizado
        Text(
            text = "ADICIONAR JOGO",
            color = TextoSecundario,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = novoPackage,
                onValueChange = { novoPackage = it },
                placeholder = { Text("com.exemplo.jogo", color = TextoSecundario, fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeNeon,
                    unfocusedBorderColor = Borda,
                    focusedTextColor = TextoPrincipal,
                    unfocusedTextColor = TextoPrincipal,
                    cursorColor = VerdeNeon
                ),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            IconButton(
                onClick = {
                    val pkg = novoPackage.trim()
                    if (pkg.isNotEmpty()) {
                        jogosCustom = jogosCustom + (pkg to pkg.substringAfterLast("."))
                        jogosAtivos = (jogosAtivos + pkg).toMutableSet()
                        novoPackage = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(VerdeNeon.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = VerdeNeon)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista jogos conhecidos
        Text(
            text = "JOGOS CONHECIDOS",
            color = TextoSecundario,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val todosJogos = GameDetectorService.JOGOS_CONHECIDOS + jogosCustom

        todosJogos.entries.forEach { (pkg, nome) ->
            val ativo = jogosAtivos.contains(pkg)
            val esteJogoAtivo = jogoAtivo == pkg

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                color = if (esteJogoAtivo) VerdeNeon.copy(alpha = 0.08f) else FundoCard,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (esteJogoAtivo) VerdeNeon.copy(alpha = 0.4f) else Borda)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = nome,
                                color = TextoPrincipal,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (esteJogoAtivo) {
                                Surface(
                                    color = VerdeNeon.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "ATIVO",
                                        color = VerdeNeon,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = pkg,
                            color = TextoSecundario,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = ativo,
                        onCheckedChange = { checked ->
                            jogosAtivos = if (checked) {
                                (jogosAtivos + pkg).toMutableSet()
                            } else {
                                (jogosAtivos - pkg).toMutableSet()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VerdeNeon,
                            checkedTrackColor = VerdeNeon.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextoSecundario,
                            uncheckedTrackColor = Borda
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
