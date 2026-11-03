package com.example.habittracker.data.repository

import com.example.habittracker.data.Registro
import com.example.habittracker.data.RegistroDao

// Mesmo padrão do FilmesRepository da aula: recebe o DAO e repassa operações.
class RegistroRepository(private val dao: RegistroDao) {
    fun buscarTodos() = dao.buscarTodos()
    suspend fun inserir(registro: Registro) = dao.inserir(registro)
    suspend fun atualizar(registro: Registro) = dao.atualizar(registro)
    suspend fun remover(registro: Registro) = dao.remover(registro)
    suspend fun buscarConclusao(id: Int, dia: Int) = dao.buscarConclusao(id, dia)
    suspend fun removerTodos() = dao.removerTodos()
}
