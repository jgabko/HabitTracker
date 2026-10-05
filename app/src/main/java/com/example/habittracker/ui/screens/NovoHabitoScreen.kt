package com.example.habittracker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.habittracker.ui.components.TelaStub

@Composable
fun NovoHabitoScreen(
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    TelaStub(
        titulo = "Novo hábito",
        modifier = modifier,
        onVoltar = onVoltar,
        texto = "Formulário completo (dias e período) entra na Etapa 6."
    )
}
