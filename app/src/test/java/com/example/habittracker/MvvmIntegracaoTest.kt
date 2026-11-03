package com.example.habittracker

import android.content.Context
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.example.habittracker.data.*
import com.example.habittracker.data.repository.*
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
    private lateinit var habitosRepository: HabitoRepository
    private lateinit var recompensasRepository: RecompensaRepository
    private lateinit var registrosRepository: RegistroRepository
    private lateinit var perfilRepository: PerfilRepository
    private lateinit var habitos: HabitosViewModel
    private lateinit var recompensas: RecompensasViewModel
    private lateinit var principal: ExecutorCoroutineDispatcher
    private val store = ViewModelStore()

    @Before fun preparar() = runBlocking<Unit> {
        principal = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        Dispatchers.setMain(principal)
        banco = AppDatabase.criar(contexto, nome)
        habitosRepository = HabitoRepository(banco.habitoDao(), banco)
        recompensasRepository = RecompensaRepository(banco.recompensaDao(), banco)
        registrosRepository = RegistroRepository(banco.registroDao())
        perfilRepository = PerfilRepository(banco.perfilDao(), banco)
        habitos = HabitosViewModelFactory(habitosRepository, recompensasRepository,
            registrosRepository, perfilRepository).create(HabitosViewModel::class.java)
        recompensas = RecompensasViewModelFactory(recompensasRepository,
            perfilRepository).create(RecompensasViewModel::class.java)
        store.put("habitos", habitos)
        store.put("recompensas", recompensas)
        aguardar { it.habitos.size == 8 && it.recompensas.size == 4 && it.saldoMinutos == 45 }
    }
    @After fun encerrar() = runBlocking<Unit> {
        withContext(principal) { store.clear() }
        banco.close()
        contexto.deleteDatabase(nome)
        Dispatchers.resetMain()
        principal.close()
    }
    private suspend fun aguardar(condicao: (HabitosUiState) -> Boolean) = withTimeout(15000) {
        while (true) {
            val h = habitos.uiState.value
            val r = recompensas.uiState.value
            if (!h.carregando && condicao(h) && h.perfil == r.perfil && h.recompensas == r.recompensas) break
            delay(10)
        }
    }
    private suspend fun novoHabito(nome: String, categoria: Categoria, tipo: TipoHabito = TipoHabito.FAZER) {
        withContext(principal) {
            habitos.onNovo()
            habitos.onNomeChange(nome)
            habitos.onCategoriaChange(categoria)
            habitos.onTipoChange(tipo)
            habitos.onSalvar()
        }
    }
    private suspend fun novaRecompensa(nome: String, custo: Int) {
        withContext(principal) {
            recompensas.onNovo()
            recompensas.onNomeChange(nome)
            recompensas.onCustoChange(custo.toString())
            recompensas.onSalvar()
        }
    }
    @Test fun exemplosCalculosEConquistasPreservados() {
        val estado = habitos.uiState.value
        assertEquals(45, estado.saldoMinutos)
        assertEquals(2, estado.habitos.count { it.tipo == TipoHabito.EVITAR })
        assertEquals(3, estado.habitoPorId(7)?.ultimaRecaida)
        assertEquals(275, estado.minutosFocoTotal())
        assertTrue(listaDeConquistas(estado).any { it.desbloqueada })
    }
    @Test fun crudAtualizaOsDoisUiStatesSemRecarregar() = runBlocking<Unit> {
        novoHabito("Novo", Categoria.CORPO)
        aguardar { it.habitos.any { h -> h.nome == "Novo" } }
        val h = habitos.uiState.value.habitos.last()
        withContext(principal) {
            habitos.onEditar(h)
            habitos.onNomeChange("Editado")
            habitos.onSalvar()
        }
        aguardar { it.habitoPorId(h.id)?.nome == "Editado" }
        novaRecompensa("Passeio", 15)
        aguardar { it.recompensas.any { r -> r.nome == "Passeio" } }
        val r = recompensas.uiState.value.recompensas.last()
        withContext(principal) {
            recompensas.onEditar(r)
            recompensas.onCustoChange("25")
            recompensas.onSalvar()
        }
        aguardar { it.recompensaPorId(r.id)?.custoMinutos == 25 }
        habitos.removerHabito(h.id).join()
        recompensas.removerRecompensa(r.id).join()
        aguardar { it.habitoPorId(h.id) == null && it.recompensaPorId(r.id) == null }
        assertNull(habitos.uiState.value.erro)
        assertNull(recompensas.uiState.value.erro)
    }
    @Test fun alteracaoExternaAoViewModelChegaPeloFlow() = runBlocking<Unit> {
        // Simula uma mudança de dados que a tela não solicitou.
        val id = habitosRepository.inserir(Habito(nome = "Externo", tipo = TipoHabito.FAZER, categoria = Categoria.MENTE)).toInt()
        aguardar { it.habitoPorId(id)?.nome == "Externo" }
        val item = checkNotNull(habitosRepository.buscarPorId(id))
        habitosRepository.atualizar(item.copy(nome = "Externo editado"))
        aguardar { it.habitoPorId(id)?.nome == "Externo editado" }
        habitosRepository.remover(item)
        aguardar { it.habitoPorId(id) == null }
    }
    @Test fun saldoConclusaoRecaidaFocoEResgateContinuamAtomicos() = runBlocking<Unit> {
        novoHabito("Evitar", Categoria.DISCIPLINA, TipoHabito.EVITAR)
        aguardar { it.habitos.any { h -> h.nome == "Evitar" } }
        val h = habitos.uiState.value.habitos.last()
        habitos.alternarConclusaoHoje(h.id).join()
        aguardar { it.saldoMinutos == 55 && it.concluidoHoje(h.id) }
        habitosRepository.atualizar(h.copy(minutosRecompensa = 20))
        habitos.alternarConclusaoHoje(h.id).join()
        aguardar { it.saldoMinutos == 45 && !it.concluidoHoje(h.id) }
        habitos.alternarConclusaoHoje(h.id).join()
        habitos.registrarRecaida(h.id).join()
        aguardar { it.saldoMinutos == 45 && !it.concluidoHoje(h.id) }
        habitos.registrarSessaoFoco(h.id, 25).join()
        aguardar { it.saldoMinutos == 50 }
        novaRecompensa("Pausa", 15)
        aguardar { it.recompensas.any { r -> r.nome == "Pausa" } }
        val r = recompensas.uiState.value.recompensas.last()
        recompensas.resgatarRecompensa(r.id).join()
        aguardar { it.saldoMinutos == 35 && it.recompensaPorId(r.id)?.vezesResgatada == 1 }
        recompensasRepository.atualizar(r.copy(custoMinutos = 999))
        recompensas.resgatarRecompensa(r.id).join()
        assertNotNull(recompensas.uiState.value.erro)
        assertEquals(35, perfilRepository.buscar()?.saldoMinutos)
    }
    @Test fun doisResgatesSimultaneosNaoGastamOMesmoSaldo() = runBlocking<Unit> {
        val id = recompensasRepository.inserir(Recompensa(nome = "Pausa", custoMinutos = 30)).toInt()
        val resultados = withContext(Dispatchers.IO) {
            val primeiro = async { runCatching { recompensasRepository.resgatar(id, 15) }.isSuccess }
            val segundo = async { runCatching { recompensasRepository.resgatar(id, 15) }.isSuccess }
            listOf(primeiro.await(), segundo.await())
        }
        assertEquals(1, resultados.count { it })
        assertEquals(15, perfilRepository.buscar()?.saldoMinutos)
        assertEquals(1, recompensasRepository.buscarPorId(id)?.vezesResgatada)
        assertEquals(1, registrosRepository.buscarTodos().first().count { it.recompensaId == id })
    }
    @Test fun formulariosValidacaoEEdicaoPermanecemNoUiState() = runBlocking<Unit> {
        withContext(principal) {
            habitos.onSalvar()
            assertTrue(habitos.uiState.value.nomeInvalido)
            habitos.onNomeChange("Formulário")
            habitos.onSalvar()
        }
        aguardar { it.habitos.any { h -> h.nome == "Formulário" } }
        withTimeout(15000) { habitos.uiState.first { it.nome.isEmpty() && !it.salvando } }
        val h = habitos.uiState.value.habitos.last()
        withContext(principal) {
            habitos.onEditar(h)
            habitos.onNomeChange("Rascunho")
            habitos.onEditar(h)
            assertEquals("Rascunho", habitos.uiState.value.nome)
            habitos.onSalvar()
            recompensas.onSalvar()
            assertTrue(recompensas.uiState.value.custoInvalido)
        }
        aguardar { it.habitoPorId(h.id)?.nome == "Rascunho" }
    }
    @Test fun reabrirPreservaDadosERemoverTudoNaoRecriaExemplos() = runBlocking<Unit> {
        habitos.alterarNotificacoes(false).join()
        habitosRepository.removerTodos()
        recompensasRepository.removerTodos()
        aguardar { it.habitos.isEmpty() && it.recompensas.isEmpty() && !it.notificacoesAtivas }
        withContext(principal) { store.clear() }
        banco.close()
        banco = AppDatabase.criar(contexto, nome)
        val reaberto = PerfilRepository(banco.perfilDao(), banco)
        reaberto.inicializar()
        assertTrue(banco.habitoDao().buscarTodos().first().isEmpty())
        assertTrue(banco.recompensaDao().buscarTodos().first().isEmpty())
        assertTrue(banco.registroDao().buscarTodos().first().isNotEmpty())
        assertFalse(checkNotNull(reaberto.buscar()).notificacoesAtivas)
    }
}
