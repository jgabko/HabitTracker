package com.example.habittracker.data.repository

import com.example.habittracker.data.*
import androidx.room.withTransaction

// Mesmo padrão do FilmesRepository da aula: recebe o DAO e repassa operações.
class PerfilRepository(private val dao: PerfilDao, private val banco: AppDatabase) {
    fun buscarTodos() = dao.buscarTodos()
    suspend fun inserir(perfil: Perfil) = dao.inserir(perfil)
    suspend fun atualizar(perfil: Perfil) = dao.atualizar(perfil)
    suspend fun remover(perfil: Perfil) = dao.remover(perfil)
    suspend fun buscar() = dao.buscar()
    suspend fun salvar(perfil: Perfil) = dao.salvar(perfil)

    suspend fun inicializar() = banco.withTransaction {
        if (dao.buscar() == null) gravarExemplos()
    }

    suspend fun restaurarExemplos() = banco.withTransaction { gravarExemplos() }

    private suspend fun gravarExemplos() {
        val exemplos = DadosExemplo()
        banco.registroDao().removerTodos()
        banco.habitoDao().removerTodos()
        banco.recompensaDao().removerTodos()
        for (habito in exemplos.habitos) banco.habitoDao().inserir(habito)
        for (recompensa in exemplos.recompensas) banco.recompensaDao().inserir(recompensa)
        for (registro in exemplos.registros) banco.registroDao().inserir(registro)
        dao.salvar(Perfil(id = 1, saldoMinutos = 45))
    }

    suspend fun alterarNotificacoes(ativas: Boolean) = banco.withTransaction {
        val perfil = checkNotNull(dao.buscar())
        dao.salvar(perfil.copy(notificacoesAtivas = ativas))
    }
}
