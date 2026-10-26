package com.example.habittracker.dados

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Habito::class, Recompensa::class, Registro::class, Perfil::class], version = 1, exportSchema = true)
@TypeConverters(Conversores::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitoDao(): HabitoDao
    abstract fun recompensaDao(): RecompensaDao
    abstract fun registroDao(): RegistroDao
    abstract fun perfilDao(): PerfilDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "habittracker.db"
            ).build().also { instancia = it }
        }
    }
}
