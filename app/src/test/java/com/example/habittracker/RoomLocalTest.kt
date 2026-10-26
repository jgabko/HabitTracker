package com.example.habittracker

import android.content.Context
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.example.habittracker.data.*
import com.example.habittracker.model.*
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@SQLiteMode(SQLiteMode.Mode.LEGACY)
class RoomLocalTest {
    private val contexto: Context = ApplicationProvider.getApplicationContext()
    private val nome = "local-${UUID.randomUUID()}.db"
    @Test fun crudEReaberturaDoBancoEmArquivo() = runBlocking(Dispatchers.IO) {
        var banco = AppDatabase.criar(contexto, nome)
        try {
            val hId = banco.habitoDao().inserir(Habito(nome = "Persistência", tipo = TipoHabito.EVITAR,
                categoria = Categoria.SAUDE, diasSemana = listOf(1,5), periodo = Periodo.NOITE,
                minutosRecompensa = 15, ultimaRecaida = 3)).toInt()
            val rId = banco.recompensaDao().inserir(Recompensa(nome = "Pausa", custoMinutos = 20)).toInt()
            val h = checkNotNull(banco.habitoDao().buscarPorId(hId))
            val r = checkNotNull(banco.recompensaDao().buscarPorId(rId))
            banco.habitoDao().atualizar(h.copy(nome = "Editado"))
            banco.recompensaDao().atualizar(r.copy(custoMinutos = 30, vezesResgatada = 2))
            banco.perfilDao().salvar(Perfil(id = 1, saldoMinutos = 50, notificacoesAtivas = false))
            banco.registroDao().inserir(Registro(dia = 15, tipo = TipoRegistro.CONCLUSAO,
                habitoId = hId, nomeHabito = h.nome, categoria = h.categoria, minutos = 15))
            banco.close()
            banco = AppDatabase.criar(contexto, nome)
            assertEquals("Editado", banco.habitoDao().buscarPorId(hId)?.nome)
            assertEquals(TipoHabito.EVITAR, banco.habitoDao().buscarPorId(hId)?.tipo)
            assertEquals(listOf(1,5), banco.habitoDao().buscarTodos().single().diasSemana)
            assertEquals(3, banco.habitoDao().buscarTodos().single().ultimaRecaida)
            assertEquals(30, banco.recompensaDao().buscarTodos().single().custoMinutos)
            assertEquals(2, banco.recompensaDao().buscarTodos().single().vezesResgatada)
            assertEquals(50, banco.perfilDao().buscar()?.saldoMinutos)
            assertEquals(false, banco.perfilDao().buscar()?.notificacoesAtivas)
            banco.habitoDao().remover(checkNotNull(banco.habitoDao().buscarPorId(hId)))
            banco.recompensaDao().remover(checkNotNull(banco.recompensaDao().buscarPorId(rId)))
            banco.close()
            banco = AppDatabase.criar(contexto, nome)
            assertTrue(banco.habitoDao().buscarTodos().isEmpty())
            assertTrue(banco.recompensaDao().buscarTodos().isEmpty())
            assertEquals("Persistência", banco.registroDao().buscarTodos().single().nomeHabito)
            assertEquals(50, banco.perfilDao().buscar()?.saldoMinutos)
        } finally { banco.close(); contexto.deleteDatabase(nome) }
    }
    @Test fun falhaEmTransacaoDesfazGravacoes() = runBlocking(Dispatchers.IO) {
        val banco = AppDatabase.criar(contexto, nome)
        try {
            try {
                banco.withTransaction {
                    banco.recompensaDao().inserir(Recompensa(nome = "Não gravar", custoMinutos = 5))
                    error("Rollback")
                }
            } catch (_: IllegalStateException) { }
            assertTrue(banco.recompensaDao().buscarTodos().isEmpty())
        } finally { banco.close(); contexto.deleteDatabase(nome) }
    }
}
