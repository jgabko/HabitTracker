package com.example.habittracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
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
import com.example.habittracker.model.Categoria
import com.example.habittracker.model.Habito
import com.example.habittracker.navigation.Rotas
import com.example.habittracker.ui.components.BotaoPrincipal
import com.example.habittracker.ui.components.CartaoPadrao
import com.example.habittracker.ui.components.EmojiCirculo
import com.example.habittracker.ui.components.OpcoesChips
import com.example.habittracker.ui.components.TituloSecao

/**
 * Lista 1 (LazyColumn + Card): formulário inline no topo (nome + categoria + Adicionar),
 * e abaixo todos os hábitos, cada um com Checkbox (concluir hoje) e ícone de remover.
 * Tocar no card abre o Detalhe do Hábito.
 */
@Composable
fun HabitosScreen(navController: NavController, estado: AppState, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) { estado.carregarDados() }
    var nome by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(Categoria.MENTE) }
    var erro by remember { mutableStateOf(false) }

    fun adicionar() {
        if (nome.isBlank()) {
            erro = true
            return
        }
        estado.adicionarHabito(nome = nome, categoria = categoria, onSucesso = {
            nome = ""
            erro = false
        })
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FormularioRapido(
                nome = nome,
                aoMudarNome = {
                    nome = it
                    erro = false
                },
                erro = erro,
                categoria = categoria,
                aoMudarCategoria = { categoria = it },
                aoAdicionar = { adicionar() }
            )
        }
        item {
            TituloSecao(
                titulo = "Todos os hábitos (${estado.habitos.size})",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (estado.habitos.isEmpty()) {
            item {
                Text(
                    text = "Nenhum hábito cadastrado. Use o formulário acima para criar o primeiro.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(estado.habitos, key = { it.id }) { habito ->
                CardHabito(
                    habito = habito,
                    concluido = estado.concluidoHoje(habito.id),
                    aoAlternar = { estado.alternarConclusaoHoje(habito.id) },
                    aoRemover = { estado.removerHabito(habito.id) },
                    aoAbrir = { navController.navigate(Rotas.detalheHabito(habito.id)) }
                )
            }
        }
        item {
            // O formulário rápido usa valores padrão; o completo fica na tela "Novo Hábito"
            Spacer(modifier = Modifier.height(4.dp))
            TituloSecao(
                titulo = "Precisa de mais opções?",
                textoAcao = "Formulário completo",
                aoClicarAcao = { navController.navigate(Rotas.NOVO_HABITO) }
            )
        }
    }
}

@Composable
private fun FormularioRapido(
    nome: String,
    aoMudarNome: (String) -> Unit,
    erro: Boolean,
    categoria: Categoria,
    aoMudarCategoria: (Categoria) -> Unit,
    aoAdicionar: () -> Unit
) {
    CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Adicionar hábito",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = nome,
            onValueChange = aoMudarNome,
            label = { Text("Nome do hábito") },
            singleLine = true,
            isError = erro,
            supportingText = { if (erro) Text("Digite um nome para o hábito") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Categoria",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        OpcoesChips(
            rotulos = Categoria.entries.map { "${it.emoji} ${it.rotulo}" },
            selecionado = Categoria.entries.indexOf(categoria),
            aoSelecionar = { aoMudarCategoria(Categoria.entries[it]) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        BotaoPrincipal(texto = "Adicionar", aoClicar = aoAdicionar)
    }
}

@Composable
private fun CardHabito(
    habito: Habito,
    concluido: Boolean,
    aoAlternar: () -> Unit,
    aoRemover: () -> Unit,
    aoAbrir: () -> Unit
) {
    CartaoPadrao(modifier = Modifier.fillMaxWidth(), aoClicar = aoAbrir) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmojiCirculo(emoji = habito.emoji, tamanho = 44.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habito.nome,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${habito.categoria.rotulo} · ${habito.tipo.rotulo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Checkbox(
                checked = concluido,
                onCheckedChange = { aoAlternar() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            IconButton(onClick = aoRemover) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remover ${habito.nome}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
