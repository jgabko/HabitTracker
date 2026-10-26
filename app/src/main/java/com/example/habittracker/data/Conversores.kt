package com.example.habittracker.data

import androidx.room.TypeConverter
import com.example.habittracker.model.*

class Conversores {
    @TypeConverter fun categoriaParaTexto(valor: Categoria): String = valor.name
    @TypeConverter fun textoParaCategoria(valor: String): Categoria = Categoria.valueOf(valor)
    @TypeConverter fun tipoHabitoParaTexto(valor: TipoHabito): String = valor.name
    @TypeConverter fun textoParaTipoHabito(valor: String): TipoHabito = TipoHabito.valueOf(valor)
    @TypeConverter fun periodoParaTexto(valor: Periodo): String = valor.name
    @TypeConverter fun textoParaPeriodo(valor: String): Periodo = Periodo.valueOf(valor)
    @TypeConverter fun registroParaTexto(valor: TipoRegistro): String = valor.name
    @TypeConverter fun textoParaRegistro(valor: String): TipoRegistro = TipoRegistro.valueOf(valor)
    @TypeConverter fun diasParaTexto(valor: List<Int>): String = valor.distinct().sorted().joinToString(",")
    @TypeConverter fun textoParaDias(valor: String): List<Int> =
        if (valor.isBlank()) emptyList() else valor.split(",").map { it.toInt() }
}
