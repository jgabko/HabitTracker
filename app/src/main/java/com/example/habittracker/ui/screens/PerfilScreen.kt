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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habittracker.data.AppState
import com.example.habittracker.model.Atributo
import com.example.habittracker.navigation.Rotas
import com.example.habittracker.ui.components.BarraProgresso
import com.example.habittracker.ui.components.BotaoPrincipal
import com.example.habittracker.ui.components.CartaoPadrao
import com.example.habittracker.ui.components.EmojiCirculo
import com.example.habittracker.ui.components.TituloSecao

/**
 * Perfil: avatar (círculo com emoji, só um placeholder), nível, barras dos 5 atributos
 * e botão para as Configurações.
 */
@Composable
fun PerfilScreen(navController: NavController, estado: AppState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar + nível
        CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
            EmojiCirculo(
                emoji = "🦸",
                tamanho = 96.dp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Nível ${estado.nivel()}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${estado.pontosTotais() % AppState.PONTOS_POR_NIVEL} / ${AppState.PONTOS_POR_NIVEL} pts para o próximo nível",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            BarraProgresso(progresso = estado.progressoNivel())
        }

        // Atributos
        TituloSecao(titulo = "Atributos")
        CartaoPadrao(modifier = Modifier.fillMaxWidth()) {
            Atributo.entries.forEachIndexed { indice, atributo ->
                if (indice > 0) Spacer(modifier = Modifier.height(16.dp))
                val pontos = estado.pontosDe(atributo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${atributo.emoji} ${atributo.rotulo}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$pontos pts",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                // A barra enche até 50 pts (o mesmo valor de um nível)
                BarraProgresso(progresso = pontos / AppState.PONTOS_POR_NIVEL.toFloat())
            }
        }

        BotaoPrincipal(
            texto = "Configurações",
            aoClicar = { navController.navigate(Rotas.CONFIGURACOES) }
        )
    }
}
