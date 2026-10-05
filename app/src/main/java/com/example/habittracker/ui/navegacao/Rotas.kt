package com.example.habittracker.ui.navegacao

object Rotas {
    const val INICIO = "inicio"
    const val PROGRESSO = "progresso"
    const val FOCO = "foco"
    const val LOJA = "loja"
    const val PERFIL = "perfil"
    const val HABITOS = "habitos"
    const val NOVO_HABITO = "novo_habito"
    const val DETALHE_HABITO = "detalhe_habito/{id}"
    const val DETALHE_RECOMPENSA = "detalhe_recompensa/{id}"
    const val ESTATISTICAS = "estatisticas"
    const val CONFIGURACOES = "configuracoes"

    fun detalheHabito(id: Int) = "detalhe_habito/$id"
    fun detalheRecompensa(id: Int) = "detalhe_recompensa/$id"
}

val RotasDasAbas = listOf(
    Rotas.INICIO,
    Rotas.PROGRESSO,
    Rotas.FOCO,
    Rotas.LOJA,
    Rotas.PERFIL
)
