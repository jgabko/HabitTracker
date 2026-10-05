package com.example.habittracker.dados

enum class CategoriaHabito(val rotulo: String) {
    SAUDE("Saúde"),
    MOVIMENTO("Movimento"),
    MENTE("Mente"),
    ESTUDO("Estudo"),
    ROTINA("Rotina")
}

enum class PeriodoDoDia(val rotulo: String) {
    MANHA("Manhã"),
    TARDE("Tarde"),
    NOITE("Noite")
}

enum class DiaDaSemana(val rotulo: String, val numero: Int) {
    SEG("Seg", 1),
    TER("Ter", 2),
    QUA("Qua", 3),
    QUI("Qui", 4),
    SEX("Sex", 5),
    SAB("Sáb", 6),
    DOM("Dom", 7)
}

enum class Atributo(val rotulo: String) {
    FORCA("Força"),
    VITALIDADE("Vitalidade"),
    SABEDORIA("Sabedoria"),
    FOCO("Foco"),
    DISCIPLINA("Disciplina")
}

enum class TipoRegistro {
    CONCLUSAO,
    RECAIDA,
    FOCO,
    RESGATE
}

data class Habito(
    val id: Int,
    val nome: String,
    val categoria: CategoriaHabito,
    val diasDaSemana: Set<DiaDaSemana>,
    val periodo: PeriodoDoDia,
    val atributo: Atributo,
    val minutosGanho: Int
)

data class Recompensa(
    val id: Int,
    val nome: String,
    val custoMinutos: Int
)

data class Registro(
    val id: Int,
    val tipo: TipoRegistro,
    val dia: Int,
    val habitoId: Int? = null,
    val recompensaId: Int? = null,
    val minutos: Int = 0
)

const val DIA_HOJE = 15
const val DIAS_NO_MES = 30
const val TAG_APP = "HabitTracker"

fun diaDaSemanaDoCalendario(dia: Int): DiaDaSemana {
    val indice = (dia - 1).mod(DiaDaSemana.entries.size)
    return DiaDaSemana.entries[indice]
}
