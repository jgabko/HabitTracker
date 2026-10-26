package com.example.habittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habittracker.data.AppState
import com.example.habittracker.model.TipoHabito
import com.example.habittracker.navigation.Rotas
import com.example.habittracker.ui.components.BotaoPrincipal
import com.example.habittracker.ui.components.CartaoPadrao
import com.example.habittracker.ui.components.EmojiCirculo
import com.example.habittracker.ui.components.TituloSecao

private val letrasDosDias = listOf("D", "S", "T", "Q", "Q", "S", "S")

/**
 * Progresso (Figma progresso): ofensiva atual, calendário e "dias sem" por hábito,
 * com botão para as Estatísticas.
 *
 * O calendário é só um placeholder visual: mês fictício de 30 dias, colorido pela
 * quantidade de atividades de cada dia. Não tem navegação entre meses nem toque no dia.
 */
@Composable
fun ProgressoScreen(navController: NavController, estado: AppState, modifier: Modifier = Modifier) {
    val habitosEvitar = estado.habitos.filter { it.tipo == TipoHabito.EVITAR }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ofensiva atual
        CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Ofensiva atual",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "🔥 ${estado.ofensivaGeral()} dias",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "Dias seguidos com pelo menos uma atividade.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Calendário (placeholder)
        CalendarioPlaceholder(estado)

        // Dias sem, por hábito do tipo "evitar"
        TituloSecao(titulo = "Dias sem")
        if (habitosEvitar.isEmpty()) {
            Text(
                text = "Você ainda não tem hábitos do tipo Evitar. Crie um em Novo Hábito.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            habitosEvitar.forEach { habito ->
                CartaoPadrao(
                    modifier = Modifier.fillMaxWidth(),
                    aoClicar = { navController.navigate(Rotas.detalheHabito(habito.id)) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        EmojiCirculo(emoji = habito.emoji, tamanho = 44.dp)
                        Text(
                            text = habito.nome,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${estado.diasSem(habito)} dias",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        BotaoPrincipal(
            texto = "Ver estatísticas",
            aoClicar = { navController.navigate(Rotas.ESTATISTICAS) }
        )
    }
}

@Composable
private fun CalendarioPlaceholder(estado: AppState) {
    // Dia 1 do mês fictício cai numa segunda-feira, então a primeira semana começa com 1 célula vazia
    val vazios = estado.diaDaSemana(1)
    val celulas: List<Int?> = List(vazios) { null } + (1..AppState.DIAS_NO_MES).toList()
    val semanas = celulas.chunked(7)

    CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Calendário",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Mês fictício de ${AppState.DIAS_NO_MES} dias · mais verde = mais atividades",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            letrasDosDias.forEach { letra ->
                Text(
                    text = letra,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        semanas.forEach { semana ->
            Row(modifier = Modifier.fillMaxWidth()) {
                semana.forEach { dia ->
                    CelulaDia(modifier = Modifier.weight(1f), dia = dia, estado = estado)
                }
                // Completa a última semana para as células manterem a mesma largura
                repeat(7 - semana.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CelulaDia(modifier: Modifier, dia: Int?, estado: AppState) {
    val celula = modifier
        .aspectRatio(1f)
        .padding(2.dp)

    if (dia == null) {
        Spacer(modifier = celula)
        return
    }

    val futuro = dia > estado.diaHoje
    val atividades = estado.contagemNoDia(dia)
    val cor: Color = when {
        futuro || atividades == 0 -> MaterialTheme.colorScheme.surfaceContainerHigh
        atividades <= 2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        atividades <= 4 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.primary
    }
    val corTexto = when {
        futuro -> MaterialTheme.colorScheme.outline
        atividades >= 5 -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val forma = RoundedCornerShape(8.dp)
    val destaqueHoje = if (dia == estado.diaHoje) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.secondary, forma)
    } else {
        Modifier
    }

    Box(
        modifier = celula
            .clip(forma)
            .background(cor)
            .then(destaqueHoje),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$dia",
            style = MaterialTheme.typography.labelLarge,
            color = corTexto
        )
    }
}
