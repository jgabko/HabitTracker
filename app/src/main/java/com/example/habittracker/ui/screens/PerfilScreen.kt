package com.example.habittracker.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.habittracker.dados.AppState
import com.example.habittracker.ui.components.AcaoStub
import com.example.habittracker.ui.components.TelaStub

@Composable
fun PerfilScreen(
    estado: AppState,
    onConfiguracoes: () -> Unit,
    modifier: Modifier = Modifier
) {
    TelaStub(
        titulo = "Perfil",
        modifier = modifier,
        texto = "Força ${estado.forca} · Vitalidade ${estado.vitalidade} · Sabedoria ${estado.sabedoria} · Foco ${estado.foco} · Disciplina ${estado.disciplina}.",
        acoes = listOf(
            AcaoStub("Configurações", onClick = onConfiguracoes)
        )
    )
}
