package com.example.habittracker.data.repository

import androidx.room.withTransaction
import com.example.habittracker.data.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Compartilhado pelos dois ViewModels para serializar operações que alteram várias tabelas. */
class DadosRepository(private val banco: AppDatabase) {
    val habitos = HabitoRepository(banco.habitoDao())
    val recompensas = RecompensaRepository(banco.recompensaDao())
    val registros = RegistroRepository(banco.registroDao())
    val perfil = PerfilRepository(banco.perfilDao())
    private val mutex = Mutex()

    // Cada lista vem de um Flow do Room; não há busca manual depois de salvar.
    fun observarDados() = combine(habitos.buscarTodos(), recompensas.buscarTodos(),
        registros.buscarTodos(), perfil.buscarTodos()) { h, r, registros, p ->
        Dados(h, r, registros, p.firstOrNull() ?: Perfil(id = 1))
    }

    suspend fun transacao(acao: suspend () -> Unit) = mutex.withLock {
        banco.withTransaction {
            if (perfil.buscar() == null) restaurarExemplos()
            acao()
        }
    }

    suspend fun inicializar() = transacao { }

    // Só é chamado dentro da transação; preserva a primeira instalação do Trabalho 3.
    suspend fun restaurarExemplos() {
        val exemplos = DadosExemplo()
        registros.removerTodos()
        habitos.removerTodos()
        recompensas.removerTodos()
        exemplos.habitos.forEach { habitos.inserir(it) }
        exemplos.recompensas.forEach { recompensas.inserir(it) }
        exemplos.registros.forEach { registros.inserir(it) }
        perfil.salvar(Perfil(id = 1, saldoMinutos = 45))
    }
}

data class Dados(val habitos: List<Habito>, val recompensas: List<Recompensa>,
    val registros: List<Registro>, val perfil: Perfil)
