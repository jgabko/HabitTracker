package com.example.habittracker.dados

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AppState {
    val habitos = mutableStateListOf<Habito>()
    val recompensas = mutableStateListOf<Recompensa>()
    val registros = mutableStateListOf<Registro>()

    var saldoMinutos by mutableIntStateOf(0)
        private set
    var forca by mutableIntStateOf(0)
        private set
    var vitalidade by mutableIntStateOf(0)
        private set
    var sabedoria by mutableIntStateOf(0)
        private set
    var foco by mutableIntStateOf(0)
        private set
    var disciplina by mutableIntStateOf(0)
        private set
    var ultimaMensagem by mutableStateOf("")
        private set

    private var proximoHabitoId = 1
    private var proximaRecompensaId = 1
    private var proximoRegistroId = 1

    init {
        carregarDadosExemplo()
    }

    fun adicionarHabito(
        nome: String,
        categoria: CategoriaHabito,
        diasDaSemana: Set<DiaDaSemana> = DiaDaSemana.entries.toSet(),
        periodo: PeriodoDoDia = PeriodoDoDia.MANHA,
        atributo: Atributo = atributoPadrao(categoria),
        minutosGanho: Int = 10
    ) {
        val nomeLimpo = nome.trim()
        if (nomeLimpo.isEmpty()) {
            registrarLog("adicionarHabito ignorado: nome vazio")
            return
        }
        val habito = Habito(
            id = proximoHabitoId++,
            nome = nomeLimpo,
            categoria = categoria,
            diasDaSemana = diasDaSemana,
            periodo = periodo,
            atributo = atributo,
            minutosGanho = minutosGanho
        )
        habitos.add(habito)
        registrarLog("Hábito adicionado id=${habito.id} nome=${habito.nome}")
    }

    fun removerHabito(id: Int) {
        val removido = habitos.removeIf { it.id == id }
        if (removido) {
            registrarLog("Hábito removido id=$id")
        } else {
            registrarLog("removerHabito: id=$id não encontrado")
        }
    }

    fun concluirHabito(id: Int) {
        val habito = habitos.find { it.id == id }
        if (habito == null) {
            registrarLog("concluirHabito: id=$id não encontrado")
            return
        }
        val jaConcluido = concluidoNoDia(id, DIA_HOJE)
        if (jaConcluido) {
            registros.removeAll { it.habitoId == id && it.tipo == TipoRegistro.CONCLUSAO && it.dia == DIA_HOJE }
            saldoMinutos = (saldoMinutos - habito.minutosGanho).coerceAtLeast(0)
            ajustarAtributo(habito.atributo, -1)
            registrarLog("Hábito desmarcado id=$id saldo=$saldoMinutos")
        } else {
            registros.add(
                Registro(
                    id = proximoRegistroId++,
                    tipo = TipoRegistro.CONCLUSAO,
                    dia = DIA_HOJE,
                    habitoId = id,
                    minutos = habito.minutosGanho
                )
            )
            saldoMinutos += habito.minutosGanho
            ajustarAtributo(habito.atributo, 1)
            registrarLog("Hábito concluído id=$id +${habito.minutosGanho}min saldo=$saldoMinutos")
        }
    }

    fun registrarRecaida(id: Int) {
        val habito = habitos.find { it.id == id }
        if (habito == null) {
            registrarLog("registrarRecaida: id=$id não encontrado")
            return
        }
        registros.add(
            Registro(
                id = proximoRegistroId++,
                tipo = TipoRegistro.RECAIDA,
                dia = DIA_HOJE,
                habitoId = id
            )
        )
        registrarLog("Recaída registrada hábito=$id dia=$DIA_HOJE")
    }

    fun resgatarRecompensa(id: Int) {
        val recompensa = recompensas.find { it.id == id }
        if (recompensa == null) {
            registrarLog("resgatarRecompensa: id=$id não encontrado")
            return
        }
        if (saldoMinutos < recompensa.custoMinutos) {
            registrarLog(
                "Resgate recusado id=$id falta=${recompensa.custoMinutos - saldoMinutos}min"
            )
            return
        }
        saldoMinutos -= recompensa.custoMinutos
        registros.add(
            Registro(
                id = proximoRegistroId++,
                tipo = TipoRegistro.RESGATE,
                dia = DIA_HOJE,
                recompensaId = id,
                minutos = recompensa.custoMinutos
            )
        )
        registrarLog("Recompensa resgatada id=$id -${recompensa.custoMinutos}min saldo=$saldoMinutos")
    }

    fun adicionarRecompensa(nome: String, custoMinutos: Int) {
        val nomeLimpo = nome.trim()
        if (nomeLimpo.isEmpty() || custoMinutos <= 0) {
            registrarLog("adicionarRecompensa ignorada: dados inválidos")
            return
        }
        val recompensa = Recompensa(
            id = proximaRecompensaId++,
            nome = nomeLimpo,
            custoMinutos = custoMinutos
        )
        recompensas.add(recompensa)
        registrarLog("Recompensa adicionada id=${recompensa.id} nome=${recompensa.nome}")
    }

    fun removerRecompensa(id: Int) {
        val removida = recompensas.removeIf { it.id == id }
        if (removida) {
            registrarLog("Recompensa removida id=$id")
        } else {
            registrarLog("removerRecompensa: id=$id não encontrado")
        }
    }

    fun registrarSessaoFoco(habitoId: Int, minutos: Int) {
        val habito = habitos.find { it.id == habitoId }
        if (habito == null) {
            registrarLog("registrarSessaoFoco: hábito $habitoId não encontrado")
            return
        }
        if (minutos <= 0) {
            registrarLog("registrarSessaoFoco ignorada: minutos=$minutos")
            return
        }
        registros.add(
            Registro(
                id = proximoRegistroId++,
                tipo = TipoRegistro.FOCO,
                dia = DIA_HOJE,
                habitoId = habitoId,
                minutos = minutos
            )
        )
        ajustarAtributo(Atributo.FOCO, 1)
        registrarLog("Sessão de foco ${minutos}min no hábito $habitoId")
    }

    fun concluidoNoDia(habitoId: Int, dia: Int = DIA_HOJE): Boolean {
        return registros.any {
            it.habitoId == habitoId && it.tipo == TipoRegistro.CONCLUSAO && it.dia == dia
        }
    }

    fun habitosDoDia(dia: Int = DIA_HOJE): List<Habito> {
        val diaSemana = diaDaSemanaDoCalendario(dia)
        return habitos.filter { diaSemana in it.diasDaSemana }
    }

    fun habitoPorId(id: Int): Habito? = habitos.find { it.id == id }

    fun recompensaPorId(id: Int): Recompensa? = recompensas.find { it.id == id }

    fun valorAtributo(atributo: Atributo): Int = when (atributo) {
        Atributo.FORCA -> forca
        Atributo.VITALIDADE -> vitalidade
        Atributo.SABEDORIA -> sabedoria
        Atributo.FOCO -> foco
        Atributo.DISCIPLINA -> disciplina
    }

    fun resetarDadosExemplo() {
        carregarDadosExemplo()
        registrarLog("Dados de exemplo restaurados saldo=$saldoMinutos")
    }

    private fun carregarDadosExemplo() {
        habitos.clear()
        recompensas.clear()
        registros.clear()
        proximoHabitoId = 1
        proximaRecompensaId = 1
        proximoRegistroId = 1
        saldoMinutos = 0
        forca = 4
        vitalidade = 6
        sabedoria = 5
        foco = 3
        disciplina = 4

        habitos.addAll(
            listOf(
                Habito(1, "Beber 2L de água", CategoriaHabito.SAUDE, diasUteis(), PeriodoDoDia.MANHA, Atributo.VITALIDADE, 10),
                Habito(2, "Treinar 20 min", CategoriaHabito.MOVIMENTO, diasUteis(), PeriodoDoDia.TARDE, Atributo.FORCA, 15),
                Habito(3, "Ler 10 páginas", CategoriaHabito.ESTUDO, DiaDaSemana.entries.toSet(), PeriodoDoDia.NOITE, Atributo.SABEDORIA, 10),
                Habito(4, "Meditar 5 min", CategoriaHabito.MENTE, DiaDaSemana.entries.toSet(), PeriodoDoDia.MANHA, Atributo.FOCO, 10),
                Habito(5, "Estudar com timer", CategoriaHabito.ROTINA, diasUteis(), PeriodoDoDia.TARDE, Atributo.DISCIPLINA, 15)
            )
        )
        proximoHabitoId = 6

        recompensas.addAll(
            listOf(
                Recompensa(1, "Episódio de série", 30),
                Recompensa(2, "Jogo por 20 min", 20),
                Recompensa(3, "Passeio curto", 60)
            )
        )
        proximaRecompensaId = 4

        for (dia in 1 until DIA_HOJE) {
            val idsDoDia = when (dia % 4) {
                0 -> listOf(1, 2, 3, 4)
                1 -> listOf(1, 3, 5)
                2 -> listOf(1, 2, 4)
                else -> listOf(1, 4, 5)
            }
            idsDoDia.forEach { habitoId ->
                val habito = habitos.first { it.id == habitoId }
                registros.add(
                    Registro(
                        id = proximoRegistroId++,
                        tipo = TipoRegistro.CONCLUSAO,
                        dia = dia,
                        habitoId = habitoId,
                        minutos = habito.minutosGanho
                    )
                )
                saldoMinutos += habito.minutosGanho
            }
            if (dia % 7 == 0) {
                registros.add(
                    Registro(
                        id = proximoRegistroId++,
                        tipo = TipoRegistro.RECAIDA,
                        dia = dia,
                        habitoId = 2
                    )
                )
            }
            if (dia % 5 == 0) {
                registros.add(
                    Registro(
                        id = proximoRegistroId++,
                        tipo = TipoRegistro.FOCO,
                        dia = dia,
                        habitoId = 5,
                        minutos = 25
                    )
                )
            }
        }
        registrarLog("Exemplos carregados hábitos=${habitos.size} saldo=$saldoMinutos dia=$DIA_HOJE")
    }

    private fun ajustarAtributo(atributo: Atributo, delta: Int) {
        when (atributo) {
            Atributo.FORCA -> forca = (forca + delta).coerceAtLeast(0)
            Atributo.VITALIDADE -> vitalidade = (vitalidade + delta).coerceAtLeast(0)
            Atributo.SABEDORIA -> sabedoria = (sabedoria + delta).coerceAtLeast(0)
            Atributo.FOCO -> foco = (foco + delta).coerceAtLeast(0)
            Atributo.DISCIPLINA -> disciplina = (disciplina + delta).coerceAtLeast(0)
        }
    }

    private fun registrarLog(mensagem: String) {
        ultimaMensagem = mensagem
        Log.d(TAG_APP, mensagem)
    }

    private fun diasUteis(): Set<DiaDaSemana> {
        return setOf(
            DiaDaSemana.SEG,
            DiaDaSemana.TER,
            DiaDaSemana.QUA,
            DiaDaSemana.QUI,
            DiaDaSemana.SEX
        )
    }

    private fun atributoPadrao(categoria: CategoriaHabito): Atributo {
        return when (categoria) {
            CategoriaHabito.SAUDE -> Atributo.VITALIDADE
            CategoriaHabito.MOVIMENTO -> Atributo.FORCA
            CategoriaHabito.MENTE -> Atributo.FOCO
            CategoriaHabito.ESTUDO -> Atributo.SABEDORIA
            CategoriaHabito.ROTINA -> Atributo.DISCIPLINA
        }
    }
}
