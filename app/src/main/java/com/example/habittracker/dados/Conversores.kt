package com.example.habittracker.dados

import androidx.room.TypeConverter

class Conversores {
    @TypeConverter fun categoriaParaTexto(valor: CategoriaHabito): String = valor.name
    @TypeConverter fun textoParaCategoria(valor: String): CategoriaHabito = CategoriaHabito.valueOf(valor)
    @TypeConverter fun periodoParaTexto(valor: PeriodoDoDia): String = valor.name
    @TypeConverter fun textoParaPeriodo(valor: String): PeriodoDoDia = PeriodoDoDia.valueOf(valor)
    @TypeConverter fun atributoParaTexto(valor: Atributo): String = valor.name
    @TypeConverter fun textoParaAtributo(valor: String): Atributo = Atributo.valueOf(valor)
    @TypeConverter fun tipoParaTexto(valor: TipoRegistro): String = valor.name
    @TypeConverter fun textoParaTipo(valor: String): TipoRegistro = TipoRegistro.valueOf(valor)
    @TypeConverter fun diasParaTexto(valor: Set<DiaDaSemana>): String = valor.sortedBy { it.numero }.joinToString(",") { it.name }
    @TypeConverter fun textoParaDias(valor: String): Set<DiaDaSemana> =
        if (valor.isBlank()) emptySet() else valor.split(",").map { DiaDaSemana.valueOf(it) }.toSet()
}
