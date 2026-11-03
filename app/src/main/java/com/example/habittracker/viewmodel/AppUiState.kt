package com.example.habittracker.viewmodel

import com.example.habittracker.data.*
import kotlin.math.roundToInt

/** Dados imutáveis compartilhados pelas telas. Cálculos preservados do Trabalho 3. */
data class AppUiState(
    val habitos: List<Habito> = emptyList(),
    val recompensas: List<Recompensa> = emptyList(),
    val registros: List<Registro> = emptyList(),
    val perfil: Perfil = Perfil(id = 1)
) {
    companion object {
        const val DIAS_NO_MES = 30
        const val PONTOS_POR_CONCLUSAO = 2
        const val MINUTOS_FOCO_POR_MINUTO_LAZER = 5
        const val MINUTOS_FOCO_POR_PONTO = 10
        const val PONTOS_POR_NIVEL = 50
    }
    val diaHoje = 15
    val saldoMinutos get() = perfil.saldoMinutos
    val notificacoesAtivas get() = perfil.notificacoesAtivas
    fun habitoPorId(id: Int): Habito? = habitos.firstOrNull { it.id == id }
    fun recompensaPorId(id: Int): Recompensa? = recompensas.firstOrNull { it.id == id }
    fun diaDaSemana(dia: Int): Int = dia % 7
    fun habitosDeHoje(): List<Habito> = habitos.filter { diaDaSemana(diaHoje) in it.diasSemana }

    fun concluidoEm(habitoId: Int, dia: Int): Boolean = registros.any {
        it.habitoId == habitoId && it.dia == dia && it.tipo == TipoRegistro.CONCLUSAO
    }
    fun concluidoHoje(habitoId: Int): Boolean = concluidoEm(habitoId, diaHoje)

    fun diasSem(habito: Habito): Int = diaHoje - habito.ultimaRecaida

    /** Dias seguidos concluindo o hábito (hoje conta se já foi feito; senão conta a partir de ontem). */
    fun ofensivaAtual(habitoId: Int): Int {
        var dia = if (concluidoEm(habitoId, diaHoje)) diaHoje else diaHoje - 1
        var total = 0
        while (dia >= 1 && concluidoEm(habitoId, dia)) {
            total++
            dia--
        }
        return total
    }

    /** O número mostrado no card: "dias sem" para EVITAR, ofensiva para FAZER. */
    fun sequenciaDe(habito: Habito): Int =
        if (habito.tipo == TipoHabito.EVITAR) diasSem(habito) else ofensivaAtual(habito.id)

    fun melhorOfensiva(habitoId: Int): Int {
        var melhor = 0
        var atual = 0
        for (dia in 1..diaHoje) {
            if (concluidoEm(habitoId, dia)) {
                atual++
                if (atual > melhor) melhor = atual
            } else {
                atual = 0
            }
        }
        return melhor
    }

    fun totalConclusoes(habitoId: Int): Int =
        registros.count { it.habitoId == habitoId && it.tipo == TipoRegistro.CONCLUSAO }

    /** Fração (0f a 1f) dos últimos 7 dias em que o hábito foi concluído. */
    fun taxaUltimos7Dias(habitoId: Int): Float {
        val concluidos = ((diaHoje - 6)..diaHoje).count { concluidoEm(habitoId, it) }
        return concluidos / 7f
    }

    fun minutosFocoDoHabito(habitoId: Int): Int =
        registros.filter { it.habitoId == habitoId && it.tipo == TipoRegistro.FOCO }.sumOf { it.minutos }

    // ------------------------------------------------------------------
    // Cálculos gerais (calendário, estatísticas, avatar)
    // ------------------------------------------------------------------

    /** Atividades de um dia: conclusões e sessões de foco (recaídas não contam). */
    fun atividadesNoDia(dia: Int): List<Registro> =
        registros.filter { it.dia == dia && it.tipo != TipoRegistro.RECAIDA && it.tipo != TipoRegistro.RESGATE }

    fun contagemNoDia(dia: Int): Int = atividadesNoDia(dia).size

    /** Dias seguidos com pelo menos 1 atividade (hoje conta se já houve atividade). */
    fun ofensivaGeral(): Int {
        var dia = if (contagemNoDia(diaHoje) > 0) diaHoje else diaHoje - 1
        var total = 0
        while (dia >= 1 && contagemNoDia(dia) > 0) {
            total++
            dia--
        }
        return total
    }

    fun totalConclusoesGeral(): Int = registros.count { it.tipo == TipoRegistro.CONCLUSAO }

    fun totalSessoesFoco(): Int = registros.count { it.tipo == TipoRegistro.FOCO }

    fun minutosFocoTotal(): Int =
        registros.filter { it.tipo == TipoRegistro.FOCO }.sumOf { it.minutos }

    /** Pontos de um atributo, calculados a partir do histórico. */
    fun pontosDe(atributo: Atributo): Int =
        (perfil.pontosBase(atributo) + registros.sumOf { registro ->
            when (registro.tipo) {
                TipoRegistro.CONCLUSAO ->
                    if (registro.categoria.atributo == atributo) PONTOS_POR_CONCLUSAO else 0
                TipoRegistro.FOCO ->
                    if (atributo == Atributo.FOCO) registro.minutos / MINUTOS_FOCO_POR_PONTO else 0
                TipoRegistro.RECAIDA, TipoRegistro.RESGATE -> 0
            }
        }).coerceAtLeast(0)

    fun pontosTotais(): Int = Atributo.entries.sumOf { pontosDe(it) }

    fun nivel(): Int = 1 + pontosTotais() / PONTOS_POR_NIVEL

    /** Progresso (0f a 1f) até o próximo nível. */
    fun progressoNivel(): Float =
        (pontosTotais() % PONTOS_POR_NIVEL) / PONTOS_POR_NIVEL.toFloat()


    fun minutosFaltando(recompensa: Recompensa): Int = (recompensa.custoMinutos - saldoMinutos).coerceAtLeast(0)
/** Percentual de conclusões em relação ao previsto (dias agendados de cada hábito, do dia 1 até hoje). */
    fun taxaDeConclusao(): Int {
    var previstos = 0
    var feitos = 0
    habitos.forEach { habito ->
        for (dia in 1..diaHoje) {
            if (diaDaSemana(dia) in habito.diasSemana) {
                previstos++
                if (concluidoEm(habito.id, dia)) feitos++
            }
        }
    }
    return if (previstos == 0) 0 else (feitos * 100f / previstos).roundToInt()
}
}
