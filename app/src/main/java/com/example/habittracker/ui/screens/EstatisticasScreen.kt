package com.example.habittracker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.habittracker.ui.components.TelaStub

@Composable
fun EstatisticasScreen(
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    TelaStub(
        titulo = "Estatísticas",
        modifier = modifier,
        onVoltar = onVoltar,
        texto = "Gráficos e totais entram na Etapa 12."
    )
}
