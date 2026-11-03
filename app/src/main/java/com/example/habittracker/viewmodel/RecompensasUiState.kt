package com.example.habittracker.viewmodel

data class RecompensasUiState(
    val dados: AppUiState = AppUiState(),
    val formulario: FormularioRecompensa = FormularioRecompensa(),
    val edicao: FormularioRecompensa = FormularioRecompensa(),
    val aba: Int = 1,
    val mensagem: String = "",
    val carregando: Boolean = true,
    val salvando: Boolean = false,
    val erro: String? = null
)
