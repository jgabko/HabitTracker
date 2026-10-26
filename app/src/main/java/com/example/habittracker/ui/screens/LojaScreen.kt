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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habittracker.data.AppState
import com.example.habittracker.data.Conquista
import com.example.habittracker.data.listaDeConquistas
import com.example.habittracker.model.Recompensa
import com.example.habittracker.navigation.Rotas
import com.example.habittracker.ui.components.BotaoPrincipal
import com.example.habittracker.ui.components.CartaoPadrao
import com.example.habittracker.ui.components.EmojiCirculo
import com.example.habittracker.ui.components.TituloSecao

/** Loja: duas abas internas, Conquistas (medalhas, só leitura) e Loja (Lista 2 de recompensas). */
@Composable
fun LojaScreen(navController: NavController, estado: AppState, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) { estado.carregarDados() }
    var aba by remember { mutableStateOf(1) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = aba,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            listOf("Conquistas", "Loja").forEachIndexed { indice, titulo ->
                Tab(
                    selected = aba == indice,
                    onClick = { aba = indice },
                    text = { Text(titulo) }
                )
            }
        }
        if (aba == 0) {
            ConquistasAba(estado)
        } else {
            RecompensasAba(navController, estado)
        }
    }
}

// ----------------------------------------------------------------------
// Aba Conquistas
// ----------------------------------------------------------------------

@Composable
private fun ConquistasAba(estado: AppState) {
    val conquistas = listaDeConquistas(estado)
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "${conquistas.count { it.desbloqueada }} de ${conquistas.size} conquistas desbloqueadas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(conquistas) { conquista -> CartaoConquista(conquista) }
        }
    }
}

@Composable
private fun CartaoConquista(conquista: Conquista) {
    CartaoPadrao(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (conquista.desbloqueada) 1f else 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EmojiCirculo(
                emoji = if (conquista.desbloqueada) conquista.emoji else "🔒",
                tamanho = 52.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conquista.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = conquista.descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ----------------------------------------------------------------------
// Aba Loja (Lista 2)
// ----------------------------------------------------------------------

@Composable
private fun RecompensasAba(navController: NavController, estado: AppState) {
    var nome by remember { mutableStateOf("") }
    var custoTexto by remember { mutableStateOf("") }
    var tentouAdicionar by remember { mutableStateOf(false) }

    val custo = custoTexto.toIntOrNull()
    val nomeInvalido = tentouAdicionar && nome.isBlank()
    val custoInvalido = tentouAdicionar && (custo == null || custo <= 0)

    fun adicionar() {
        tentouAdicionar = true
        if (nome.isBlank() || custo == null || custo <= 0) return
        estado.adicionarRecompensa(nome = nome, custoMinutos = custo, onSucesso = {
            nome = ""
            custoTexto = ""
            tentouAdicionar = false
        })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Saldo de tempo livre",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${estado.saldoMinutos} min",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        item {
            CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Adicionar recompensa",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da recompensa") },
                    singleLine = true,
                    isError = nomeInvalido,
                    supportingText = { if (nomeInvalido) Text("Digite um nome") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = custoTexto,
                    onValueChange = { custoTexto = it.filter(Char::isDigit).take(4) },
                    label = { Text("Custo em minutos") },
                    singleLine = true,
                    isError = custoInvalido,
                    supportingText = { if (custoInvalido) Text("Digite um custo maior que zero") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                BotaoPrincipal(texto = "Adicionar", aoClicar = { adicionar() })
            }
        }
        item {
            TituloSecao(
                titulo = "Recompensas (${estado.recompensas.size})",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (estado.recompensas.isEmpty()) {
            item {
                Text(
                    text = "Nenhuma recompensa cadastrada. Use o formulário acima para criar a primeira.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(estado.recompensas, key = { it.id }) { recompensa ->
                CardRecompensa(
                    recompensa = recompensa,
                    faltam = estado.minutosFaltando(recompensa),
                    aoResgatar = { estado.resgatarRecompensa(recompensa.id) },
                    aoRemover = { estado.removerRecompensa(recompensa.id) },
                    aoAbrir = { navController.navigate(Rotas.detalheRecompensa(recompensa.id)) }
                )
            }
        }
    }
}

@Composable
private fun CardRecompensa(
    recompensa: Recompensa,
    faltam: Int,
    aoResgatar: () -> Unit,
    aoRemover: () -> Unit,
    aoAbrir: () -> Unit
) {
    CartaoPadrao(modifier = Modifier.fillMaxWidth(), aoClicar = aoAbrir) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EmojiCirculo(emoji = recompensa.emoji, tamanho = 44.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recompensa.nome,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${recompensa.custoMinutos} min · resgatada ${recompensa.vezesResgatada}x",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = aoRemover) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remover ${recompensa.nome}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        BotaoPrincipal(
            texto = if (faltam == 0) "Resgatar" else "Faltam $faltam min",
            aoClicar = aoResgatar,
            habilitado = faltam == 0
        )
    }
}
