package com.example.habittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habittracker.data.AppState
import com.example.habittracker.ui.components.BotaoSecundario
import com.example.habittracker.ui.components.CartaoEstatistica
import com.example.habittracker.ui.components.CartaoPadrao
import kotlin.math.roundToInt

private val ALTURA_MAXIMA_BARRA: Dp = 100.dp

/**
 * Estatísticas (versão enxuta e neutra): quatro números e um gráfico de barras
 * desenhado só com Box (atividades por dia nos últimos 7 dias).
 */
@Composable
fun EstatisticasScreen(navController: NavController, estado: AppState, modifier: Modifier = Modifier) {
    val dias = ((estado.diaHoje - 6)..estado.diaHoje).toList()
    val contagens = dias.map { estado.contagemNoDia(it) }
    val maximo = (contagens.maxOrNull() ?: 0).coerceAtLeast(1)

    // Média de atividades por dia, do dia 1 até hoje
    val totalAtividades = (1..estado.diaHoje).sumOf { estado.contagemNoDia(it) }
    val media = (totalAtividades * 10f / estado.diaHoje).roundToInt() / 10f

    val melhorOfensiva = estado.habitos.maxOfOrNull { estado.melhorOfensiva(it.id) } ?: 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoEstatistica(modifier = Modifier.weight(1f), valor = "${estado.minutosFocoTotal()} min", rotulo = "Tempo total de foco")
            CartaoEstatistica(modifier = Modifier.weight(1f), valor = "$media", rotulo = "Média diária de atividades")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoEstatistica(modifier = Modifier.weight(1f), valor = "${taxaDeConclusao(estado)}%", rotulo = "Taxa de conclusão")
            CartaoEstatistica(modifier = Modifier.weight(1f), valor = "$melhorOfensiva dias", rotulo = "Melhor ofensiva")
        }

        // Gráfico de barras com Box
        CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Atividades por dia",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Últimos 7 dias (conclusões e sessões de foco)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ALTURA_MAXIMA_BARRA + 24.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                dias.forEachIndexed { indice, dia ->
                    val quantidade = contagens[indice]
                    val altura = (ALTURA_MAXIMA_BARRA * (quantidade / maximo.toFloat())).coerceAtLeast(3.dp)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "$quantidade",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(altura)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (dia == estado.diaHoje) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                dias.forEach { dia ->
                    Text(
                        text = "$dia",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        BotaoSecundario(texto = "Voltar", aoClicar = { navController.popBackStack() })
    }
}

/** Percentual de conclusões em relação ao previsto (dias agendados de cada hábito, do dia 1 até hoje). */
private fun taxaDeConclusao(estado: AppState): Int {
    var previstos = 0
    var feitos = 0
    estado.habitos.forEach { habito ->
        for (dia in 1..estado.diaHoje) {
            if (estado.diaDaSemana(dia) in habito.diasSemana) {
                previstos++
                if (estado.concluidoEm(habito.id, dia)) feitos++
            }
        }
    }
    return if (previstos == 0) 0 else (feitos * 100f / previstos).roundToInt()
}
