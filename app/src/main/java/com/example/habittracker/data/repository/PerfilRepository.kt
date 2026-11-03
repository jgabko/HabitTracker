package com.example.habittracker.data.repository

import com.example.habittracker.data.Perfil
import com.example.habittracker.data.PerfilDao

// Mesmo padrão do FilmesRepository da aula: recebe o DAO e repassa operações.
class PerfilRepository(private val dao: PerfilDao) {
    fun buscarTodos() = dao.buscarTodos()
    suspend fun inserir(perfil: Perfil) = dao.inserir(perfil)
    suspend fun atualizar(perfil: Perfil) = dao.atualizar(perfil)
    suspend fun remover(perfil: Perfil) = dao.remover(perfil)
    suspend fun buscar() = dao.buscar()
    suspend fun salvar(perfil: Perfil) = dao.salvar(perfil)
}
