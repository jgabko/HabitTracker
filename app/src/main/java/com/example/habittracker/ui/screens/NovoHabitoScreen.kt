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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habittracker.data.AppState
import com.example.habittracker.model.Categoria
import com.example.habittracker.model.Periodo
import com.example.habittracker.model.TipoHabito
import com.example.habittracker.ui.components.BotaoPrincipal
import com.example.habittracker.ui.components.BotaoSecundario
import com.example.habittracker.ui.components.OpcoesChips
import com.example.habittracker.ui.components.SecaoCampo
import com.example.habittracker.ui.components.SeletorDiasDaSemana

/**
 * Novo Hábito (Figma criar-habito): formulário completo com nome, tipo, categoria,
 * dias da semana e período. "Salvar" grava no AppState (mesma lista da tela de Hábitos).
 */
@Composable
fun NovoHabitoScreen(navController: NavController, estado: AppState, modifier: Modifier = Modifier) {
    var nome by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoHabito.FAZER) }
    var categoria by remember { mutableStateOf(Categoria.MENTE) }
    var dias by remember { mutableStateOf(listOf(0, 1, 2, 3, 4, 5, 6)) }
    var periodo by remember { mutableStateOf(Periodo.MANHA) }
    var tentouSalvar by remember { mutableStateOf(false) }

    val nomeInvalido = tentouSalvar && nome.isBlank()
    val diasInvalidos = tentouSalvar && dias.isEmpty()

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
            onValueChange = { nome = it },
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
                aoSelecionar = { tipo = TipoHabito.entries[it] }
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
                aoSelecionar = { categoria = Categoria.entries[it] }
            )
        }

        SecaoCampo(titulo = "Dias da semana") {
            SeletorDiasDaSemana(
                selecionados = dias,
                aoAlternar = { dia ->
                    dias = if (dia in dias) dias - dia else (dias + dia).sorted()
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
                aoSelecionar = { periodo = Periodo.entries[it] }
            )
        }

        BotaoPrincipal(
            texto = "Salvar hábito",
            aoClicar = {
                tentouSalvar = true
                if (nome.isNotBlank() && dias.isNotEmpty()) {
                    estado.adicionarHabito(
                        nome = nome,
                        categoria = categoria,
                        tipo = tipo,
                        periodo = periodo,
                        diasSemana = dias,
                        onSucesso = { navController.popBackStack() }
                    )
                }
            }
        )
        BotaoSecundario(texto = "Cancelar", aoClicar = { navController.popBackStack() })
    }
}
