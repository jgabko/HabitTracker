package com.example.habittracker

import com.example.habittracker.data.Conversores
import com.example.habittracker.model.*
import org.junit.Assert.*
import org.junit.Test

class ConversoresTest {
    private val conversores = Conversores()
    @Test fun enumsFazemIdaEVolta() {
        Categoria.entries.forEach { assertEquals(it, conversores.textoParaCategoria(conversores.categoriaParaTexto(it))) }
        Periodo.entries.forEach { assertEquals(it, conversores.textoParaPeriodo(conversores.periodoParaTexto(it))) }
        TipoHabito.entries.forEach { assertEquals(it, conversores.textoParaTipoHabito(conversores.tipoHabitoParaTexto(it))) }
        TipoRegistro.entries.forEach { assertEquals(it, conversores.textoParaRegistro(conversores.registroParaTexto(it))) }
    }
    @Test fun diasMantemValoresUnicosEmOrdem() {
        assertEquals(listOf(0,1,6), conversores.textoParaDias(conversores.diasParaTexto(listOf(6,1,0,1))))
        assertEquals(emptyList<Int>(), conversores.textoParaDias(""))
    }
}
