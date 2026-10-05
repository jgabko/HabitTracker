package com.example.habittracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class AcaoStub(
    val rotulo: String,
    val primaria: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaStub(
    titulo: String,
    modifier: Modifier = Modifier,
    onVoltar: (() -> Unit)? = null,
    acoesTopo: @Composable RowScope.() -> Unit = {},
    texto: String = "Tela em construção. A navegação já funciona.",
    acoes: List<AcaoStub> = emptyList()
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    if (onVoltar != null) {
                        IconButton(onClick = onVoltar) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    }
                },
                actions = acoesTopo,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            acoes.forEach { acao ->
                if (acao.primaria) {
                    Button(
                        onClick = acao.onClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(acao.rotulo)
                    }
                } else {
                    OutlinedButton(
                        onClick = acao.onClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(acao.rotulo)
                    }
                }
            }
        }
    }
}
