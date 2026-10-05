package com.example.habittracker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.habittracker.dados.AppState
import com.example.habittracker.ui.components.AcaoStub
import com.example.habittracker.ui.components.TelaStub

@Composable
fun LojaScreen(
    estado: AppState,
    onDetalheRecompensa: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val primeiraId = estado.recompensas.firstOrNull()?.id
    TelaStub(
        titulo = "Loja",
        modifier = modifier,
        texto = "Saldo ${estado.saldoMinutos} min · ${estado.recompensas.size} recompensas.",
        acoes = listOfNotNull(
            primeiraId?.let { id ->
                AcaoStub("Detalhe da 1ª recompensa", primaria = true) { onDetalheRecompensa(id) }
            }
        )
    )
}
