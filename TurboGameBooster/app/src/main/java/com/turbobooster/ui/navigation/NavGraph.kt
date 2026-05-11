package com.turbobooster.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.turbobooster.ui.screens.*
import com.turbobooster.ui.theme.FundoCard
import com.turbobooster.ui.theme.VerdeNeon
import com.turbobooster.viewmodel.MainViewModel

sealed class Tela(val rota: String, val titulo: String, val icone: ImageVector) {
    object Home : Tela("home", "Home", Icons.Default.Home)
    object Otimizar : Tela("otimizar", "Otimizar", Icons.Default.FlashOn)
    object Jogos : Tela("jogos", "Jogos", Icons.Default.SportsEsports)
    object Overlay : Tela("overlay", "Overlay", Icons.Default.BarChart)
    object Shizuku : Tela("shizuku", "Shizuku", Icons.Default.Settings)
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    val telas = listOf(
        Tela.Home, Tela.Otimizar, Tela.Jogos, Tela.Overlay, Tela.Shizuku
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = FundoCard) {
                val backStack by navController.currentBackStackEntryAsState()
                val destinoAtual = backStack?.destination

                telas.forEach { tela ->
                    NavigationBarItem(
                        selected = destinoAtual?.hierarchy?.any { it.route == tela.rota } == true,
                        onClick = {
                            navController.navigate(tela.rota) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tela.icone, contentDescription = tela.titulo) },
                        label = { Text(tela.titulo, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VerdeNeon,
                            selectedTextColor = VerdeNeon,
                            indicatorColor = FundoCard
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tela.Home.rota,
            modifier = Modifier.padding(padding)
        ) {
            composable(Tela.Home.rota) { HomeScreen(viewModel) }
            composable(Tela.Otimizar.rota) { OtimizarScreen(viewModel) }
            composable(Tela.Jogos.rota) { JogosScreen(viewModel) }
            composable(Tela.Overlay.rota) { OverlayScreen(viewModel) }
            composable(Tela.Shizuku.rota) { ShizukuScreen(viewModel) }
        }
    }
}
