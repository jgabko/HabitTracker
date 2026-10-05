package com.example.habittracker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.habittracker.dados.AppState
import com.example.habittracker.ui.components.AcaoStub
import com.example.habittracker.ui.components.TelaStub

@Composable
fun DetalheHabitoScreen(
    habitoId: Int,
    estado: AppState,
    onVoltar: () -> Unit,
    onFocar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habito = estado.habitoPorId(habitoId)
    TelaStub(
        titulo = habito?.nome ?: "Hábito",
        modifier = modifier,
        onVoltar = onVoltar,
        texto = if (habito == null) {
            "Hábito $habitoId não encontrado."
        } else {
            "${habito.categoria.rotulo} · ${habito.periodo.rotulo} · +${habito.minutosGanho} min."
        },
        acoes = listOf(
            AcaoStub("Focar neste hábito", primaria = true, onClick = onFocar)
        )
    )
}
