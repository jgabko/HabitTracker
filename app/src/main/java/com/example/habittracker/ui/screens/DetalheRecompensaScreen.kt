package com.example.habittracker.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.habittracker.dados.AppState
import com.example.habittracker.dados.Recompensa
import com.example.habittracker.dados.TAG_APP
import com.example.habittracker.ui.components.TelaDados
import kotlinx.coroutines.CancellationException

@Composable
fun DetalheRecompensaScreen(
    recompensaId: Int, estado: AppState, onVoltar: () -> Unit, modifier: Modifier = Modifier
) {
    var recompensa by remember(recompensaId) { mutableStateOf<Recompensa?>(null) }
    var carregando by remember(recompensaId) { mutableStateOf(true) }
    var erro by remember(recompensaId) { mutableStateOf<String?>(null) }
    var editando by rememberSaveable(recompensaId) { mutableStateOf(false) }
    var nome by rememberSaveable(recompensaId) { mutableStateOf("") }
    var custo by rememberSaveable(recompensaId) { mutableStateOf("") }
    var excluir by remember { mutableStateOf(false) }
    LaunchedEffect(recompensaId, estado.versaoDados) {
        carregando = true
        erro = null
        try {
            recompensa = estado.buscarRecompensaNoBanco(recompensaId)
        } catch (cancelamento: CancellationException) {
            throw cancelamento
        } catch (falha: Exception) {
            Log.e(TAG_APP, "Erro ao buscar recompensa por ID", falha)
            erro = "Não foi possível carregar a recompensa."
        } finally { carregando = false }
    }
    TelaDados("Detalhe da recompensa", estado, modifier, onVoltar) {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val atual = recompensa
            when {
                carregando -> Text("Carregando recompensa…")
                erro != null -> {
                    Text(erro.orEmpty())
                    Button(onClick = { estado.recarregar() }) { Text("Tentar novamente") }
                }
                atual == null -> Text("Recompensa não encontrada.")
                editando -> {
                    val valor = custo.toIntOrNull()
                    OutlinedTextField(nome, { nome = it }, label = { Text("Nome da recompensa") }, singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = !estado.salvando)
                    OutlinedTextField(custo, { custo = it }, label = { Text("Custo em minutos") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), enabled = !estado.salvando)
                    Button(
                        onClick = { estado.atualizarRecompensa(atual.copy(nome = nome, custoMinutos = valor ?: 0)) { editando = false } },
                        enabled = nome.isNotBlank() && valor != null && valor > 0 && !estado.salvando
                    ) { Text("Salvar alterações") }
                    OutlinedButton(onClick = { editando = false }, enabled = !estado.salvando) { Text("Cancelar edição") }
                }
                else -> {
                    Text(atual.nome)
                    Text("Custo: " + atual.custoMinutos + " min")
                    Text("Saldo: " + estado.saldoMinutos + " min")
                    Text("Faltam: " + (atual.custoMinutos - estado.saldoMinutos).coerceAtLeast(0) + " min")
                    Button(onClick = { nome = atual.nome; custo = atual.custoMinutos.toString(); editando = true }, enabled = !estado.salvando) { Text("Editar recompensa") }
                    OutlinedButton(onClick = { estado.resgatarRecompensa(recompensaId) }, enabled = !estado.salvando && estado.saldoMinutos >= atual.custoMinutos) { Text("Resgatar") }
                    OutlinedButton(onClick = { excluir = true }, enabled = !estado.salvando) { Text("Remover recompensa") }
                }
            }
        }
    }
    if (excluir) AlertDialog(
        onDismissRequest = { excluir = false },
        title = { Text("Remover recompensa?") },
        text = { Text("A recompensa será removida do banco.") },
        confirmButton = { TextButton(onClick = { excluir = false; estado.removerRecompensa(recompensaId, onVoltar) }) { Text("Remover") } },
        dismissButton = { TextButton(onClick = { excluir = false }) { Text("Cancelar") } }
    )
}
