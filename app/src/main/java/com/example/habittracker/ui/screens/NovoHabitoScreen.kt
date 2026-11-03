package com.example.habittracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habittracker.viewmodel.*
import androidx.compose.runtime.collectAsState
import com.example.habittracker.data.Categoria
import com.example.habittracker.data.Periodo
import com.example.habittracker.data.TipoHabito
import com.example.habittracker.ui.components.BotaoPrincipal
import com.example.habittracker.ui.components.BotaoSecundario
import com.example.habittracker.ui.components.OpcoesChips
import com.example.habittracker.ui.components.SecaoCampo
import com.example.habittracker.ui.components.SeletorDiasDaSemana

/**
 * Novo Hábito (Figma criar-habito): formulário completo com nome, tipo, categoria,
 * dias da semana e período. "Salvar" envia um evento ao ViewModel; a lista observa o Flow do Room.
 */
@Composable
fun NovoHabitoScreen(navController: NavController, viewModel: HabitosViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()
    val estado = uiState.dados
    val form = uiState.formulario
    val nome = form.nome
    val tipo = form.tipo
    val categoria = form.categoria
    val dias = form.dias
    val periodo = form.periodo
    val nomeInvalido = form.nomeInvalido
    val diasInvalidos = form.diasInvalidos

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = nome,
            onValueChange = { viewModel.onNomeChange(it) },
            label = { Text("Nome do hábito") },
            singleLine = true,
            isError = nomeInvalido,
            supportingText = { if (nomeInvalido) Text("Digite um nome para o hábito") },
            modifier = Modifier.fillMaxWidth()
        )

        SecaoCampo(titulo = "Tipo") {
            OpcoesChips(
                rotulos = TipoHabito.entries.map { it.rotulo },
                selecionado = TipoHabito.entries.indexOf(tipo),
                aoSelecionar = { viewModel.onTipoChange(TipoHabito.entries[it]) }
            )
            Text(
                text = if (tipo == TipoHabito.FAZER) "Algo que você quer fazer todo dia."
                else "Algo que você quer parar de fazer (conta os dias sem).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SecaoCampo(titulo = "Categoria") {
            OpcoesChips(
                rotulos = Categoria.entries.map { "${it.emoji} ${it.rotulo}" },
                selecionado = Categoria.entries.indexOf(categoria),
                aoSelecionar = { viewModel.onCategoriaChange(Categoria.entries[it]) }
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
                aoSelecionar = { viewModel.onPeriodoChange(Periodo.entries[it]) }
            )
        }

        BotaoPrincipal(
            texto = "Salvar hábito",
            aoClicar = {
                viewModel.onSalvar { navController.popBackStack() }
            }
        )
        BotaoSecundario(texto = "Cancelar", aoClicar = { viewModel.onCancelarNovo(); navController.popBackStack() })
    }
}
