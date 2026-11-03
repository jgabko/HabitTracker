package com.example.habittracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habittracker.viewmodel.*
import androidx.compose.runtime.collectAsState
import com.example.habittracker.navigation.Rotas
import com.example.habittracker.ui.components.BotaoSecundario
import com.example.habittracker.ui.components.CartaoPadrao

/**
 * Configurações: Switch de notificações (estado real, mas sem notificação de verdade),
 * "Sobre o app" (abre uma tela simples) e "Resetar dados de exemplo".
 */
@Composable
fun ConfiguracoesScreen(navController: NavController, viewModel: HabitosViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsState()
    val estado = uiState.dados
    val confirmandoReset = uiState.confirmandoReset
    val resetFeito = uiState.resetFeito

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notificações (simulado)
        CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notificações diárias",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (estado.notificacoesAtivas) "Ativadas (simulação, sem notificação real)"
                        else "Desativadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = estado.notificacoesAtivas,
                    onCheckedChange = { viewModel.alterarNotificacoes(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Sobre o app
        CartaoPadrao(
            modifier = Modifier.fillMaxWidth(),
            aoClicar = { navController.navigate(Rotas.SOBRE) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sobre o app",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "›",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Resetar dados
        CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Dados do app",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Apaga o que você criou e volta aos dados de exemplo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            BotaoSecundario(
                texto = "Resetar dados de exemplo",
                aoClicar = {
                    viewModel.onConfirmarReset()
                }
            )
            if (resetFeito) {
                Text(
                    text = "✓ Dados de exemplo restaurados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }

    if (confirmandoReset) {
        AlertDialog(
            onDismissRequest = { viewModel.onCancelarReset() },
            title = { Text("Resetar dados?") },
            text = { Text("Hábitos, recompensas, histórico e saldo voltam aos dados de exemplo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onResetarDados()
                    }
                ) { Text("Resetar") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCancelarReset() }) { Text("Cancelar") }
            }
        )
    }
}
