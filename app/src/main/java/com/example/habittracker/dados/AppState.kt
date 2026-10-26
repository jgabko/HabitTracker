package com.example.habittracker.dados

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Mantém as listas reativas da interface. O banco é a fonte dos dados;
 * as listas são recarregadas após cada transação e ao abrir as telas.
 */
class AppState(private val banco: AppDatabase) : ViewModel() {
    val habitos = mutableStateListOf<Habito>()
    val recompensas = mutableStateListOf<Recompensa>()
    val registros = mutableStateListOf<Registro>()
    var saldoMinutos by mutableIntStateOf(0)
        private set
    var forca by mutableIntStateOf(0)
        private set
    var vitalidade by mutableIntStateOf(0)
        private set
    var sabedoria by mutableIntStateOf(0)
        private set
    var foco by mutableIntStateOf(0)
        private set
    var disciplina by mutableIntStateOf(0)
        private set
    var ultimaMensagem by mutableStateOf("")
        private set
    var erro by mutableStateOf<String?>(null)
        private set
    var carregando by mutableStateOf(true)
        private set
    var salvando by mutableStateOf(false)
        private set
    var versaoDados by mutableIntStateOf(0)
        private set

    private val mutex = Mutex()

    init { recarregar() }

    private data class Dados(
        val habitos: List<Habito>,
        val recompensas: List<Recompensa>,
        val registros: List<Registro>,
        val perfil: Perfil
    )

    private suspend fun lerDados() = Dados(
        banco.habitoDao().buscarTodos(),
        banco.recompensaDao().buscarTodos(),
        banco.registroDao().buscarTodos(),
        banco.perfilDao().buscar() ?: Perfil(id = 1)
    )

    private fun aplicar(dados: Dados) {
        habitos.clear()
        habitos.addAll(dados.habitos)
        recompensas.clear()
        recompensas.addAll(dados.recompensas)
        registros.clear()
        registros.addAll(dados.registros)
        saldoMinutos = dados.perfil.saldoMinutos
        forca = dados.perfil.forca
        vitalidade = dados.perfil.vitalidade
        sabedoria = dados.perfil.sabedoria
        foco = dados.perfil.foco
        disciplina = dados.perfil.disciplina
        versaoDados++
    }

    // viewModelScope é um CoroutineScope ligado à vida da tela.
    // launch(Dispatchers.IO) mantém todas as operações de banco fora da Main.
    private fun executar(
        mensagem: String? = null,
        onSucesso: () -> Unit = {},
        acao: suspend () -> Unit
    ): Job = viewModelScope.launch(Dispatchers.IO) {
        mutex.withLock {
            withContext(Dispatchers.Main) {
                salvando = true
                erro = null
            }
            try {
                banco.withTransaction { acao() }
                val dados = banco.withTransaction { lerDados() }
                withContext(Dispatchers.Main) {
                    aplicar(dados)
                    if (mensagem != null) {
                        ultimaMensagem = mensagem
                        Log.d(TAG_APP, mensagem)
                    }
                    onSucesso()
                }
            } catch (cancelamento: CancellationException) {
                throw cancelamento
            } catch (falha: Exception) {
                Log.e(TAG_APP, "Falha na operação do banco", falha)
                withContext(Dispatchers.Main) {
                    erro = falha.message ?: "Não foi possível acessar os dados."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    salvando = false
                    carregando = false
                }
            }
        }
    }

    fun recarregar(): Job = executar { }
    suspend fun carregarDados() { recarregar().join() }

    suspend fun buscarHabitoNoBanco(id: Int): Habito? =
        withContext(Dispatchers.IO) { banco.habitoDao().buscarPorId(id) }

    suspend fun buscarRecompensaNoBanco(id: Int): Recompensa? =
        withContext(Dispatchers.IO) { banco.recompensaDao().buscarPorId(id) }

    fun adicionarHabito(
        nome: String,
        categoria: CategoriaHabito,
        diasDaSemana: Set<DiaDaSemana> = DiaDaSemana.entries.toSet(),
        periodo: PeriodoDoDia = PeriodoDoDia.MANHA,
        atributo: Atributo = atributoPadrao(categoria),
        minutosGanho: Int = 10,
        onSucesso: () -> Unit = {}
    ) {
        executar("Hábito salvo.", onSucesso) {
            require(nome.isNotBlank()) { "Informe o nome do hábito." }
            require(diasDaSemana.isNotEmpty()) { "Selecione pelo menos um dia." }
            require(minutosGanho > 0) { "Os minutos devem ser maiores que zero." }
            banco.habitoDao().inserir(Habito(
                nome = nome.trim(), categoria = categoria, diasDaSemana = diasDaSemana,
                periodo = periodo, atributo = atributo, minutosGanho = minutosGanho
            ))
        }
    }

    fun atualizarHabito(habito: Habito, onSucesso: () -> Unit = {}) {
        executar("Hábito atualizado.", onSucesso) {
            require(habito.nome.isNotBlank() && habito.minutosGanho > 0 && habito.diasDaSemana.isNotEmpty()) {
                "Preencha um nome, dias e minutos válidos."
            }
            check(banco.habitoDao().atualizar(habito.copy(nome = habito.nome.trim())) == 1) {
                "O hábito não existe mais."
            }
        }
    }

    fun removerHabito(id: Int) { removerHabito(id, {}) }
    fun removerHabito(id: Int, onSucesso: () -> Unit) {
        executar("Hábito removido.", onSucesso) {
            val habito = banco.habitoDao().buscarPorId(id) ?: error("Hábito não encontrado.")
            banco.habitoDao().remover(habito)
        }
    }

    fun adicionarRecompensa(nome: String, custoMinutos: Int, onSucesso: () -> Unit = {}) {
        executar("Recompensa salva.", onSucesso) {
            require(nome.isNotBlank() && custoMinutos > 0) { "Informe um nome e um custo maior que zero." }
            banco.recompensaDao().inserir(Recompensa(nome = nome.trim(), custoMinutos = custoMinutos))
        }
    }

    fun atualizarRecompensa(recompensa: Recompensa, onSucesso: () -> Unit = {}) {
        executar("Recompensa atualizada.", onSucesso) {
            require(recompensa.nome.isNotBlank() && recompensa.custoMinutos > 0) { "Informe um nome e um custo válido." }
            check(banco.recompensaDao().atualizar(recompensa.copy(nome = recompensa.nome.trim())) == 1) {
                "A recompensa não existe mais."
            }
        }
    }

    fun removerRecompensa(id: Int) { removerRecompensa(id, {}) }
    fun removerRecompensa(id: Int, onSucesso: () -> Unit) {
        executar("Recompensa removida.", onSucesso) {
            val recompensa = banco.recompensaDao().buscarPorId(id) ?: error("Recompensa não encontrada.")
            banco.recompensaDao().remover(recompensa)
        }
    }

    fun concluirHabito(id: Int) {
        executar("Conclusão do hábito atualizada.") {
            val habito = banco.habitoDao().buscarPorId(id) ?: error("Hábito não encontrado.")
            val perfil = banco.perfilDao().buscar() ?: Perfil(id = 1)
            val anterior = banco.registroDao().buscarConclusao(id, DIA_HOJE)
            val delta = if (anterior == null) 1 else -1
            val minutos = anterior?.minutos ?: habito.minutosGanho
            if (anterior == null) {
                banco.registroDao().inserir(Registro(tipo = TipoRegistro.CONCLUSAO, dia = DIA_HOJE, habitoId = id, minutos = minutos))
            } else {
                banco.registroDao().remover(anterior)
            }
            banco.perfilDao().salvar(ajustarAtributo(
                perfil.copy(saldoMinutos = (perfil.saldoMinutos + delta * minutos).coerceAtLeast(0)),
                habito.atributo, delta
            ))
        }
    }

    fun registrarRecaida(id: Int) {
        executar("Recaída registrada.") {
            check(banco.habitoDao().buscarPorId(id) != null) { "Hábito não encontrado." }
            banco.registroDao().inserir(Registro(tipo = TipoRegistro.RECAIDA, dia = DIA_HOJE, habitoId = id))
        }
    }

    fun resgatarRecompensa(id: Int) {
        executar("Recompensa resgatada.") {
            val recompensa = banco.recompensaDao().buscarPorId(id) ?: error("Recompensa não encontrada.")
            val perfil = banco.perfilDao().buscar() ?: Perfil(id = 1)
            require(perfil.saldoMinutos >= recompensa.custoMinutos) { "Saldo insuficiente para resgatar." }
            banco.perfilDao().salvar(perfil.copy(saldoMinutos = perfil.saldoMinutos - recompensa.custoMinutos))
            banco.registroDao().inserir(Registro(tipo = TipoRegistro.RESGATE, dia = DIA_HOJE, recompensaId = id, minutos = recompensa.custoMinutos))
        }
    }

    fun registrarSessaoFoco(habitoId: Int, minutos: Int) {
        executar("Sessão de foco registrada.") {
            require(minutos > 0) { "Informe uma duração válida." }
            check(banco.habitoDao().buscarPorId(habitoId) != null) { "Hábito não encontrado." }
            banco.registroDao().inserir(Registro(tipo = TipoRegistro.FOCO, dia = DIA_HOJE, habitoId = habitoId, minutos = minutos))
            val perfil = banco.perfilDao().buscar() ?: Perfil(id = 1)
            banco.perfilDao().salvar(perfil.copy(foco = perfil.foco + 1))
        }
    }

    fun concluidoNoDia(habitoId: Int, dia: Int = DIA_HOJE): Boolean =
        registros.any { it.habitoId == habitoId && it.tipo == TipoRegistro.CONCLUSAO && it.dia == dia }

    fun habitosDoDia(dia: Int = DIA_HOJE): List<Habito> =
        habitos.filter { diaDaSemanaDoCalendario(dia) in it.diasDaSemana }

    fun habitoPorId(id: Int): Habito? = habitos.find { it.id == id }
    fun recompensaPorId(id: Int): Recompensa? = recompensas.find { it.id == id }

    fun valorAtributo(atributo: Atributo): Int = when (atributo) {
        Atributo.FORCA -> forca
        Atributo.VITALIDADE -> vitalidade
        Atributo.SABEDORIA -> sabedoria
        Atributo.FOCO -> foco
        Atributo.DISCIPLINA -> disciplina
    }

    // A carga de exemplos só ocorre por ação explícita, nunca ao abrir o app.
    fun resetarDadosExemplo() {
        executar("Dados de exemplo restaurados.") {
            banco.registroDao().removerTodos()
            banco.habitoDao().removerTodos()
            banco.recompensaDao().removerTodos()
            banco.perfilDao().salvar(Perfil(id = 1))
            banco.habitoDao().inserir(Habito(nome = "Beber água", categoria = CategoriaHabito.SAUDE, diasDaSemana = DiaDaSemana.entries.toSet(), periodo = PeriodoDoDia.MANHA, atributo = Atributo.VITALIDADE, minutosGanho = 10))
            banco.habitoDao().inserir(Habito(nome = "Ler 10 páginas", categoria = CategoriaHabito.ESTUDO, diasDaSemana = DiaDaSemana.entries.toSet(), periodo = PeriodoDoDia.NOITE, atributo = Atributo.SABEDORIA, minutosGanho = 10))
            banco.recompensaDao().inserir(Recompensa(nome = "Episódio de série", custoMinutos = 30))
        }
    }

    private fun ajustarAtributo(perfil: Perfil, atributo: Atributo, delta: Int): Perfil = when (atributo) {
        Atributo.FORCA -> perfil.copy(forca = (perfil.forca + delta).coerceAtLeast(0))
        Atributo.VITALIDADE -> perfil.copy(vitalidade = (perfil.vitalidade + delta).coerceAtLeast(0))
        Atributo.SABEDORIA -> perfil.copy(sabedoria = (perfil.sabedoria + delta).coerceAtLeast(0))
        Atributo.FOCO -> perfil.copy(foco = (perfil.foco + delta).coerceAtLeast(0))
        Atributo.DISCIPLINA -> perfil.copy(disciplina = (perfil.disciplina + delta).coerceAtLeast(0))
    }

    private fun atributoPadrao(categoria: CategoriaHabito): Atributo = when (categoria) {
        CategoriaHabito.SAUDE -> Atributo.VITALIDADE
        CategoriaHabito.MOVIMENTO -> Atributo.FORCA
        CategoriaHabito.MENTE -> Atributo.FOCO
        CategoriaHabito.ESTUDO -> Atributo.SABEDORIA
        CategoriaHabito.ROTINA -> Atributo.DISCIPLINA
    }
}
