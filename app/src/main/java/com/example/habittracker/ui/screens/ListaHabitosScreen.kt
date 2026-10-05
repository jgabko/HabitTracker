package com.example.habittracker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.habittracker.dados.AppState
import com.example.habittracker.ui.components.AcaoStub
import com.example.habittracker.ui.components.TelaStub

@Composable
fun ListaHabitosScreen(
    estado: AppState,
    onVoltar: () -> Unit,
    onDetalheHabito: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primeiroId = estado.habitos.firstOrNull()?.id
    TelaStub(
        titulo = "Hábitos",
        modifier = modifier,
        onVoltar = onVoltar,
        texto = "${estado.habitos.size} hábitos na lista. Formulário inline entra na Etapa 5.",
        acoes = listOfNotNull(
            primeiroId?.let { id ->
                AcaoStub("Abrir detalhe do 1º hábito", primaria = true) { onDetalheHabito(id) }
            }
        )
    )
}
