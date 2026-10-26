package com.example.habittracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.habittracker.data.AppState
import com.example.habittracker.model.TipoRegistro
import com.example.habittracker.navigation.Rotas
import com.example.habittracker.ui.components.BarraProgresso
import com.example.habittracker.ui.components.BotaoPrincipal
import com.example.habittracker.ui.components.BotaoSecundario
import com.example.habittracker.ui.components.CartaoPadrao
import com.example.habittracker.ui.components.OpcoesChips
import com.example.habittracker.ui.components.SecaoCampo

private const val PASSO_SIMULADO = 5

// Durações permitidas (em minutos)
private val duracoes = listOf(15, 25, 45)

/**
 * Foco (Pomodoro simulado): escolhe o hábito e a duração, toca em Iniciar e usa "+5 min"
 * para simular a passagem do tempo. Ao completar a duração, a sessão é registrada no AppState.
 * Não usa LaunchedEffect nem delay: o tempo só avança quando o usuário toca no botão.
 *
 * habitoId vem da rota (-1 quando a aba Foco é aberta direto pela barra inferior).
 */
@Composable
fun FocoScreen(
    navController: NavController,
    estado: AppState,
    habitoId: Int,
    modifier: Modifier = Modifier
) {
    // posição (na lista de hábitos) do hábito escolhido; começa no hábito vindo da rota, se houver
    var posicaoHabito by remember { mutableStateOf(estado.habitos.indexOfFirst { it.id == habitoId }.coerceAtLeast(0)) }
    var duracao by remember { mutableStateOf(25) }
    var emAndamento by remember { mutableStateOf(false) }
    var decorridos by remember { mutableStateOf(0) }
    var concluida by remember { mutableStateOf(false) }

    // Se a lista encolheu (hábito removido), volta para o primeiro
    val posicaoValida = if (posicaoHabito < estado.habitos.size) posicaoHabito else 0
    val habito = estado.habitos.getOrNull(posicaoValida)
    val sessoesHoje = estado.registros.filter {
        it.tipo == TipoRegistro.FOCO && it.dia == estado.diaHoje
    }

    fun resetar() {
        emAndamento = false
        concluida = false
        decorridos = 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (habito == null) {
            Text(
                text = "Cadastre um hábito para poder focar nele.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BotaoPrincipal(
                texto = "Criar hábito",
                aoClicar = { navController.navigate(Rotas.NOVO_HABITO) }
            )
        } else {
            // Cronômetro
            CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${habito.emoji} ${habito.nome}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                val restante = duracao - decorridos
                Text(
                    text = "${restante.toString().padStart(2, '0')}:00",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (concluida) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = when {
                        concluida -> "Sessão concluída!"
                        emAndamento -> "$decorridos de $duracao min"
                        else -> "Pronto para começar"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                BarraProgresso(progresso = decorridos / duracao.toFloat())
            }

            if (concluida) {
                CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "🎉 Sessão registrada!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "+${duracao / AppState.MINUTOS_FOCO_POR_MINUTO_LAZER} min de tempo livre · " +
                            "+${duracao / AppState.MINUTOS_FOCO_POR_PONTO} pontos de Foco",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                BotaoPrincipal(texto = "Nova sessão", aoClicar = { resetar() })
            } else if (emAndamento) {
                BotaoPrincipal(
                    texto = "+$PASSO_SIMULADO min",
                    aoClicar = {
                        decorridos = (decorridos + PASSO_SIMULADO).coerceAtMost(duracao)
                        if (decorridos >= duracao) {
                            estado.registrarSessaoFoco(habito.id, duracao) {
                                emAndamento = false
                                concluida = true
                            }
                        }
                    }
                )
                BotaoSecundario(texto = "Resetar", aoClicar = { resetar() })
                Text(
                    text = "Simulação: cada toque em \"+$PASSO_SIMULADO min\" avança o tempo. " +
                        "Resetar cancela a sessão sem registrar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                SecaoCampo(titulo = "Hábito") {
                    OpcoesChips(
                        rotulos = estado.habitos.map { "${it.emoji} ${it.nome}" },
                        selecionado = posicaoValida,
                        aoSelecionar = { posicaoHabito = it }
                    )
                }
                SecaoCampo(titulo = "Duração") {
                    OpcoesChips(
                        rotulos = duracoes.map { "$it min" },
                        selecionado = duracoes.indexOf(duracao),
                        aoSelecionar = { duracao = duracoes[it] }
                    )
                }
                BotaoPrincipal(texto = "Iniciar", aoClicar = { emAndamento = true })
            }

            // Resumo do dia
            CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Foco hoje",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (sessoesHoje.isEmpty()) "Nenhuma sessão ainda."
                    else "${sessoesHoje.sumOf { it.minutos }} min em ${sessoesHoje.size} " +
                        if (sessoesHoje.size == 1) "sessão" else "sessões",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
