package com.turbobooster.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.turbobooster.shizuku.ShizukuManager
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
    val context = LocalContext.current

    val startDestination = remember {
        if (hasOverlayPermission(context)) "home" else "permissoes"
    }

    val tabs = listOf(
        Triple("home", "Home", Icons.Default.Home),
        Triple("otimizar", "Boost", Icons.Default.FlashOn),
        Triple("jogos", "Jogos", Icons.Default.SportsEsports),
        Triple("overlay", "Overlay", Icons.Default.Visibility)
    )

    val back by navController.currentBackStackEntryAsState()
    val rotaAtual = back?.destination?.route
    val mostrarBottomBar = rotaAtual != "permissoes"

    Scaffold(
        bottomBar = {
            if (mostrarBottomBar) {
                NavigationBar(containerColor = BgCard, tonalElevation = 0.dp) {
                    tabs.forEach { (rota, label, icone) ->
                        NavigationBarItem(
                            selected = rotaAtual == rota,
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
            }
        },
        containerColor = BgDeep
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable("permissoes") {
                PermissionsScreen(
                    onContinuar = {
                        navController.navigate("home") {
                            popUpTo("permissoes") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") {
                HomeScreen(state = state, onTurboClick = vm::turboBoost)
            }
            composable("otimizar") {
                OtimizarScreen(
                    state = state,
                    onLimparCache = vm::limparCache,
                    onLiberarRam = vm::liberarRam,
                    onMatar = vm::matarBackground,
                    onRestaurar = vm::restaurar,
                    onConectarShizuku = { ShizukuManager.solicitarPermissao() },
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
