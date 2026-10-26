package com.example.habittracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Data class 2 (item da Loja de Recompensas). O custo é pago com o saldo de tempo livre. */
@Entity(tableName = "recompensas")
data class Recompensa(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val emoji: String = "🎁",
    val custoMinutos: Int,
    val vezesResgatada: Int = 0
)
