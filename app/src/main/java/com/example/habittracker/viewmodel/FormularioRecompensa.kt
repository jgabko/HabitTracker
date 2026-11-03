package com.example.habittracker.viewmodel

import com.example.habittracker.data.Recompensa

data class FormularioRecompensa(
    val item: Recompensa? = null,
    val nome: String = "",
    val custoTexto: String = "",
    val tentouSalvar: Boolean = false,
    val salvo: Boolean = false
) {
    val custo get() = custoTexto.toIntOrNull() ?: 0
    val nomeInvalido get() = tentouSalvar && nome.isBlank()
    val custoInvalido get() = tentouSalvar && custo <= 0
    val valido get() = nome.isNotBlank() && custo > 0
}
