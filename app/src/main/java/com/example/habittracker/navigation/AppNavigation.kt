package com.example.habittracker.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.habittracker.data.AppState
import com.example.habittracker.ui.screens.ConfiguracoesScreen
import com.example.habittracker.ui.screens.DetalheHabitoScreen
import com.example.habittracker.ui.screens.DetalheRecompensaScreen
import com.example.habittracker.ui.screens.EstatisticasScreen
import com.example.habittracker.ui.screens.FocoScreen
import com.example.habittracker.ui.screens.HabitosScreen
import com.example.habittracker.ui.screens.InicioScreen
import com.example.habittracker.ui.screens.LojaScreen
import com.example.habittracker.ui.screens.NovoHabitoScreen
import com.example.habittracker.ui.screens.PerfilScreen
import com.example.habittracker.ui.screens.ProgressoScreen
import com.example.habittracker.ui.screens.SobreScreen

private data class ItemAba(val rota: String, val rotulo: String, val icone: ImageVector)

private val abas = listOf(
    ItemAba(Rotas.INICIO, "Início", Icons.Default.Home),
    ItemAba(Rotas.PROGRESSO, "Progresso", Icons.Default.DateRange),
    ItemAba(Rotas.FOCO, "Foco", Icons.Default.PlayArrow),
    ItemAba(Rotas.LOJA, "Loja", Icons.Default.ShoppingCart),
    ItemAba(Rotas.PERFIL, "Perfil", Icons.Default.Person)
)

private fun tituloDaRota(rota: String?): String = when (rota) {
    Rotas.PROGRESSO -> "Progresso"
    Rotas.FOCO, Rotas.FOCO_DO_HABITO -> "Foco"
    Rotas.LOJA -> "Recompensas"
    Rotas.PERFIL -> "Perfil"
    Rotas.HABITOS -> "Meus Hábitos"
    Rotas.NOVO_HABITO -> "Novo Hábito"
    Rotas.DETALHE_HABITO -> "Detalhe do Hábito"
    Rotas.DETALHE_RECOMPENSA -> "Detalhe da Recompensa"
    Rotas.ESTATISTICAS -> "Estatísticas"
    Rotas.CONFIGURACOES -> "Configurações"
    Rotas.SOBRE -> "Sobre o app"
    else -> "HabitTracker"
}

/**
 * Raiz do app: Scaffold com TopAppBar (voltar), NavigationBar (5 abas) e o NavHost com todas as rotas.
 * O estado (AppState) é criado na MainActivity e repassado às telas.
 */
@Composable
fun AppNavigation(estado: AppState) {
    val navController = rememberNavController()
    val entradaAtual by navController.currentBackStackEntryAsState()

    // Rota atual (ex.: "inicio", "habito/{id}")
    val rotaAtual = entradaAtual?.destination?.route

    val ehAba = rotaAtual in Rotas.ABAS
    // O Início terá cabeçalho próprio (saudação + engrenagem), então não usa a barra de topo
    val mostrarBarraTopo = rotaAtual != Rotas.INICIO
    // Só as telas internas têm botão de voltar (as abas não)
    val comVoltar = !ehAba

    val snackbarState = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() }
    androidx.compose.runtime.LaunchedEffect(estado.erro) {
        estado.erro?.let {
            snackbarState.showSnackbar(it)
            estado.limparErro()
        }
    }

    androidx.compose.foundation.layout.Box {
    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarState) },
        topBar = {
            if (mostrarBarraTopo) {
                BarraTopo(
                    titulo = tituloDaRota(rotaAtual),
                    comVoltar = comVoltar,
                    aoVoltar = { navController.popBackStack() }
                )
            }
        },
        bottomBar = {
            if (ehAba) {
                BarraInferior(rotaAtual = rotaAtual, navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rotas.INICIO,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Rotas.INICIO) { InicioScreen(navController, estado) }
            composable(Rotas.PROGRESSO) { ProgressoScreen(navController, estado) }
            composable(Rotas.FOCO) { FocoScreen(navController, estado, -1) }
            composable(Rotas.LOJA) { LojaScreen(navController, estado) }
            composable(Rotas.PERFIL) { PerfilScreen(navController, estado) }

            composable(Rotas.FOCO_DO_HABITO) { entrada ->
                // Mesmo esquema da aula: o argumento chega como texto e é convertido para Int
                val id = entrada.arguments?.getString("habitoId")?.toIntOrNull() ?: -1
                FocoScreen(navController, estado, id)
            }
            composable(Rotas.HABITOS) { HabitosScreen(navController, estado) }
            composable(Rotas.NOVO_HABITO) { NovoHabitoScreen(navController, estado) }
            composable(Rotas.DETALHE_HABITO) { entrada ->
                // Mesmo esquema da aula: o argumento chega como texto e é convertido para Int
                val id = entrada.arguments?.getString("id")?.toIntOrNull() ?: -1
                DetalheHabitoScreen(navController, estado, id)
            }
            composable(Rotas.DETALHE_RECOMPENSA) { entrada ->
                val id = entrada.arguments?.getString("id")?.toIntOrNull() ?: -1
                DetalheRecompensaScreen(navController, estado, id)
            }
            composable(Rotas.ESTATISTICAS) { EstatisticasScreen(navController, estado) }
            composable(Rotas.CONFIGURACOES) { ConfiguracoesScreen(navController, estado) }
            composable(Rotas.SOBRE) { SobreScreen(navController) }
        }
    }
    if (estado.carregando || estado.salvando) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize().testTag("operacao-banco").background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
                .clickable(indication = null,
                    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {},
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarraTopo(titulo: String, comVoltar: Boolean, aoVoltar: () -> Unit) {
    TopAppBar(
        title = { Text(text = titulo, style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            if (comVoltar) {
                IconButton(onClick = aoVoltar) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun BarraInferior(rotaAtual: String?, navController: NavController) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        abas.forEach { aba ->
            NavigationBarItem(
                selected = rotaAtual == aba.rota,
                onClick = {
                    navController.navigate(aba.rota) {
                        // Volta até o Início, para não empilhar uma aba sobre a outra
                        popUpTo(Rotas.INICIO)
                        launchSingleTop = true
                    }
                },
                icon = { Icon(imageVector = aba.icone, contentDescription = aba.rotulo) },
                label = { Text(text = aba.rotulo) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
