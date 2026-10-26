package com.example.habittracker

import com.example.habittracker.dados.*
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversoresTest {
    private val conversores = Conversores()

    @Test fun diasMantemSelecaoSemDependerDaOrdem() {
        val dias = setOf(DiaDaSemana.SEX, DiaDaSemana.SEG, DiaDaSemana.DOM)
        val salvo = conversores.diasParaTexto(dias)
        assertEquals("SEG,SEX,DOM", salvo)
        assertEquals(dias, conversores.textoParaDias(salvo))
        assertEquals(emptySet<DiaDaSemana>(), conversores.textoParaDias(""))
    }

    @Test fun enumsMantemIdentidadeAoSalvarECarregar() {
        CategoriaHabito.entries.forEach { assertEquals(it, conversores.textoParaCategoria(conversores.categoriaParaTexto(it))) }
        PeriodoDoDia.entries.forEach { assertEquals(it, conversores.textoParaPeriodo(conversores.periodoParaTexto(it))) }
        Atributo.entries.forEach { assertEquals(it, conversores.textoParaAtributo(conversores.atributoParaTexto(it))) }
        TipoRegistro.entries.forEach { assertEquals(it, conversores.textoParaTipo(conversores.tipoParaTexto(it))) }
    }
}
