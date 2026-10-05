package com.example.habittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.habittracker.dados.AppState
import com.example.habittracker.dados.CategoriaHabito
import com.example.habittracker.dados.DIA_HOJE
import com.example.habittracker.dados.DIAS_NO_MES
import com.example.habittracker.ui.components.CartaoPadrao
import com.example.habittracker.ui.components.TituloSecao
import com.example.habittracker.ui.theme.HabitTrackerTheme

@Composable
fun EstadoDemoScreen(
    estado: AppState,
    modifier: Modifier = Modifier
) {
    val primeiroHabitoId = estado.habitos.firstOrNull()?.id
    val ultimaRecompensaId = estado.recompensas.lastOrNull()?.id

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Estado do app",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Dia $DIA_HOJE de $DIAS_NO_MES · Logcat tag HabitTracker",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))
        TituloSecao("Resumo")
        CartaoPadrao {
            Text("Saldo: ${estado.saldoMinutos} min", color = MaterialTheme.colorScheme.onSurface)
            Text("Hábitos: ${estado.habitos.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Recompensas: ${estado.recompensas.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Registros: ${estado.registros.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Força ${estado.forca} · Vitalidade ${estado.vitalidade} · Sabedoria ${estado.sabedoria}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Foco ${estado.foco} · Disciplina ${estado.disciplina}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        TituloSecao("Ações (Log.d)")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    estado.adicionarHabito("Caminhar 10 min", CategoriaHabito.MOVIMENTO)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adicionar hábito")
            }
            OutlinedButton(
                onClick = { primeiroHabitoId?.let(estado::concluirHabito) },
                modifier = Modifier.fillMaxWidth(),
                enabled = primeiroHabitoId != null
            ) {
                Text("Concluir / desmarcar 1º hábito")
            }
            OutlinedButton(
                onClick = { primeiroHabitoId?.let(estado::registrarRecaida) },
                modifier = Modifier.fillMaxWidth(),
                enabled = primeiroHabitoId != null
            ) {
                Text("Registrar recaída")
            }
            OutlinedButton(
                onClick = { primeiroHabitoId?.let { estado.registrarSessaoFoco(it, 25) } },
                modifier = Modifier.fillMaxWidth(),
                enabled = primeiroHabitoId != null
            ) {
                Text("Registrar foco 25 min")
            }
            OutlinedButton(
                onClick = { ultimaRecompensaId?.let(estado::resgatarRecompensa) },
                modifier = Modifier.fillMaxWidth(),
                enabled = ultimaRecompensaId != null
            ) {
                Text("Resgatar última recompensa")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { estado.habitos.lastOrNull()?.id?.let(estado::removerHabito) },
                    modifier = Modifier.weight(1f),
                    enabled = estado.habitos.isNotEmpty()
                ) {
                    Text("Remover último")
                }
                OutlinedButton(
                    onClick = { estado.resetarDadosExemplo() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Resetar")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        TituloSecao("Último Log.d")
        CartaoPadrao {
            Text(
                text = estado.ultimaMensagem.ifBlank { "Nenhuma ação ainda." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121410, showSystemUi = true)
@Composable
private fun EstadoDemoPreview() {
    HabitTrackerTheme {
        EstadoDemoScreen(estado = AppState())
    }
}
