package com.example.habittracker.data.repository

import com.example.habittracker.data.Habito
import com.example.habittracker.data.HabitoDao

// Mesmo padrão do FilmesRepository da aula: recebe o DAO e repassa operações.
class HabitoRepository(private val dao: HabitoDao) {
    fun buscarTodos() = dao.buscarTodos()
    suspend fun inserir(habito: Habito) = dao.inserir(habito)
    suspend fun atualizar(habito: Habito) = dao.atualizar(habito)
    suspend fun remover(habito: Habito) = dao.remover(habito)
    suspend fun buscarPorId(id: Int) = dao.buscarPorId(id)
    suspend fun removerTodos() = dao.removerTodos()
}
