package com.example.habittracker.viewmodel

import com.example.habittracker.data.*

data class FormularioHabito(
    val item: Habito? = null,
    val nome: String = "",
    val tipo: TipoHabito = TipoHabito.FAZER,
    val categoria: Categoria = Categoria.MENTE,
    val dias: List<Int> = (0..6).toList(),
    val periodo: Periodo = Periodo.MANHA,
    val minutos: Int = 10,
    val tentouSalvar: Boolean = false,
    val salvo: Boolean = false
) {
    val nomeInvalido get() = tentouSalvar && nome.isBlank()
    val diasInvalidos get() = tentouSalvar && dias.isEmpty()
    val valido get() = nome.isNotBlank() && dias.isNotEmpty() && minutos > 0
}
