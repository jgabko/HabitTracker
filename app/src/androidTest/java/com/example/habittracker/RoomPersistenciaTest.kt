package com.example.habittracker

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.habittracker.dados.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomPersistenciaTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun abrir(nome: String): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, nome).build()

    private fun habito(nome: String = "Caminhar") = Habito(
        nome = nome, categoria = CategoriaHabito.MOVIMENTO,
        diasDaSemana = setOf(DiaDaSemana.SEG, DiaDaSemana.QUA),
        periodo = PeriodoDoDia.TARDE, atributo = Atributo.FORCA, minutosGanho = 15
    )

    @Test
    fun crudCompletoDosDoisDaos() = runBlocking(Dispatchers.IO) {
        val nome = "crud-${UUID.randomUUID()}.db"
        val db = abrir(nome)
        try {
            val hId = db.habitoDao().inserir(habito()).toInt()
            val rId = db.recompensaDao().inserir(Recompensa(nome = "Filme", custoMinutos = 30)).toInt()
            assertTrue(hId > 0)
            assertTrue(rId > 0)
            assertEquals(1, db.habitoDao().buscarTodos().size)
            assertEquals(1, db.recompensaDao().buscarTodos().size)
            val h = requireNotNull(db.habitoDao().buscarPorId(hId))
            val r = requireNotNull(db.recompensaDao().buscarPorId(rId))
            assertEquals(setOf(DiaDaSemana.SEG, DiaDaSemana.QUA), h.diasDaSemana)
            assertEquals(1, db.habitoDao().atualizar(h.copy(nome = "Caminhar 30 min", minutosGanho = 20)))
            assertEquals(1, db.recompensaDao().atualizar(r.copy(nome = "Cinema", custoMinutos = 60)))
            assertEquals("Caminhar 30 min", db.habitoDao().buscarPorId(hId)?.nome)
            assertEquals(60, db.recompensaDao().buscarPorId(rId)?.custoMinutos)
            assertEquals(1, db.habitoDao().remover(requireNotNull(db.habitoDao().buscarPorId(hId))))
            assertEquals(1, db.recompensaDao().remover(requireNotNull(db.recompensaDao().buscarPorId(rId))))
            assertNull(db.habitoDao().buscarPorId(hId))
            assertNull(db.recompensaDao().buscarPorId(rId))
            val novoId = db.habitoDao().inserir(habito("Novo hábito")).toInt()
            assertTrue(novoId > hId)
        } finally {
            db.close()
            context.deleteDatabase(nome)
        }
    }

    @Test
    fun dadosContinuamAoFecharEReabrirOArquivoDoBanco() = runBlocking(Dispatchers.IO) {
        val nome = "persistencia-${UUID.randomUUID()}.db"
        var db = abrir(nome)
        try {
            val id = db.habitoDao().inserir(habito("Persistência real")).toInt()
            val recompensaId = db.recompensaDao().inserir(Recompensa(nome = "Pausa", custoMinutos = 10)).toInt()
            db.perfilDao().salvar(Perfil(id = 1, saldoMinutos = 25, forca = 3))
            db.registroDao().inserir(Registro(tipo = TipoRegistro.CONCLUSAO, dia = DIA_HOJE, habitoId = id, minutos = 15))
            db.close()
            db = abrir(nome)
            assertEquals("Persistência real", db.habitoDao().buscarPorId(id)?.nome)
            assertEquals("Pausa", db.recompensaDao().buscarPorId(recompensaId)?.nome)
            assertEquals(25, db.perfilDao().buscar()?.saldoMinutos)
            assertNotNull(db.registroDao().buscarConclusao(id, DIA_HOJE))
            db.habitoDao().remover(requireNotNull(db.habitoDao().buscarPorId(id)))
            assertNull(db.registroDao().buscarTodos().single().habitoId)
            assertEquals(25, db.perfilDao().buscar()?.saldoMinutos)
        } finally {
            db.close()
            context.deleteDatabase(nome)
        }
    }

    @Test
    fun falhaEmTransacaoNaoDeixaEscritaParcial() = runBlocking(Dispatchers.IO) {
        val nome = "transacao-${UUID.randomUUID()}.db"
        val db = abrir(nome)
        try {
            try {
                db.withTransaction {
                    db.habitoDao().inserir(habito())
                    error("Falha simulada para verificar rollback")
                }
            } catch (_: IllegalStateException) { }
            assertTrue(db.habitoDao().buscarTodos().isEmpty())
        } finally {
            db.close()
            context.deleteDatabase(nome)
        }
    }
}
