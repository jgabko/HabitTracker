package com.example.habittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Índice = dia da semana (0 = domingo ... 6 = sábado), igual ao Habito.diasSemana
private val letrasDosDias = listOf("D", "S", "T", "Q", "Q", "S", "S")

/** Sete círculos (D S T Q Q S S) que o usuário toca para marcar os dias do hábito. */
@Composable
fun SeletorDiasDaSemana(
    selecionados: List<Int>,
    aoAlternar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        letrasDosDias.forEachIndexed { dia, letra ->
            val ativo = dia in selecionados
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (ativo) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    .clickable { aoAlternar(dia) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letra,
                    fontWeight = FontWeight.SemiBold,
                    color = if (ativo) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
