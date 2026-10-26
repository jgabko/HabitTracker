package com.example.habittracker.model

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "perfil")
data class Perfil(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val saldoMinutos: Int = 0,
    // Ajustes de origem para preservar a progressão de instalações da versão 1.
    // Na instalação nova, os pontos são inteiramente calculados pelo histórico.
    val forca: Int = 0,
    val vitalidade: Int = 0,
    val sabedoria: Int = 0,
    val foco: Int = 0,
    val disciplina: Int = 0,
    @ColumnInfo(defaultValue = "1") val notificacoesAtivas: Boolean = true
) {
    fun pontosBase(atributo: Atributo): Int = when (atributo) {
        Atributo.FORCA -> forca
        Atributo.VITALIDADE -> vitalidade
        Atributo.SABEDORIA -> sabedoria
        Atributo.FOCO -> foco
        Atributo.DISCIPLINA -> disciplina
    }
}
