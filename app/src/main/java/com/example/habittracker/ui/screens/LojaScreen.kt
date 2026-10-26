package com.example.habittracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.habittracker.dados.AppState
import com.example.habittracker.ui.components.TelaDados

@Composable
fun LojaScreen(
    estado: AppState,
    onDetalheRecompensa: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { estado.carregarDados() }
    var nome by rememberSaveable { mutableStateOf("") }
    var custo by rememberSaveable { mutableStateOf("") }
    val minutos = custo.toIntOrNull()
    TelaDados("Loja", estado, modifier) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Saldo: " + estado.saldoMinutos + " min")
                    OutlinedTextField(nome, { nome = it }, label = { Text("Nome da recompensa") }, singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = !estado.salvando)
                    OutlinedTextField(custo, { custo = it }, label = { Text("Custo em minutos") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), enabled = !estado.salvando, isError = custo.isNotBlank() && (minutos == null || minutos <= 0))
                    Button(
                        onClick = { estado.adicionarRecompensa(nome, minutos ?: 0) { nome = ""; custo = "" } },
                        enabled = nome.isNotBlank() && minutos != null && minutos > 0 && !estado.salvando && !estado.carregando,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Adicionar recompensa") }
                }
            }
            if (!estado.carregando && estado.recompensas.isEmpty()) item {
                Text("Nenhuma recompensa cadastrada.")
            }
            items(estado.recompensas, key = { it.id }) { recompensa ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp)) {
                        Column(Modifier.weight(1f).clickable { onDetalheRecompensa(recompensa.id) }.padding(8.dp)) {
                            Text(recompensa.nome, style = MaterialTheme.typography.titleMedium)
                            Text(recompensa.custoMinutos.toString() + " min")
                            Text("Toque para ver ou editar", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { estado.removerRecompensa(recompensa.id) }, enabled = !estado.salvando) {
                            Icon(Icons.Default.Delete, "Remover recompensa " + recompensa.nome)
                        }
                    }
                }
            }
        }
    }
}
