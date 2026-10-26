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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habittracker.data.AppState
import com.example.habittracker.navigation.Rotas
import com.example.habittracker.ui.components.BotaoSecundario
import com.example.habittracker.ui.components.CartaoPadrao

/**
 * Configurações: Switch de notificações (estado real, mas sem notificação de verdade),
 * "Sobre o app" (abre uma tela simples) e "Resetar dados de exemplo".
 */
@Composable
fun ConfiguracoesScreen(navController: NavController, estado: AppState, modifier: Modifier = Modifier) {
    var confirmandoReset by remember { mutableStateOf(false) }
    var resetFeito by remember { mutableStateOf(false) }

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
                    onCheckedChange = { estado.alterarNotificacoes(it) },
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
                    resetFeito = false
                    confirmandoReset = true
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
            onDismissRequest = { confirmandoReset = false },
            title = { Text("Resetar dados?") },
            text = { Text("Hábitos, recompensas, histórico e saldo voltam aos dados de exemplo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        estado.restaurarDadosDeExemplo { resetFeito = true }
                        confirmandoReset = false
                    }
                ) { Text("Resetar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoReset = false }) { Text("Cancelar") }
            }
        )
    }
}
