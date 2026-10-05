package com.example.habittracker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.habittracker.dados.AppState
import com.example.habittracker.ui.components.TelaStub

@Composable
fun DetalheRecompensaScreen(
    recompensaId: Int,
    estado: AppState,
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recompensa = estado.recompensaPorId(recompensaId)
    val falta = if (recompensa == null) {
        0
    } else {
        (recompensa.custoMinutos - estado.saldoMinutos).coerceAtLeast(0)
    }
    TelaStub(
        titulo = recompensa?.nome ?: "Recompensa",
        modifier = modifier,
        onVoltar = onVoltar,
        texto = if (recompensa == null) {
            "Recompensa $recompensaId não encontrada."
        } else {
            "Custa ${recompensa.custoMinutos} min · saldo ${estado.saldoMinutos} min · faltam $falta min."
        }
    )
}
