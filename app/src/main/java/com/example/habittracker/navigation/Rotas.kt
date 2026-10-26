package com.example.habittracker.navigation

/** Todas as rotas nomeadas do app, em um só lugar. */
object Rotas {
    // Abas da NavigationBar
    const val INICIO = "inicio"
    const val PROGRESSO = "progresso"
    const val FOCO = "foco"
    const val LOJA = "loja"
    const val PERFIL = "perfil"

    // Telas internas
    const val HABITOS = "habitos"
    const val NOVO_HABITO = "novo_habito"
    const val DETALHE_HABITO = "habito/{id}"
    const val DETALHE_RECOMPENSA = "recompensa/{id}"
    const val ESTATISTICAS = "estatisticas"
    const val CONFIGURACOES = "configuracoes"
    const val SOBRE = "sobre"

    // Foco aberto pelo Detalhe do Hábito (recebe o id do hábito, igual às outras rotas com argumento)
    const val FOCO_DO_HABITO = "foco/{habitoId}"

    val ABAS = listOf(INICIO, PROGRESSO, FOCO, LOJA, PERFIL)

    fun detalheHabito(id: Int) = "habito/$id"
    fun detalheRecompensa(id: Int) = "recompensa/$id"
    fun focoDoHabito(habitoId: Int) = "foco/$habitoId"
}
