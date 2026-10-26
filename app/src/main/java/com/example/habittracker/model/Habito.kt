package com.example.habittracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** FAZER = hábito que se marca todo dia. EVITAR = hábito que se quer parar (conta "dias sem"). */
enum class TipoHabito(val rotulo: String) {
    FAZER("Fazer"),
    EVITAR("Evitar")
}

enum class Periodo(val rotulo: String, val emoji: String) {
    MANHA("Manhã", "☀️"),
    TARDE("Tarde", "🌤️"),
    NOITE("Noite", "🌙")
}

/**
 * Data class 1 (item da Lista de Hábitos).
 * diasSemana: 0 = domingo ... 6 = sábado.
 * ultimaRecaida: dia do mês da última recaída (só usado em hábitos EVITAR).
 */
@Entity(tableName = "habitos")
data class Habito(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val tipo: TipoHabito,
    val categoria: Categoria,
    val emoji: String = categoria.emoji,
    val periodo: Periodo = Periodo.MANHA,
    val diasSemana: List<Int> = listOf(0, 1, 2, 3, 4, 5, 6),
    val minutosRecompensa: Int = 10,
    val ultimaRecaida: Int = 0
)
