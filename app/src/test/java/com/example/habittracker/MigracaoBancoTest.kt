package com.example.habittracker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.habittracker.data.*
import com.example.habittracker.model.*
import kotlinx.coroutines.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@SQLiteMode(SQLiteMode.Mode.LEGACY)
class MigracaoBancoTest {
    private val contexto: Context = ApplicationProvider.getApplicationContext()

    private fun criarAntigo(nome: String, comPerfil: Boolean) {
        val esquema = JSONObject(checkNotNull(javaClass.classLoader?.getResourceAsStream("legacy-v1.json"))
            .bufferedReader().use { it.readText() }).getJSONObject("database")
        contexto.openOrCreateDatabase(nome, Context.MODE_PRIVATE, null).use { db ->
            val entidades = esquema.getJSONArray("entities")
            for (i in 0 until entidades.length()) {
                val e = entidades.getJSONObject(i)
                db.execSQL(e.getString("createSql").replace("\${TABLE_NAME}", e.getString("tableName")))
                val indices = e.optJSONArray("indices")
                if (indices != null) for (j in 0 until indices.length()) {
                    db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}",e.getString("tableName")))
                }
            }
            db.execSQL("INSERT INTO habitos VALUES(5,'Caminhar antigo','MOVIMENTO','SEG,QUA,DOM','TARDE','FORCA',15)")
            db.execSQL("INSERT INTO recompensas VALUES(3,'Filme antigo',20)")
            db.execSQL("INSERT INTO registros VALUES(1,'CONCLUSAO',15,5,NULL,15)")
            db.execSQL("INSERT INTO registros VALUES(2,'RESGATE',15,NULL,3,20)")
            if (comPerfil) db.execSQL("INSERT INTO perfil VALUES(1,35,7,2,3,4,5)")
            val setup = esquema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) db.execSQL(setup.getString(i))
            db.version = 1
        }
    }
    @Test fun migracaoPreservaCadastrosSaldoHistoricoEPontosSemDuplicacao() = runBlocking(Dispatchers.IO) {
        val nome = "migracao-${UUID.randomUUID()}.db"
        criarAntigo(nome, true)
        val banco = AppDatabase.criar(contexto, nome)
        try {
            val h = checkNotNull(banco.habitoDao().buscarPorId(5))
            assertEquals("Caminhar antigo", h.nome)
            assertEquals(Categoria.CORPO, h.categoria)
            assertEquals(listOf(1,3,0), h.diasSemana)
            assertEquals(15, h.minutosRecompensa)
            assertEquals(1, banco.recompensaDao().buscarPorId(3)?.vezesResgatada)
            val registros = banco.registroDao().buscarTodos()
            assertEquals(2, registros.size)
            assertEquals("Caminhar antigo", registros.first().nomeHabito)
            val perfil = checkNotNull(banco.perfilDao().buscar())
            assertEquals(35, perfil.saldoMinutos)
            assertEquals(7, perfil.forca + registros.count { it.tipo == TipoRegistro.CONCLUSAO && it.categoria == Categoria.CORPO } * 2)
            assertTrue(perfil.notificacoesAtivas)
        } finally { banco.close(); contexto.deleteDatabase(nome) }
    }
    @Test fun migracaoSemPerfilNaoRecriaExemplosNemApagaCadastros() = runBlocking(Dispatchers.IO) {
        val nome = "migracao-sem-perfil-${UUID.randomUUID()}.db"
        criarAntigo(nome, false)
        val banco = AppDatabase.criar(contexto, nome)
        try {
            assertNotNull(banco.perfilDao().buscar())
            assertEquals(1, banco.habitoDao().buscarTodos().size)
            assertEquals("Caminhar antigo", banco.habitoDao().buscarTodos().single().nome)
        } finally { banco.close(); contexto.deleteDatabase(nome) }
    }
}
