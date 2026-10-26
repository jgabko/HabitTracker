package com.example.habittracker.dados

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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

@Entity(tableName = "habitos")
data class Habito(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val categoria: CategoriaHabito,
    val diasDaSemana: Set<DiaDaSemana>,
    val periodo: PeriodoDoDia,
    val atributo: Atributo,
    val minutosGanho: Int
)

@Entity(tableName = "recompensas")
data class Recompensa(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val custoMinutos: Int
)

@Entity(
    tableName = "registros",
    foreignKeys = [
        ForeignKey(entity = Habito::class, parentColumns = ["id"], childColumns = ["habitoId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = Recompensa::class, parentColumns = ["id"], childColumns = ["recompensaId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("habitoId"), Index("recompensaId")]
)
data class Registro(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: TipoRegistro,
    val dia: Int,
    val habitoId: Int? = null,
    val recompensaId: Int? = null,
    val minutos: Int = 0
)

@Entity(tableName = "perfil")
data class Perfil(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val saldoMinutos: Int = 0,
    val forca: Int = 0,
    val vitalidade: Int = 0,
    val sabedoria: Int = 0,
    val foco: Int = 0,
    val disciplina: Int = 0
)

const val DIA_HOJE = 15
const val DIAS_NO_MES = 30
const val TAG_APP = "HabitTracker"

fun diaDaSemanaDoCalendario(dia: Int): DiaDaSemana {
    val indice = (dia - 1).mod(DiaDaSemana.entries.size)
    return DiaDaSemana.entries[indice]
}
