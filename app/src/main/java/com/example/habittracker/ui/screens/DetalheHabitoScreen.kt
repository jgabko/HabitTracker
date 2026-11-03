package com.example.habittracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CancellationException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habittracker.viewmodel.*
import androidx.compose.runtime.collectAsState
import com.example.habittracker.data.Categoria
import com.example.habittracker.data.Habito
import com.example.habittracker.data.Periodo
import com.example.habittracker.data.TipoHabito
import com.example.habittracker.navigation.Rotas
import com.example.habittracker.ui.components.BarraProgresso
import com.example.habittracker.ui.components.BotaoPrincipal
import com.example.habittracker.ui.components.BotaoSecundario
import com.example.habittracker.ui.components.CartaoEstatistica
import com.example.habittracker.ui.components.CartaoPadrao
import com.example.habittracker.ui.components.EmojiCirculo
import com.example.habittracker.ui.components.OpcoesChips
import com.example.habittracker.ui.components.SecaoCampo
import com.example.habittracker.ui.components.SeletorDiasDaSemana
import kotlin.math.roundToInt

// Quanto o hábito rende (em minutos de tempo livre) a cada conclusão
private val opcoesMinutos = listOf(5, 10, 15, 20)

private val abreviacaoDias = listOf("dom", "seg", "ter", "qua", "qui", "sex", "sáb")

private fun textoDosDias(dias: List<Int>): String =
    if (dias.size == 7) "Todos os dias" else dias.sorted().joinToString(", ") { abreviacaoDias[it] }

/** O id veio pela rota; o hábito certo é buscado no HabitosUiState. */
@Composable
fun DetalheHabitoScreen(
    navController: NavController,
    viewModel: HabitosViewModel,
    habitoId: Int,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val estado = uiState
    if (uiState.carregando) {
        Text("Carregando…", modifier = Modifier.padding(20.dp))
        return
    }
    val habito = estado.habitoPorId(habitoId)
    val atual = habito
    if (atual == null) {
        // Acontece, por exemplo, se o hábito foi removido enquanto a tela estava aberta
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Hábito não encontrado",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            BotaoSecundario(texto = "Voltar", aoClicar = { navController.popBackStack() })
        }
    } else {
        DetalheConteudo(navController, viewModel, atual, modifier)
    }
}

@Composable
private fun DetalheConteudo(
    navController: NavController,
    viewModel: HabitosViewModel,
    habito: Habito,
    modifier: Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val estado = uiState
    val evitar = habito.tipo == TipoHabito.EVITAR
    val taxa = estado.taxaUltimos7Dias(habito.id)

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EmojiCirculo(emoji = habito.emoji, tamanho = 64.dp)
            Column {
                Text(
                    text = habito.nome,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${habito.categoria.rotulo} · ${habito.tipo.rotulo} · ${habito.periodo.rotulo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${textoDosDias(habito.diasSemana)} · +${habito.minutosRecompensa} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Estatísticas
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (evitar) {
                CartaoEstatistica(modifier = Modifier.weight(1f), valor = "${estado.diasSem(habito)}", rotulo = "Dias sem")
            } else {
                CartaoEstatistica(modifier = Modifier.weight(1f), valor = "${estado.ofensivaAtual(habito.id)}", rotulo = "Ofensiva atual")
            }
            CartaoEstatistica(modifier = Modifier.weight(1f), valor = "${estado.melhorOfensiva(habito.id)}", rotulo = "Melhor ofensiva")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoEstatistica(modifier = Modifier.weight(1f), valor = "${estado.totalConclusoes(habito.id)}", rotulo = "Total de conclusões")
            CartaoEstatistica(modifier = Modifier.weight(1f), valor = "${estado.minutosFocoDoHabito(habito.id)} min", rotulo = "Tempo de foco")
        }
        CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Últimos 7 dias",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(taxa * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            BarraProgresso(progresso = taxa)
        }

        // Ações
        BotaoPrincipal(
            texto = "Focar neste hábito",
            aoClicar = { navController.navigate(Rotas.focoDoHabito(habito.id)) }
        )
        if (evitar) {
            BotaoSecundario(
                texto = "Registrar recaída",
                aoClicar = { viewModel.registrarRecaida(habito.id) }
            )
        }

        // Edição na própria tela (os campos começam com os valores atuais do hábito)
        EdicaoDoHabito(viewModel = viewModel, habito = habito)
        com.example.habittracker.ui.components.BotaoRemover(
            "Remover hábito", "O hábito será excluído. O histórico será preservado."
        ) { viewModel.removerHabito(habito.id) { navController.popBackStack() } }
    }
}

@Composable
private fun EdicaoDoHabito(viewModel: HabitosViewModel, habito: Habito) {
    val uiState by viewModel.uiState.collectAsState()
    val estado = uiState
    LaunchedEffect(habito.id) { viewModel.onEditar(habito) }
    val form = uiState
    val nome = form.nome
    val categoria = form.categoria
    val periodo = form.periodo
    val dias = form.dias
    val minutos = form.minutos
    val salvo = form.salvo
    val nomeInvalido = form.nomeInvalido
    val diasInvalidos = form.diasInvalidos

    CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Editar hábito",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedTextField(
                value = nome,
                onValueChange = {
                    viewModel.onNomeChange(it)
                },
                label = { Text("Nome do hábito") },
                singleLine = true,
                isError = nomeInvalido,
                supportingText = { if (nomeInvalido) Text("Digite um nome para o hábito") },
                modifier = Modifier.fillMaxWidth()
            )
            SecaoCampo(titulo = "Categoria") {
                OpcoesChips(
                    rotulos = Categoria.entries.map { "${it.emoji} ${it.rotulo}" },
                    selecionado = Categoria.entries.indexOf(categoria),
                    aoSelecionar = {
                        viewModel.onCategoriaChange(Categoria.entries[it])
                    }
                )
            }
            SecaoCampo(titulo = "Dias da semana") {
                SeletorDiasDaSemana(
                    selecionados = dias,
                    aoAlternar = { dia ->
                        viewModel.onDiaAlternar(dia)
                    }
                )
                if (diasInvalidos) {
                    Text(
                        text = "Escolha pelo menos um dia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            SecaoCampo(titulo = "Período") {
                OpcoesChips(
                    rotulos = Periodo.entries.map { "${it.emoji} ${it.rotulo}" },
                    selecionado = Periodo.entries.indexOf(periodo),
                    aoSelecionar = {
                        viewModel.onPeriodoChange(Periodo.entries[it])
                    }
                )
            }
            SecaoCampo(titulo = "Recompensa por conclusão") {
                OpcoesChips(
                    rotulos = opcoesMinutos.map { "$it min" },
                    selecionado = opcoesMinutos.indexOf(minutos),
                    aoSelecionar = {
                        viewModel.onMinutosChange(opcoesMinutos[it])
                    }
                )
            }
            BotaoSecundario(
                texto = "Salvar alterações",
                aoClicar = {
                    viewModel.onSalvar()
                }
            )
            if (salvo) {
                Text(
                    text = "✓ Alterações salvas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
