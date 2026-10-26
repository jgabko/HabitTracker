package com.example.habittracker.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habittracker.dados.AppState
import com.example.habittracker.dados.Habito
import com.example.habittracker.dados.TAG_APP
import com.example.habittracker.ui.components.FormularioHabito
import com.example.habittracker.ui.components.TelaDados
import kotlinx.coroutines.CancellationException

@Composable
fun DetalheHabitoScreen(
    habitoId: Int, estado: AppState, onVoltar: () -> Unit, onFocar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var habito by remember(habitoId) { mutableStateOf<Habito?>(null) }
    var carregando by remember(habitoId) { mutableStateOf(true) }
    var erro by remember(habitoId) { mutableStateOf<String?>(null) }
    var editando by rememberSaveable(habitoId) { mutableStateOf(false) }
    var excluir by remember { mutableStateOf(false) }
    // A rota entrega somente o ID. O detalhe consulta o DAO, e não a lista em memória.
    LaunchedEffect(habitoId, estado.versaoDados) {
        carregando = true
        erro = null
        try {
            habito = estado.buscarHabitoNoBanco(habitoId)
        } catch (cancelamento: CancellationException) {
            throw cancelamento
        } catch (falha: Exception) {
            Log.e(TAG_APP, "Erro ao buscar hábito por ID", falha)
            erro = "Não foi possível carregar o hábito."
        } finally { carregando = false }
    }
    TelaDados("Detalhe do hábito", estado, modifier, onVoltar) {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val atual = habito
            when {
                carregando -> Text("Carregando hábito…")
                erro != null -> {
                    Text(erro.orEmpty())
                    Button(onClick = { estado.recarregar() }) { Text("Tentar novamente") }
                }
                atual == null -> Text("Hábito não encontrado.")
                editando -> {
                    FormularioHabito(atual, !estado.salvando) { atualizado ->
                        estado.atualizarHabito(atualizado) { editando = false }
                    }
                    OutlinedButton(onClick = { editando = false }, enabled = !estado.salvando) { Text("Cancelar edição") }
                }
                else -> {
                    Text(atual.nome)
                    Text(atual.categoria.rotulo + " · " + atual.periodo.rotulo)
                    Text(atual.diasDaSemana.sortedBy { it.numero }.joinToString { it.rotulo })
                    Text("Ganho por conclusão: " + atual.minutosGanho + " min")
                    Text("Atributo: " + atual.atributo.rotulo)
                    Button(onClick = { editando = true }, enabled = !estado.salvando, modifier = Modifier.fillMaxWidth()) { Text("Editar hábito") }
                    OutlinedButton(onClick = { estado.concluirHabito(habitoId) }, enabled = !estado.salvando) { Text(if (estado.concluidoNoDia(habitoId)) "Desmarcar conclusão" else "Concluir hoje") }
                    OutlinedButton(onClick = onFocar) { Text("Focar neste hábito") }
                    OutlinedButton(onClick = { excluir = true }, enabled = !estado.salvando) { Text("Remover hábito") }
                }
            }
        }
    }
    if (excluir) AlertDialog(
        onDismissRequest = { excluir = false },
        title = { Text("Remover hábito?") },
        text = { Text("O hábito será removido do banco. O histórico de saldo será preservado.") },
        confirmButton = { TextButton(onClick = { excluir = false; estado.removerHabito(habitoId, onVoltar) }) { Text("Remover") } },
        dismissButton = { TextButton(onClick = { excluir = false }) { Text("Cancelar") } }
    )
}
