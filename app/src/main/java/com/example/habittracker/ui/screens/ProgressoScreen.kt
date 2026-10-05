package com.example.habittracker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.habittracker.ui.components.AcaoStub
import com.example.habittracker.ui.components.TelaStub

@Composable
fun ProgressoScreen(
    onEstatisticas: () -> Unit,
    modifier: Modifier = Modifier
) {
    TelaStub(
        titulo = "Progresso",
        modifier = modifier,
        texto = "Calendário e ofensiva entram na Etapa 10.",
        acoes = listOf(
            AcaoStub("Estatísticas", primaria = true, onClick = onEstatisticas)
        )
    )
}
