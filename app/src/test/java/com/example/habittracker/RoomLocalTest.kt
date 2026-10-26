package com.example.habittracker

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.example.habittracker.dados.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode
import java.util.UUID

/** Verifica os DAOs gerados pelo Room no ambiente Android de testes da JVM. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@SQLiteMode(SQLiteMode.Mode.LEGACY)
class RoomLocalTest {
    @Test fun crudEReaberturaDoBancoEmArquivo() = runBlocking(Dispatchers.IO) {
        val contexto: Context = ApplicationProvider.getApplicationContext()
        val nome = "room-local-${UUID.randomUUID()}.db"
        fun abrir() = Room.databaseBuilder(contexto, AppDatabase::class.java, nome).build()
        var banco = abrir()
        try {
            val habitoId = banco.habitoDao().inserir(Habito(
                nome = "Persistência", categoria = CategoriaHabito.SAUDE,
                diasDaSemana = setOf(DiaDaSemana.SEG, DiaDaSemana.SEX),
                periodo = PeriodoDoDia.MANHA, atributo = Atributo.VITALIDADE, minutosGanho = 10
            )).toInt()
            val recompensaId = banco.recompensaDao().inserir(Recompensa(nome = "Pausa", custoMinutos = 20)).toInt()
            val habito = requireNotNull(banco.habitoDao().buscarPorId(habitoId))
            val recompensa = requireNotNull(banco.recompensaDao().buscarPorId(recompensaId))
            banco.habitoDao().atualizar(habito.copy(nome = "Persistência editada"))
            banco.recompensaDao().atualizar(recompensa.copy(custoMinutos = 30))
            banco.perfilDao().salvar(Perfil(id = 1, saldoMinutos = 50))
            banco.registroDao().inserir(Registro(tipo = TipoRegistro.CONCLUSAO, dia = DIA_HOJE, habitoId = habitoId, minutos = 10))
            banco.close()
            banco = abrir()
            assertEquals("Persistência editada", banco.habitoDao().buscarPorId(habitoId)?.nome)
            assertEquals(setOf(DiaDaSemana.SEG, DiaDaSemana.SEX), banco.habitoDao().buscarTodos().single().diasDaSemana)
            assertEquals(30, banco.recompensaDao().buscarTodos().single().custoMinutos)
            assertEquals(50, banco.perfilDao().buscar()?.saldoMinutos)
            assertNotNull(banco.registroDao().buscarConclusao(habitoId, DIA_HOJE))
            banco.habitoDao().remover(requireNotNull(banco.habitoDao().buscarPorId(habitoId)))
            banco.recompensaDao().remover(requireNotNull(banco.recompensaDao().buscarPorId(recompensaId)))
            banco.close()
            banco = abrir()
            assertTrue(banco.habitoDao().buscarTodos().isEmpty())
            assertTrue(banco.recompensaDao().buscarTodos().isEmpty())
            assertNull(banco.registroDao().buscarTodos().single().habitoId)
            assertEquals(50, banco.perfilDao().buscar()?.saldoMinutos)
        } finally { banco.close(); contexto.deleteDatabase(nome) }
    }

    @Test fun transacaoComFalhaDesfazGravacoes() = runBlocking(Dispatchers.IO) {
        val contexto: Context = ApplicationProvider.getApplicationContext()
        val nome = "rollback-local-${UUID.randomUUID()}.db"
        val banco = Room.databaseBuilder(contexto, AppDatabase::class.java, nome).build()
        try {
            try {
                banco.withTransaction {
                    banco.recompensaDao().inserir(Recompensa(nome = "Não deve ficar salva", custoMinutos = 5))
                    error("Falha simulada")
                }
            } catch (_: IllegalStateException) { }
            assertTrue(banco.recompensaDao().buscarTodos().isEmpty())
        } finally { banco.close(); contexto.deleteDatabase(nome) }
    }
}
