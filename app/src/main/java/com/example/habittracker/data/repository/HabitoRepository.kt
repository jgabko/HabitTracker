package com.example.habittracker.data.repository

import com.example.habittracker.data.*
import androidx.room.withTransaction

// Mesmo padrão do FilmesRepository da aula: recebe o DAO e repassa operações.
class HabitoRepository(private val dao: HabitoDao, private val banco: AppDatabase) {
    fun buscarTodos() = dao.buscarTodos()
    suspend fun inserir(habito: Habito) = dao.inserir(habito)
    suspend fun atualizar(habito: Habito) = dao.atualizar(habito)
    suspend fun remover(habito: Habito) = dao.remover(habito)
    suspend fun buscarPorId(id: Int) = dao.buscarPorId(id)
    suspend fun removerTodos() = dao.removerTodos()

    // Concluir precisa salvar o registro e o saldo juntos.
    suspend fun alternarConclusao(id: Int, dia: Int) = banco.withTransaction {
        val habito = checkNotNull(dao.buscarPorId(id))
        val perfil = checkNotNull(banco.perfilDao().buscar())
        val anterior = banco.registroDao().buscarConclusao(id, dia)
        if (anterior == null) {
            banco.registroDao().inserir(Registro(dia = dia, tipo = TipoRegistro.CONCLUSAO,
                habitoId = id, nomeHabito = habito.nome, categoria = habito.categoria,
                minutos = habito.minutosRecompensa))
            banco.perfilDao().salvar(perfil.copy(saldoMinutos = perfil.saldoMinutos + habito.minutosRecompensa))
        } else {
            banco.registroDao().remover(anterior)
            banco.perfilDao().salvar(perfil.copy(saldoMinutos = (perfil.saldoMinutos - anterior.minutos).coerceAtLeast(0)))
        }
    }

    suspend fun registrarRecaida(id: Int, dia: Int) = banco.withTransaction {
        val habito = checkNotNull(dao.buscarPorId(id))
        require(habito.tipo == TipoHabito.EVITAR)
        val anterior = banco.registroDao().buscarConclusao(id, dia)
        if (anterior != null) {
            val perfil = checkNotNull(banco.perfilDao().buscar())
            banco.registroDao().remover(anterior)
            banco.perfilDao().salvar(perfil.copy(saldoMinutos = (perfil.saldoMinutos - anterior.minutos).coerceAtLeast(0)))
        }
        banco.registroDao().inserir(Registro(dia = dia, tipo = TipoRegistro.RECAIDA,
            habitoId = id, nomeHabito = habito.nome, categoria = habito.categoria))
        dao.atualizar(habito.copy(ultimaRecaida = dia))
    }

    suspend fun registrarFoco(id: Int, minutos: Int, dia: Int) = banco.withTransaction {
        require(minutos > 0)
        val habito = checkNotNull(dao.buscarPorId(id))
        val perfil = checkNotNull(banco.perfilDao().buscar())
        banco.registroDao().inserir(Registro(dia = dia, tipo = TipoRegistro.FOCO,
            habitoId = id, nomeHabito = habito.nome, categoria = habito.categoria, minutos = minutos))
        banco.perfilDao().salvar(perfil.copy(saldoMinutos = perfil.saldoMinutos + minutos / 5))
    }
}
