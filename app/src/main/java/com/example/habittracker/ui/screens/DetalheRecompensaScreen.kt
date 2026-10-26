package com.example.habittracker.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import android.util.Log
import kotlinx.coroutines.CancellationException
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
import com.example.habittracker.model.Recompensa
import com.example.habittracker.ui.components.BarraProgresso
import com.example.habittracker.ui.components.BotaoPrincipal
import com.example.habittracker.ui.components.BotaoSecundario
import com.example.habittracker.ui.components.CartaoEstatistica
import com.example.habittracker.ui.components.CartaoPadrao
import com.example.habittracker.ui.components.EmojiCirculo

/** O id veio pela rota; a recompensa certa é buscada no AppState. */
@Composable
fun DetalheRecompensaScreen(
    navController: NavController,
    estado: AppState,
    recompensaId: Int,
    modifier: Modifier = Modifier
) {
    var recompensa by remember(recompensaId) { mutableStateOf<Recompensa?>(null) }
    var carregando by remember(recompensaId) { mutableStateOf(true) }
    var falha by remember(recompensaId) { mutableStateOf<String?>(null) }
    LaunchedEffect(recompensaId, estado.versaoDados) {
        try {
            falha = null
            recompensa = estado.buscarRecompensaNoBanco(recompensaId)
        } catch (cancelamento: CancellationException) {
            throw cancelamento
        } catch (erro: Exception) {
            Log.e("HabitTracker", "Falha ao consultar detalhe", erro)
            falha = "Não foi possível carregar os dados."
        } finally { carregando = false }
    }
    if (carregando) {
        Text("Carregando…", modifier = Modifier.padding(20.dp))
        return
    }
    if (falha != null) {
        Column(Modifier.padding(20.dp)) {
            Text(falha.orEmpty())
            BotaoSecundario("Tentar novamente", { estado.recarregar() })
        }
        return
    }
    val atual = recompensa
    if (atual == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Recompensa não encontrada",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            BotaoSecundario(texto = "Voltar", aoClicar = { navController.popBackStack() })
        }
    } else {
        DetalheRecompensaConteudo(navController, estado, atual, modifier)
    }
}

@Composable
private fun DetalheRecompensaConteudo(
    navController: NavController,
    estado: AppState,
    recompensa: Recompensa,
    modifier: Modifier
) {
    var mensagem by remember { mutableStateOf("") }
    val faltam = estado.minutosFaltando(recompensa)
    val progresso = estado.saldoMinutos / recompensa.custoMinutos.toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EmojiCirculo(emoji = recompensa.emoji, tamanho = 64.dp)
            Text(
                text = recompensa.nome,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoEstatistica(modifier = Modifier.weight(1f), valor = "${recompensa.custoMinutos} min", rotulo = "Custo")
            CartaoEstatistica(modifier = Modifier.weight(1f), valor = "${estado.saldoMinutos} min", rotulo = "Seu saldo")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CartaoEstatistica(modifier = Modifier.weight(1f), valor = "$faltam min", rotulo = "Quanto falta")
            CartaoEstatistica(modifier = Modifier.weight(1f), valor = "${recompensa.vezesResgatada}x", rotulo = "Já resgatada")
        }

        CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (faltam == 0) "Você já tem tempo livre suficiente."
                else "Conclua hábitos e sessões de foco para juntar mais $faltam min.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            BarraProgresso(progresso = progresso)
        }

        BotaoPrincipal(
            texto = if (faltam == 0) "Resgatar" else "Faltam $faltam min",
            habilitado = faltam == 0,
            aoClicar = {
                val custo = recompensa.custoMinutos
                estado.resgatarRecompensa(recompensa.id) {
                    mensagem = "✓ Resgatado! -$custo min do seu saldo."
                }
            }
        )
        if (mensagem.isNotEmpty()) {
            Text(
                text = mensagem,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        EdicaoDaRecompensa(estado, recompensa)
        com.example.habittracker.ui.components.BotaoRemover(
            "Remover recompensa", "A recompensa será excluída. Os resgates serão preservados."
        ) { estado.removerRecompensa(recompensa.id) { navController.popBackStack() } }
        BotaoSecundario(texto = "Voltar", aoClicar = { navController.popBackStack() })
    }
}

@Composable
private fun EdicaoDaRecompensa(estado: AppState, recompensa: Recompensa) {
    var nome by remember(recompensa.id) { mutableStateOf(recompensa.nome) }
    var custo by remember(recompensa.id) { mutableStateOf(recompensa.custoMinutos.toString()) }
    var salvo by remember(recompensa.id) { mutableStateOf(false) }
    CartaoPadrao(Modifier.fillMaxWidth()) {
        Text("Editar recompensa", style = MaterialTheme.typography.titleMedium)
        androidx.compose.material3.OutlinedTextField(
            value = nome, onValueChange = { nome = it; salvo = false },
            label = { Text("Nome da recompensa") }, modifier = Modifier.fillMaxWidth(), singleLine = true
        )
        androidx.compose.material3.OutlinedTextField(
            value = custo, onValueChange = { custo = it.filter(Char::isDigit).take(4); salvo = false },
            label = { Text("Custo em minutos") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
        )
        BotaoPrincipal("Salvar alterações", {
            estado.atualizarRecompensa(recompensa.copy(nome = nome, custoMinutos = custo.toIntOrNull() ?: 0)) {
                salvo = true
            }
        }, habilitado = nome.isNotBlank() && (custo.toIntOrNull() ?: 0) > 0)
        if (salvo) Text("✓ Alterações salvas", color = MaterialTheme.colorScheme.primary)
    }
}
