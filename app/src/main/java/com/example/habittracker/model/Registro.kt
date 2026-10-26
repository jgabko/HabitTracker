package com.example.habittracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TipoRegistro { CONCLUSAO, FOCO, RECAIDA, RESGATE }

/**
 * Histórico de atividades (alimenta calendário, estatísticas e avatar).
 * Guarda nome e categoria do hábito no momento do registro, então o histórico
 * continua válido mesmo que o hábito seja removido depois.
 * minutos: duração da sessão (só para tipo FOCO).
 */
@Entity(tableName = "registros")
data class Registro(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dia: Int,
    val tipo: TipoRegistro,
    val habitoId: Int,
    val nomeHabito: String,
    val categoria: Categoria,
    val minutos: Int = 0,
    val recompensaId: Int? = null
)
