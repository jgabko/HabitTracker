package com.example.habittracker.data.repository

import com.example.habittracker.data.*
import androidx.room.withTransaction

// Mesmo padrão do FilmesRepository da aula: recebe o DAO e repassa operações.
class RecompensaRepository(private val dao: RecompensaDao, private val banco: AppDatabase) {
    fun buscarTodos() = dao.buscarTodos()
    suspend fun inserir(recompensa: Recompensa) = dao.inserir(recompensa)
    suspend fun atualizar(recompensa: Recompensa) = dao.atualizar(recompensa)
    suspend fun remover(recompensa: Recompensa) = dao.remover(recompensa)
    suspend fun buscarPorId(id: Int) = dao.buscarPorId(id)
    suspend fun removerTodos() = dao.removerTodos()

    // O Room desfaz o resgate se alguma destas três gravações falhar.
    suspend fun resgatar(id: Int, dia: Int) = banco.withTransaction {
        val item = checkNotNull(dao.buscarPorId(id))
        val perfil = checkNotNull(banco.perfilDao().buscar())
        require(perfil.saldoMinutos >= item.custoMinutos) { "Saldo insuficiente." }
        banco.perfilDao().salvar(perfil.copy(saldoMinutos = perfil.saldoMinutos - item.custoMinutos))
        dao.atualizar(item.copy(vezesResgatada = item.vezesResgatada + 1))
        banco.registroDao().inserir(Registro(dia = dia, tipo = TipoRegistro.RESGATE,
            habitoId = 0, nomeHabito = item.nome, categoria = Categoria.DISCIPLINA,
            minutos = item.custoMinutos, recompensaId = id))
    }
}
