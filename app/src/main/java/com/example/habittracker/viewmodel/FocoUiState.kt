package com.example.habittracker.viewmodel

data class FocoUiState(
    val rotaHabitoId: Int? = null,
    val habitoId: Int? = null,
    val duracao: Int = 25,
    val emAndamento: Boolean = false,
    val decorridos: Int = 0,
    val concluida: Boolean = false
)
