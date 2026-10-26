package com.example.habittracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habittracker.data.AppState
import com.example.habittracker.model.Habito
import com.example.habittracker.model.TipoHabito
import com.example.habittracker.navigation.Rotas
import com.example.habittracker.ui.components.BarraProgresso
import com.example.habittracker.ui.components.CartaoPadrao
import com.example.habittracker.ui.components.EmojiCirculo
import com.example.habittracker.ui.components.TituloSecao

// Índice = dia da semana (0 = domingo), igual ao AppState.diaDaSemana
private val nomesDosDias = listOf(
    "Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira",
    "Quinta-feira", "Sexta-feira", "Sábado"
)

/**
 * Início (Figma home-dashboard): saudação + engrenagem, card de saldo,
 * hábitos de hoje com check, "Ver todos" e botão "+".
 */
@Composable
fun InicioScreen(navController: NavController, estado: AppState, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) { estado.carregarDados() }
    val habitosHoje = estado.habitosDeHoje()
    val feitos = habitosHoje.count { estado.concluidoHoje(it.id) }
    val nomeDoDia = nomesDosDias[estado.diaDaSemana(estado.diaHoje)]

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // padding embaixo maior para o botão "+" não cobrir o último card
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Cabecalho(
                    subtitulo = "$nomeDoDia · dia ${estado.diaHoje} de ${AppState.DIAS_NO_MES}",
                    aoClicarConfiguracoes = { navController.navigate(Rotas.CONFIGURACOES) }
                )
            }
            item {
                CartaoSaldo(
                    saldoMinutos = estado.saldoMinutos,
                    feitos = feitos,
                    total = habitosHoje.size,
                    ofensiva = estado.ofensivaGeral()
                )
            }
            item {
                TituloSecao(
                    titulo = "Hábitos de hoje",
                    textoAcao = "Ver todos",
                    aoClicarAcao = { navController.navigate(Rotas.HABITOS) },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (habitosHoje.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum hábito para hoje. Toque no + para criar um.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(habitosHoje) { habito ->
                    LinhaHabitoHoje(
                        habito = habito,
                        sequencia = estado.sequenciaDe(habito),
                        concluido = estado.concluidoHoje(habito.id),
                        aoAlternar = { estado.alternarConclusaoHoje(habito.id) },
                        aoAbrir = { navController.navigate(Rotas.detalheHabito(habito.id)) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate(Rotas.NOVO_HABITO) },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Novo hábito")
        }
    }
}

@Composable
private fun Cabecalho(subtitulo: String, aoClicarConfiguracoes: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Olá! 👋",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = aoClicarConfiguracoes) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configurações",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CartaoSaldo(saldoMinutos: Int, feitos: Int, total: Int, ofensiva: Int) {
    CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Tempo livre disponível",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$saldoMinutos min",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hoje: $feitos de $total hábitos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "🔥 $ofensiva dias seguidos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        BarraProgresso(progresso = if (total == 0) 0f else feitos / total.toFloat())
    }
}

@Composable
private fun LinhaHabitoHoje(
    habito: Habito,
    sequencia: Int,
    concluido: Boolean,
    aoAlternar: () -> Unit,
    aoAbrir: () -> Unit
) {
    val detalhe = if (habito.tipo == TipoHabito.EVITAR) {
        "$sequencia dias sem"
    } else {
        "${habito.periodo.rotulo} · 🔥 $sequencia"
    }

    CartaoPadrao(modifier = Modifier.fillMaxWidth(), aoClicar = aoAbrir) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EmojiCirculo(emoji = habito.emoji, tamanho = 44.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habito.nome,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$detalhe · +${habito.minutosRecompensa} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Checkbox(
                checked = concluido,
                onCheckedChange = { aoAlternar() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
