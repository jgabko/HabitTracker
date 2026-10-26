package com.example.habittracker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.lifecycle.ViewModelStore
import com.example.habittracker.data.*
import com.example.habittracker.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode
import java.util.UUID
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@SQLiteMode(SQLiteMode.Mode.LEGACY)
class IntegracaoTrabalho2Test {
    private val contexto: Context = ApplicationProvider.getApplicationContext()
    private val nome = "integracao-${UUID.randomUUID()}.db"
    private lateinit var banco: AppDatabase
    private lateinit var estado: AppState
    private lateinit var principal: ExecutorCoroutineDispatcher
    private val store = ViewModelStore()

    @Before fun preparar() = runBlocking {
        principal = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        Dispatchers.setMain(principal)
        banco = AppDatabase.criar(contexto, nome)
        estado = AppState(banco)
        store.put("estado", estado)
        estado.recarregar().join()
        assertNull(estado.erro)
    }
    @After fun encerrar() {
        store.clear()
        banco.close()
        contexto.deleteDatabase(nome)
        Dispatchers.resetMain()
        principal.close()
    }
    @Test fun exemplosOriginaisConquistasECalculosSaoPreservados() {
        assertEquals(8, estado.habitos.size)
        assertEquals(4, estado.recompensas.size)
        assertEquals(45, estado.saldoMinutos)
        assertEquals(2, estado.habitos.count { it.tipo == TipoHabito.EVITAR })
        assertEquals(3, estado.habitoPorId(7)?.ultimaRecaida)
        assertEquals(275, estado.minutosFocoTotal())
        assertTrue(estado.pontosTotais() > 0)
        assertTrue(listaDeConquistas(estado).any { it.desbloqueada })
    }
    @Test fun crudNasMesmasListasEPersistenciaSemRecriarExemplos() = runBlocking {
        estado.adicionarHabito("Integrado", Categoria.CORPO, TipoHabito.EVITAR,
            diasSemana = listOf(1,3), onSucesso = {}).join()
        val h = estado.habitos.first { it.nome == "Integrado" }
        estado.atualizarHabito(h.copy(nome = "Editado", minutosRecompensa = 20)).join()
        assertEquals("Editado", estado.buscarHabitoNoBanco(h.id)?.nome)
        estado.adicionarRecompensa("Passeio", 15).join()
        val r = estado.recompensas.first { it.nome == "Passeio" }
        estado.atualizarRecompensa(r.copy(nome = "Passeio editado", custoMinutos = 25)).join()
        estado.alterarNotificacoes(false).join()
        banco.close()
        banco = AppDatabase.criar(contexto, nome)
        val reaberto = AppState(banco)
        store.put("reaberto", reaberto)
        reaberto.recarregar().join()
        assertEquals("Editado", reaberto.buscarHabitoNoBanco(h.id)?.nome)
        assertEquals(25, reaberto.buscarRecompensaNoBanco(r.id)?.custoMinutos)
        assertFalse(reaberto.notificacoesAtivas)
        reaberto.habitos.toList().forEach { reaberto.removerHabito(it.id).join() }
        reaberto.recompensas.toList().forEach { reaberto.removerRecompensa(it.id).join() }
        reaberto.recarregar().join()
        assertTrue(reaberto.habitos.isEmpty())
        assertTrue(reaberto.recompensas.isEmpty())
        assertTrue(reaberto.registros.isNotEmpty())
        assertNull(reaberto.erro)
    }
    @Test fun conclusaoEdicaoRecaidaFocoEResgateAtualizamBancoESaldo() = runBlocking {
        estado.adicionarHabito("Evitar", Categoria.DISCIPLINA, TipoHabito.EVITAR).join()
        val h = estado.habitos.first { it.nome == "Evitar" }
        val saldo = estado.saldoMinutos
        val pontos = estado.pontosDe(Atributo.DISCIPLINA)
        estado.alternarConclusaoHoje(h.id).join()
        assertEquals(saldo + 10, estado.saldoMinutos)
        assertEquals(pontos + 2, estado.pontosDe(Atributo.DISCIPLINA))
        estado.atualizarHabito(h.copy(minutosRecompensa = 20)).join()
        estado.alternarConclusaoHoje(h.id).join()
        assertEquals(saldo, estado.saldoMinutos)
        estado.alternarConclusaoHoje(h.id).join()
        estado.registrarRecaida(h.id).join()
        assertEquals(saldo, estado.saldoMinutos)
        assertFalse(estado.concluidoHoje(h.id))
        assertEquals(0, estado.diasSem(checkNotNull(estado.habitoPorId(h.id))))
        val foco = estado.pontosDe(Atributo.FOCO)
        estado.registrarSessaoFoco(h.id, 25).join()
        assertEquals(saldo + 5, estado.saldoMinutos)
        assertEquals(foco + 2, estado.pontosDe(Atributo.FOCO))
        estado.adicionarRecompensa("Resgate", 5).join()
        val r = estado.recompensas.first { it.nome == "Resgate" }
        estado.resgatarRecompensa(r.id).join()
        assertEquals(saldo, estado.saldoMinutos)
        assertEquals(1, estado.recompensaPorId(r.id)?.vezesResgatada)
        assertTrue(estado.registros.any { it.tipo == TipoRegistro.RESGATE && it.recompensaId == r.id })
        assertNull(estado.erro)
    }
    @Test fun validacaoNaoGravaNemChamaSucessoEResetEAtomico() = runBlocking {
        val quantidade = estado.habitos.size
        var sucesso = false
        estado.adicionarHabito("", Categoria.MENTE, onSucesso = { sucesso = true }).join()
        assertFalse(sucesso)
        assertNotNull(estado.erro)
        assertEquals(quantidade, estado.habitos.size)
        estado.adicionarRecompensa("Muito cara", 9999).join()
        val recompensa = estado.recompensas.first { it.nome == "Muito cara" }
        val saldo = estado.saldoMinutos
        estado.resgatarRecompensa(recompensa.id, onSucesso = { sucesso = true }).join()
        assertFalse(sucesso)
        assertEquals(saldo, estado.saldoMinutos)
        assertEquals(0, estado.recompensaPorId(recompensa.id)?.vezesResgatada)
        estado.restaurarDadosDeExemplo().join()
        assertEquals(8, estado.habitos.size)
        assertEquals(4, estado.recompensas.size)
        assertEquals(45, estado.saldoMinutos)
        assertNull(estado.erro)
    }
}
