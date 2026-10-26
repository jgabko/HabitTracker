package com.example.habittracker.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.habittracker.dados.AppState
import com.example.habittracker.ui.components.FormularioHabito
import com.example.habittracker.ui.components.TelaDados

@Composable
fun NovoHabitoScreen(estado: AppState, onVoltar: () -> Unit, modifier: Modifier = Modifier) {
    TelaDados("Novo hábito", estado, modifier, onVoltar) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            FormularioHabito(habilitado = !estado.salvando && !estado.carregando) { habito ->
                estado.adicionarHabito(
                    habito.nome, habito.categoria, habito.diasDaSemana,
                    habito.periodo, habito.atributo, habito.minutosGanho,
                    onSucesso = onVoltar
                )
            }
        }
    }
}
