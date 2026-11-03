package com.example.habittracker.viewmodel

import com.example.habittracker.data.*

data class RecompensasUiState(
    val recompensas: List<Recompensa> = emptyList(),
    val perfil: Perfil = Perfil(id = 1),
    val nome: String = "",
    val custoTexto: String = "",
    val recompensaEmEdicao: Recompensa? = null,
    val tentouSalvar: Boolean = false,
    val salvo: Boolean = false,
    val aba: Int = 1,
    val mensagem: String = "",
    val carregando: Boolean = false,
    val salvando: Boolean = false,
    val erro: String? = null
) {
    val saldoMinutos get() = perfil.saldoMinutos
    val nomeInvalido get() = tentouSalvar && nome.isBlank()
    val custoInvalido get() = tentouSalvar && (custoTexto.toIntOrNull() ?: 0) <= 0
    val valido get() = nome.isNotBlank() && (custoTexto.toIntOrNull() ?: 0) > 0
    fun recompensaPorId(id: Int) = recompensas.find { it.id == id }
    fun minutosFaltando(item: Recompensa) = (item.custoMinutos - saldoMinutos).coerceAtLeast(0)
}
