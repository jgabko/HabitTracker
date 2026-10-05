package com.example.habittracker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.habittracker.ui.components.TelaStub

@Composable
fun ConfiguracoesScreen(
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    TelaStub(
        titulo = "Configurações",
        modifier = modifier,
        onVoltar = onVoltar,
        texto = "Switch, Sobre e resetar dados entram na Etapa 13."
    )
}
