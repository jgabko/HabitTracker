package com.example.habittracker

import android.content.Context
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.example.habittracker.data.*
import com.example.habittracker.data.repository.DadosRepository
import com.example.habittracker.viewmodel.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
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
class MvvmIntegracaoTest {
    private val contexto: Context = ApplicationProvider.getApplicationContext()
    private val nome = "mvvm-${UUID.randomUUID()}.db"
    private lateinit var banco: AppDatabase
    private lateinit var repository: DadosRepository
    private lateinit var habitos: HabitosViewModel
    private lateinit var recompensas: RecompensasViewModel
    private lateinit var principal: ExecutorCoroutineDispatcher
    private val store = ViewModelStore()

    @Before fun preparar() = runBlocking<Unit> {
        principal = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        Dispatchers.setMain(principal)
        banco = AppDatabase.criar(contexto, nome)
        repository = DadosRepository(banco)
        habitos = HabitosViewModelFactory(repository).create(HabitosViewModel::class.java)
        recompensas = RecompensasViewModelFactory(repository).create(RecompensasViewModel::class.java)
        store.put("habitos", habitos)
        store.put("recompensas", recompensas)
        aguardar { it.habitos.size == 8 && it.recompensas.size == 4 }
    }
    @After fun encerrar() = runBlocking<Unit> {
        withContext(principal) { store.clear() }
        banco.close()
        contexto.deleteDatabase(nome)
        Dispatchers.resetMain()
        principal.close()
    }
    private suspend fun aguardar(condicao: (AppUiState) -> Boolean) = withTimeout(15000) {
        habitos.uiState.first { !it.carregando && condicao(it.dados) }
        recompensas.uiState.first { !it.carregando && condicao(it.dados) }
    }
    @Test fun exemplosCalculosEConquistasPreservados() {
        val estado = habitos.uiState.value.dados
        assertEquals(45, estado.saldoMinutos)
        assertEquals(2, estado.habitos.count { it.tipo == TipoHabito.EVITAR })
        assertEquals(3, estado.habitoPorId(7)?.ultimaRecaida)
        assertEquals(275, estado.minutosFocoTotal())
        assertTrue(listaDeConquistas(estado).any { it.desbloqueada })
    }
    @Test fun crudAtualizaOsDoisUiStatesSemRecarregar() = runBlocking<Unit> {
        habitos.adicionarHabito("Novo", Categoria.CORPO).join()
        aguardar { it.habitos.any { h -> h.nome == "Novo" } }
        val h = habitos.uiState.value.dados.habitos.last()
        habitos.atualizarHabito(h.copy(nome = "Editado")).join()
        aguardar { it.habitoPorId(h.id)?.nome == "Editado" }
        recompensas.adicionarRecompensa("Passeio", 15).join()
        aguardar { it.recompensas.any { r -> r.nome == "Passeio" } }
        val r = recompensas.uiState.value.dados.recompensas.last()
        recompensas.atualizarRecompensa(r.copy(custoMinutos = 25)).join()
        aguardar { it.recompensaPorId(r.id)?.custoMinutos == 25 }
        habitos.removerHabito(h.id).join()
        recompensas.removerRecompensa(r.id).join()
        aguardar { it.habitoPorId(h.id) == null && it.recompensaPorId(r.id) == null }
        assertNull(habitos.uiState.value.erro)
        assertNull(recompensas.uiState.value.erro)
    }
    @Test fun alteracaoExternaAoViewModelChegaPeloFlow() = runBlocking<Unit> {
        // Simula uma mudança de dados que a tela não solicitou.
        val id = repository.habitos.inserir(Habito(nome = "Externo", tipo = TipoHabito.FAZER, categoria = Categoria.MENTE)).toInt()
        aguardar { it.habitoPorId(id)?.nome == "Externo" }
        val item = checkNotNull(repository.habitos.buscarPorId(id))
        repository.habitos.atualizar(item.copy(nome = "Externo editado"))
        aguardar { it.habitoPorId(id)?.nome == "Externo editado" }
        repository.habitos.remover(item)
        aguardar { it.habitoPorId(id) == null }
    }
    @Test fun saldoConclusaoRecaidaFocoEResgateContinuamAtomicos() = runBlocking<Unit> {
        habitos.adicionarHabito("Evitar", Categoria.DISCIPLINA, TipoHabito.EVITAR).join()
        aguardar { it.habitos.any { h -> h.nome == "Evitar" } }
        val h = habitos.uiState.value.dados.habitos.last()
        habitos.alternarConclusaoHoje(h.id).join()
        aguardar { it.saldoMinutos == 55 && it.concluidoHoje(h.id) }
        habitos.atualizarHabito(h.copy(minutosRecompensa = 20)).join()
        habitos.alternarConclusaoHoje(h.id).join()
        aguardar { it.saldoMinutos == 45 && !it.concluidoHoje(h.id) }
        habitos.alternarConclusaoHoje(h.id).join()
        habitos.registrarRecaida(h.id).join()
        aguardar { it.saldoMinutos == 45 && !it.concluidoHoje(h.id) }
        habitos.registrarSessaoFoco(h.id, 25).join()
        aguardar { it.saldoMinutos == 50 }
        recompensas.adicionarRecompensa("Pausa", 15).join()
        aguardar { it.recompensas.any { r -> r.nome == "Pausa" } }
        val r = recompensas.uiState.value.dados.recompensas.last()
        recompensas.resgatarRecompensa(r.id).join()
        aguardar { it.saldoMinutos == 35 && it.recompensaPorId(r.id)?.vezesResgatada == 1 }
        recompensas.atualizarRecompensa(r.copy(custoMinutos = 999)).join()
        recompensas.resgatarRecompensa(r.id).join()
        assertNotNull(recompensas.uiState.value.erro)
        assertEquals(35, repository.perfil.buscar()?.saldoMinutos)
    }
    @Test fun formulariosValidacaoEEdicaoPermanecemNoUiState() = runBlocking<Unit> {
        withContext(principal) {
            habitos.onSalvar()
            assertTrue(habitos.uiState.value.formulario.nomeInvalido)
            habitos.onNomeChange("Formulário")
            habitos.onSalvar()
        }
        aguardar { it.habitos.any { h -> h.nome == "Formulário" } }
        withTimeout(15000) { habitos.uiState.first { it.formulario.nome.isEmpty() && !it.salvando } }
        val h = habitos.uiState.value.dados.habitos.last()
        withContext(principal) {
            habitos.onEditar(h)
            habitos.onNomeChange("Rascunho", "edicao")
            habitos.onEditar(h)
            assertEquals("Rascunho", habitos.uiState.value.edicao.nome)
            habitos.onSalvar("edicao")
            recompensas.onSalvar()
            assertTrue(recompensas.uiState.value.formulario.custoInvalido)
        }
        aguardar { it.habitoPorId(h.id)?.nome == "Rascunho" }
    }
    @Test fun reabrirPreservaDadosERemoverTudoNaoRecriaExemplos() = runBlocking<Unit> {
        habitos.alterarNotificacoes(false).join()
        repository.transacao {
            repository.habitos.removerTodos()
            repository.recompensas.removerTodos()
        }
        aguardar { it.habitos.isEmpty() && it.recompensas.isEmpty() && !it.notificacoesAtivas }
        withContext(principal) { store.clear() }
        banco.close()
        banco = AppDatabase.criar(contexto, nome)
        val reaberto = DadosRepository(banco)
        reaberto.inicializar()
        val dados = withTimeout(15000) { reaberto.observarDados().first() }
        assertTrue(dados.habitos.isEmpty())
        assertTrue(dados.recompensas.isEmpty())
        assertTrue(dados.registros.isNotEmpty())
        assertFalse(dados.perfil.notificacoesAtivas)
    }
}
