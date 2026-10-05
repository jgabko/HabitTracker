package com.example.habittracker.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.habittracker.dados.AppState
import com.example.habittracker.dados.DIA_HOJE
import com.example.habittracker.ui.components.AcaoStub
import com.example.habittracker.ui.components.TelaStub

@Composable
fun InicioScreen(
    estado: AppState,
    onVerTodos: () -> Unit,
    onNovoHabito: () -> Unit,
    onConfiguracoes: () -> Unit,
    onDetalheHabito: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primeiroId = estado.habitos.firstOrNull()?.id
    TelaStub(
        titulo = "Início",
        modifier = modifier,
        texto = "Dia $DIA_HOJE · saldo ${estado.saldoMinutos} min · ${estado.habitos.size} hábitos.",
        acoesTopo = {
            IconButton(onClick = onConfiguracoes) {
                Icon(Icons.Filled.Settings, contentDescription = "Configurações")
            }
        },
        acoes = listOfNotNull(
            AcaoStub("Ver todos", onClick = onVerTodos),
            AcaoStub("Novo hábito", primaria = true, onClick = onNovoHabito),
            primeiroId?.let { id ->
                AcaoStub("Detalhe do 1º hábito") { onDetalheHabito(id) }
            }
        )
    )
}
