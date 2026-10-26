package com.example.habittracker.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.habittracker.model.*

@Database(entities = [Habito::class, Recompensa::class, Registro::class, Perfil::class], version = 2, exportSchema = true)
@TypeConverters(Conversores::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitoDao(): HabitoDao
    abstract fun recompensaDao(): RecompensaDao
    abstract fun registroDao(): RegistroDao
    abstract fun perfilDao(): PerfilDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instancia ?: synchronized(this) {
            instancia ?: criar(context).also { instancia = it }
        }

        fun criar(context: Context, nome: String = "habittracker.db"): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, nome)
                .addMigrations(MIGRACAO_1_2).build()

        /** Converte a base simplificada anterior sem apagar cadastros, saldo ou histórico. */
        val MIGRACAO_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val categoria = """CASE categoria WHEN 'MOVIMENTO' THEN 'CORPO'
                    WHEN 'ESTUDO' THEN 'MENTE' WHEN 'ROTINA' THEN 'DISCIPLINA'
                    ELSE categoria END""".trimIndent()
                db.execSQL("""CREATE TABLE habitos_novo (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nome TEXT NOT NULL,
                    tipo TEXT NOT NULL, categoria TEXT NOT NULL, emoji TEXT NOT NULL,
                    periodo TEXT NOT NULL, diasSemana TEXT NOT NULL,
                    minutosRecompensa INTEGER NOT NULL, ultimaRecaida INTEGER NOT NULL)""")
                db.execSQL("""INSERT INTO habitos_novo SELECT id, nome, 'FAZER', $categoria,
                    CASE categoria WHEN 'SAUDE' THEN '💧' WHEN 'MOVIMENTO' THEN '🏋️'
                    WHEN 'ROTINA' THEN '🛡️' ELSE '📖' END, periodo,
                    replace(replace(replace(replace(replace(replace(replace(
                    diasDaSemana,'DOM','0'),'SEG','1'),'TER','2'),'QUA','3'),'QUI','4'),'SEX','5'),'SAB','6'),
                    minutosGanho, 0 FROM habitos""")
                db.execSQL("""CREATE TABLE recompensas_novo (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nome TEXT NOT NULL,
                    emoji TEXT NOT NULL, custoMinutos INTEGER NOT NULL, vezesResgatada INTEGER NOT NULL)""")
                db.execSQL("""INSERT INTO recompensas_novo SELECT id, nome, '🎁', custoMinutos,
                    (SELECT COUNT(*) FROM registros WHERE recompensaId = recompensas.id AND tipo = 'RESGATE')
                    FROM recompensas""")
                // O Trabalho 2 usa snapshots do nome e da categoria no histórico,
                // sem apagar atividades quando o cadastro é removido.
                db.execSQL("""CREATE TABLE registros_novo (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, dia INTEGER NOT NULL,
                    tipo TEXT NOT NULL, habitoId INTEGER NOT NULL, nomeHabito TEXT NOT NULL,
                    categoria TEXT NOT NULL, minutos INTEGER NOT NULL, recompensaId INTEGER)""")
                db.execSQL("""INSERT INTO registros_novo
                    SELECT r.id, r.dia, r.tipo, COALESCE(r.habitoId,0),
                    COALESCE(h.nome,p.nome,'Cadastro removido'),
                    COALESCE(h.categoria,'DISCIPLINA'), r.minutos, r.recompensaId
                    FROM registros r LEFT JOIN habitos_novo h ON h.id = r.habitoId
                    LEFT JOIN recompensas p ON p.id = r.recompensaId""")
                db.execSQL("""INSERT OR IGNORE INTO perfil
                    (id,saldoMinutos,forca,vitalidade,sabedoria,foco,disciplina)
                    VALUES (1,0,0,0,0,0,0)""")
                // Os pontos antigos eram armazenados no perfil; o ZIP os calcula
                // pelo histórico. Compensa a diferença para não contar duas vezes.
                for ((campo, cat) in listOf("forca" to "CORPO", "vitalidade" to "SAUDE",
                    "sabedoria" to "MENTE", "foco" to "TRABALHO", "disciplina" to "DISCIPLINA")) {
                    val foco = if (campo == "foco") "+ (SELECT COALESCE(SUM(minutos / 10),0) FROM registros_novo WHERE tipo = 'FOCO')" else ""
                    db.execSQL("""UPDATE perfil SET $campo = $campo - (
                        (SELECT COUNT(*) * 2 FROM registros_novo WHERE tipo = 'CONCLUSAO' AND categoria = '$cat') $foco)""")
                }
                db.execSQL("ALTER TABLE perfil ADD COLUMN notificacoesAtivas INTEGER NOT NULL DEFAULT 1")
                db.execSQL("DROP TABLE registros")
                db.execSQL("DROP TABLE habitos")
                db.execSQL("DROP TABLE recompensas")
                db.execSQL("ALTER TABLE habitos_novo RENAME TO habitos")
                db.execSQL("ALTER TABLE recompensas_novo RENAME TO recompensas")
                db.execSQL("ALTER TABLE registros_novo RENAME TO registros")
            }
        }
    }
}
