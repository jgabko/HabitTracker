package com.example.habittracker.ui.navegacao

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habittracker.dados.AppState
import com.example.habittracker.ui.screens.ConfiguracoesScreen
import com.example.habittracker.ui.screens.DetalheHabitoScreen
import com.example.habittracker.ui.screens.DetalheRecompensaScreen
import com.example.habittracker.ui.screens.EstatisticasScreen
import com.example.habittracker.ui.screens.FocoScreen
import com.example.habittracker.ui.screens.InicioScreen
import com.example.habittracker.ui.screens.ListaHabitosScreen
import com.example.habittracker.ui.screens.LojaScreen
import com.example.habittracker.ui.screens.NovoHabitoScreen
import com.example.habittracker.ui.screens.PerfilScreen
import com.example.habittracker.ui.screens.ProgressoScreen

private data class Aba(
    val rota: String,
    val rotulo: String,
    val icone: ImageVector
)

private val Abas = listOf(
    Aba(Rotas.INICIO, "Início", Icons.Filled.Home),
    Aba(Rotas.PROGRESSO, "Progresso", Icons.Filled.DateRange),
    Aba(Rotas.FOCO, "Foco", Icons.Filled.Star),
    Aba(Rotas.LOJA, "Loja", Icons.Filled.ShoppingCart),
    Aba(Rotas.PERFIL, "Perfil", Icons.Filled.Person)
)

@Composable
fun AppNavegacao(
    estado: AppState,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val entradaAtual by navController.currentBackStackEntryAsState()
    val rotaAtual = entradaAtual?.destination?.route
    val mostrarAbas = rotaAtual in RotasDasAbas

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (mostrarAbas) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Abas.forEach { aba ->
                        NavigationBarItem(
                            selected = rotaAtual == aba.rota,
                            onClick = {
                                navController.navigate(aba.rota) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(aba.icone, contentDescription = aba.rotulo) },
                            label = { Text(aba.rotulo) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingAbas ->
        NavHost(
            navController = navController,
            startDestination = Rotas.INICIO,
            modifier = Modifier.padding(paddingAbas)
        ) {
            composable(Rotas.INICIO) {
                InicioScreen(
                    estado = estado,
                    onVerTodos = { navController.navigate(Rotas.HABITOS) },
                    onNovoHabito = { navController.navigate(Rotas.NOVO_HABITO) },
                    onConfiguracoes = { navController.navigate(Rotas.CONFIGURACOES) },
                    onDetalheHabito = { id -> navController.navigate(Rotas.detalheHabito(id)) }
                )
            }
            composable(Rotas.PROGRESSO) {
                ProgressoScreen(
                    onEstatisticas = { navController.navigate(Rotas.ESTATISTICAS) }
                )
            }
            composable(Rotas.FOCO) { FocoScreen() }
            composable(Rotas.LOJA) {
                LojaScreen(
                    estado = estado,
                    onDetalheRecompensa = { id ->
                        navController.navigate(Rotas.detalheRecompensa(id))
                    }
                )
            }
            composable(Rotas.PERFIL) {
                PerfilScreen(
                    estado = estado,
                    onConfiguracoes = { navController.navigate(Rotas.CONFIGURACOES) }
                )
            }
            composable(Rotas.HABITOS) {
                ListaHabitosScreen(
                    estado = estado,
                    onVoltar = { navController.popBackStack() },
                    onDetalheHabito = { id -> navController.navigate(Rotas.detalheHabito(id)) }
                )
            }
            composable(Rotas.NOVO_HABITO) {
                NovoHabitoScreen(onVoltar = { navController.popBackStack() })
            }
            composable(
                route = Rotas.DETALHE_HABITO,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                DetalheHabitoScreen(
                    habitoId = id,
                    estado = estado,
                    onVoltar = { navController.popBackStack() },
                    onFocar = {
                        navController.navigate(Rotas.FOCO) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(
                route = Rotas.DETALHE_RECOMPENSA,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                DetalheRecompensaScreen(
                    recompensaId = id,
                    estado = estado,
                    onVoltar = { navController.popBackStack() }
                )
            }
            composable(Rotas.ESTATISTICAS) {
                EstatisticasScreen(onVoltar = { navController.popBackStack() })
            }
            composable(Rotas.CONFIGURACOES) {
                ConfiguracoesScreen(onVoltar = { navController.popBackStack() })
            }
        }
    }
}
