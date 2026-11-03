package com.example.habittracker.data.repository

import com.example.habittracker.data.Recompensa
import com.example.habittracker.data.RecompensaDao

// Mesmo padrão do FilmesRepository da aula: recebe o DAO e repassa operações.
class RecompensaRepository(private val dao: RecompensaDao) {
    fun buscarTodos() = dao.buscarTodos()
    suspend fun inserir(recompensa: Recompensa) = dao.inserir(recompensa)
    suspend fun atualizar(recompensa: Recompensa) = dao.atualizar(recompensa)
    suspend fun remover(recompensa: Recompensa) = dao.remover(recompensa)
    suspend fun buscarPorId(id: Int) = dao.buscarPorId(id)
    suspend fun removerTodos() = dao.removerTodos()
}
