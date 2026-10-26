package com.example.habittracker.data

import androidx.room.*
import com.example.habittracker.model.*

@Dao
interface HabitoDao {
    @Insert suspend fun inserir(habito: Habito): Long
    @Query("SELECT * FROM habitos ORDER BY id") suspend fun buscarTodos(): List<Habito>
    @Query("SELECT * FROM habitos WHERE id = :id") suspend fun buscarPorId(id: Int): Habito?
    @Update suspend fun atualizar(habito: Habito): Int
    @Delete suspend fun remover(habito: Habito): Int
    @Query("DELETE FROM habitos") suspend fun removerTodos()
}

@Dao
interface RecompensaDao {
    @Insert suspend fun inserir(recompensa: Recompensa): Long
    @Query("SELECT * FROM recompensas ORDER BY id") suspend fun buscarTodos(): List<Recompensa>
    @Query("SELECT * FROM recompensas WHERE id = :id") suspend fun buscarPorId(id: Int): Recompensa?
    @Update suspend fun atualizar(recompensa: Recompensa): Int
    @Delete suspend fun remover(recompensa: Recompensa): Int
    @Query("DELETE FROM recompensas") suspend fun removerTodos()
}

@Dao
interface RegistroDao {
    @Insert suspend fun inserir(registro: Registro): Long
    @Query("SELECT * FROM registros ORDER BY id") suspend fun buscarTodos(): List<Registro>
    @Query("SELECT * FROM registros WHERE habitoId = :id AND dia = :dia AND tipo = 'CONCLUSAO' LIMIT 1")
    suspend fun buscarConclusao(id: Int, dia: Int): Registro?
    @Update suspend fun atualizar(registro: Registro): Int
    @Delete suspend fun remover(registro: Registro): Int
    @Query("DELETE FROM registros") suspend fun removerTodos()
}

@Dao
interface PerfilDao {
    @Insert suspend fun inserir(perfil: Perfil): Long
    @Query("SELECT * FROM perfil ORDER BY id") suspend fun buscarTodos(): List<Perfil>
    @Query("SELECT * FROM perfil WHERE id = 1") suspend fun buscar(): Perfil?
    @Upsert suspend fun salvar(perfil: Perfil)
    @Update suspend fun atualizar(perfil: Perfil): Int
    @Delete suspend fun remover(perfil: Perfil): Int
}
