package com.example.habittracker.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Bloco de formulário: título da seção + conteúdo logo abaixo. */
@Composable
fun SecaoCampo(titulo: String, conteudo: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        conteudo()
    }
}

/**
 * Fileira de chips de escolha única.
 * rotulos: o texto de cada opção; selecionado: posição da opção marcada;
 * aoSelecionar: recebe a posição da opção tocada.
 */
@Composable
fun OpcoesChips(
    rotulos: List<String>,
    selecionado: Int,
    aoSelecionar: (Int) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rotulos.forEachIndexed { posicao, rotulo ->
            FilterChip(
                selected = posicao == selecionado,
                onClick = { aoSelecionar(posicao) },
                label = { Text(rotulo) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
