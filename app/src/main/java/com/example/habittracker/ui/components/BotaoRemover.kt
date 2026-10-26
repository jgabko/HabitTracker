package com.example.habittracker.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun BotaoRemover(texto: String, aviso: String, aoConfirmar: () -> Unit) {
    var confirmar by remember { mutableStateOf(false) }
    BotaoSecundario(texto, { confirmar = true })
    if (confirmar) AlertDialog(
        onDismissRequest = { confirmar = false },
        title = { Text(texto + "?") }, text = { Text(aviso) },
        confirmButton = { TextButton(onClick = { confirmar = false; aoConfirmar() }) { Text("Remover") } },
        dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
    )
}
