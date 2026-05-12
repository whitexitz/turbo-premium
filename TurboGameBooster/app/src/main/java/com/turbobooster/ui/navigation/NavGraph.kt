package com.turbobooster.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.turbobooster.ui.screens.*
import com.turbobooster.ui.theme.*
import com.turbobooster.viewmodel.MainViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val vm: MainViewModel = viewModel()
    val state by vm.state.collectAsState()
    val overlayAtivo by vm.overlayAtivo.collectAsState()
    val mensagem by vm.mensagem.collectAsState()

    val tabs = listOf(
        Triple("home", "Home", Icons.Default.Home),
        Triple("otimizar", "Boost", Icons.Default.FlashOn),
        Triple("jogos", "Jogos", Icons.Default.SportsEsports),
        Triple("overlay", "Overlay", Icons.Default.Visibility)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = BgCard, tonalElevation = 0.dp) {
                val back by navController.currentBackStackEntryAsState()
                val atual = back?.destination?.route
                tabs.forEach { (rota, label, icone) ->
                    NavigationBarItem(
                        selected = atual == rota,
                        onClick = {
                            navController.navigate(rota) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icone, label) },
                        label = { Text(label, fontSize = androidx.compose.ui.unit.TextUnit.Unspecified) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonGreen,
                            selectedTextColor = NeonGreen,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = NeonGreen.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        },
        containerColor = BgDeep
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(state = state, onTurboClick = vm::turboBoost)
            }
            composable("otimizar") {
                OtimizarScreen(
                    onLimparCache = vm::limparCache,
                    onLiberarRam = vm::liberarRam,
                    onMatar = vm::matarBackground,
                    onRestaurar = vm::restaurar,
                    mensagem = mensagem
                )
            }
            composable("jogos") { JogosScreen() }
            composable("overlay") {
                OverlayScreen(
                    state = state,
                    overlayAtivo = overlayAtivo,
                    onToggleOverlay = vm::toggleOverlay
                )
            }
        }
    }
}
