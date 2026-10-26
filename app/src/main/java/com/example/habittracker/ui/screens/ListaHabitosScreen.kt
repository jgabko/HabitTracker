package com.example.habittracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habittracker.dados.AppState
import com.example.habittracker.dados.CategoriaHabito
import com.example.habittracker.ui.components.TelaDados

@Composable
fun ListaHabitosScreen(
    estado: AppState,
    onVoltar: () -> Unit,
    onDetalheHabito: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { estado.carregarDados() }
    var nome by rememberSaveable { mutableStateOf("") }
    TelaDados("Meus hábitos", estado, modifier, onVoltar) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(nome, { nome = it }, label = { Text("Nome do hábito") }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !estado.salvando)
                    Button(
                        onClick = { estado.adicionarHabito(nome, CategoriaHabito.ROTINA, onSucesso = { nome = "" }) },
                        enabled = nome.isNotBlank() && !estado.salvando && !estado.carregando,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Adicionar hábito") }
                }
            }
            if (!estado.carregando && estado.habitos.isEmpty()) item {
                Text("Nenhum hábito cadastrado. Adicione seu primeiro hábito.")
            }
            items(estado.habitos, key = { it.id }) { habito ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp)) {
                        Checkbox(
                            checked = estado.concluidoNoDia(habito.id),
                            onCheckedChange = { estado.concluirHabito(habito.id) },
                            enabled = !estado.salvando
                        )
                        Column(Modifier.weight(1f).clickable { onDetalheHabito(habito.id) }.padding(8.dp)) {
                            Text(habito.nome, style = MaterialTheme.typography.titleMedium)
                            Text(habito.categoria.rotulo + " · " + habito.minutosGanho + " min")
                            Text("Toque para ver ou editar", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { estado.removerHabito(habito.id) }, enabled = !estado.salvando) {
                            Icon(Icons.Default.Delete, "Remover hábito " + habito.nome)
                        }
                    }
                }
            }
        }
    }
}
