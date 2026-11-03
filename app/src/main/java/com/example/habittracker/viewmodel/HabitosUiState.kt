package com.example.habittracker.viewmodel

data class HabitosUiState(
    val dados: AppUiState = AppUiState(),
    val formulario: FormularioHabito = FormularioHabito(),
    val rapido: FormularioHabito = FormularioHabito(),
    val foco: FocoUiState = FocoUiState(),
    val edicao: FormularioHabito = FormularioHabito(),
    val confirmandoReset: Boolean = false,
    val resetFeito: Boolean = false,
    val carregando: Boolean = true,
    val salvando: Boolean = false,
    val erro: String? = null
)
