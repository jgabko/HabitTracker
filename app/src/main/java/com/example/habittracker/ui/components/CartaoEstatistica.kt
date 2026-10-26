package com.example.habittracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/** Cartão com um número grande e um rótulo. Dentro de uma Row, use modifier = Modifier.weight(1f). */
@Composable
fun CartaoEstatistica(valor: String, rotulo: String, modifier: Modifier = Modifier) {
    CartaoPadrao(modifier = modifier) {
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
