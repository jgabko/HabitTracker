package com.example.habittracker.data

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.habittracker.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Estado visual do Trabalho 2, alimentado por gravações e leituras do Room. */
class AppState(private val banco: AppDatabase) : ViewModel() {
    companion object {
        const val DIAS_NO_MES = 30
        const val PONTOS_POR_CONCLUSAO = 2
        const val MINUTOS_FOCO_POR_MINUTO_LAZER = 5
        const val MINUTOS_FOCO_POR_PONTO = 10
        const val PONTOS_POR_NIVEL = 50
    }
    // Mantém o calendário demonstrativo do Trabalho 2.
    val diaHoje = 15
    val habitos = mutableStateListOf<Habito>()
    val recompensas = mutableStateListOf<Recompensa>()
    val registros = mutableStateListOf<Registro>()
    private var perfil by mutableStateOf(Perfil(id = 1))
    val saldoMinutos get() = perfil.saldoMinutos
    val notificacoesAtivas get() = perfil.notificacoesAtivas
    var carregando by mutableStateOf(true)
        private set
    var salvando by mutableStateOf(false)
        private set
    var erro by mutableStateOf<String?>(null)
        private set
    var versaoDados by mutableIntStateOf(0)
        private set
    private val mutex = Mutex()

    init { recarregar() }

    private data class Dados(val habitos: List<Habito>, val recompensas: List<Recompensa>,
        val registros: List<Registro>, val perfil: Perfil)

    private suspend fun lerDados() = Dados(banco.habitoDao().buscarTodos(),
        banco.recompensaDao().buscarTodos(), banco.registroDao().buscarTodos(),
        checkNotNull(banco.perfilDao().buscar()))

    private suspend fun gravarExemplos() {
        val exemplos = DadosExemplo()
        banco.registroDao().removerTodos()
        banco.habitoDao().removerTodos()
        banco.recompensaDao().removerTodos()
        exemplos.habitos.forEach { banco.habitoDao().inserir(it) }
        exemplos.recompensas.forEach { banco.recompensaDao().inserir(it) }
        exemplos.registros.forEach { banco.registroDao().inserir(it) }
        banco.perfilDao().salvar(Perfil(id = 1, saldoMinutos = 45))
    }

    private fun executar(onSucesso: () -> Unit = {}, acao: suspend () -> Unit): Job =
        CoroutineScope(Dispatchers.IO).launch(
            context = checkNotNull(viewModelScope.coroutineContext[Job])
        ) {
            mutex.withLock {
                withContext(Dispatchers.Main) { salvando = true; erro = null }
                try {
                    val dados = banco.withTransaction {
                        // O perfil marca a primeira instalação; apagar todos os hábitos
                        // não recria exemplos na próxima abertura.
                        if (banco.perfilDao().buscar() == null) gravarExemplos()
                        acao()
                        lerDados()
                    }
                    withContext(Dispatchers.Main) {
                        habitos.clear(); habitos.addAll(dados.habitos)
                        recompensas.clear(); recompensas.addAll(dados.recompensas)
                        registros.clear(); registros.addAll(dados.registros)
                        perfil = dados.perfil
                        versaoDados++
                        onSucesso()
                    }
                } catch (cancelamento: CancellationException) {
                    throw cancelamento
                } catch (falha: Exception) {
                    Log.e("HabitTracker", "Falha na operação de banco", falha)
                    withContext(Dispatchers.Main) {
                        erro = falha.message ?: "Não foi possível acessar os dados."
                    }
                } finally {
                    withContext(NonCancellable + Dispatchers.Main) {
                        salvando = false; carregando = false
                    }
                }
            }
        }

    fun recarregar(): Job = executar { }
    suspend fun carregarDados() { recarregar().join() }
    fun limparErro() { erro = null }

    suspend fun buscarHabitoNoBanco(id: Int): Habito? = coroutineScope {
        var item: Habito? = null
        CoroutineScope(Dispatchers.IO).launch(checkNotNull(coroutineContext[Job])) {
            item = banco.habitoDao().buscarPorId(id)
        }.join()
        item
    }
    suspend fun buscarRecompensaNoBanco(id: Int): Recompensa? = coroutineScope {
        var item: Recompensa? = null
        CoroutineScope(Dispatchers.IO).launch(checkNotNull(coroutineContext[Job])) {
            item = banco.recompensaDao().buscarPorId(id)
        }.join()
        item
    }

    fun habitoPorId(id: Int): Habito? = habitos.firstOrNull { it.id == id }
    fun recompensaPorId(id: Int): Recompensa? = recompensas.firstOrNull { it.id == id }
    fun diaDaSemana(dia: Int): Int = dia % 7
    fun habitosDeHoje(): List<Habito> = habitos.filter { diaDaSemana(diaHoje) in it.diasSemana }

    private fun validar(habito: Habito) {
        require(habito.nome.isNotBlank()) { "Informe o nome do hábito." }
        require(habito.diasSemana.isNotEmpty() && habito.diasSemana.all { it in 0..6 }) {
            "Escolha pelo menos um dia válido."
        }
        require(habito.minutosRecompensa > 0) { "Os minutos devem ser positivos." }
    }

    fun adicionarHabito(nome: String, categoria: Categoria, tipo: TipoHabito = TipoHabito.FAZER,
        periodo: Periodo = Periodo.MANHA, diasSemana: List<Int> = (0..6).toList(),
        minutosRecompensa: Int = 10, onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        val habito = Habito(nome = nome.trim(), tipo = tipo, categoria = categoria,
            periodo = periodo, diasSemana = diasSemana.distinct().sorted(),
            minutosRecompensa = minutosRecompensa,
            ultimaRecaida = if (tipo == TipoHabito.EVITAR) diaHoje else 0)
        validar(habito)
        banco.habitoDao().inserir(habito)
    }
    fun atualizarHabito(habito: Habito, onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        validar(habito)
        check(banco.habitoDao().atualizar(habito.copy(nome = habito.nome.trim())) == 1) {
            "O hábito não existe mais."
        }
    }
    fun removerHabito(id: Int, onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        val habito = banco.habitoDao().buscarPorId(id) ?: error("Hábito não encontrado.")
        banco.habitoDao().remover(habito)
    }
    fun concluidoEm(habitoId: Int, dia: Int): Boolean = registros.any {
        it.habitoId == habitoId && it.dia == dia && it.tipo == TipoRegistro.CONCLUSAO
    }
    fun concluidoHoje(habitoId: Int): Boolean = concluidoEm(habitoId, diaHoje)

    private suspend fun registrar(dia: Int, tipo: TipoRegistro, habito: Habito, minutos: Int = 0) {
        banco.registroDao().inserir(Registro(dia = dia, tipo = tipo, habitoId = habito.id,
            nomeHabito = habito.nome, categoria = habito.categoria, minutos = minutos))
    }
    fun alternarConclusaoHoje(id: Int): Job = executar {
        val habito = banco.habitoDao().buscarPorId(id) ?: error("Hábito não encontrado.")
        val perfil = checkNotNull(banco.perfilDao().buscar())
        val anterior = banco.registroDao().buscarConclusao(id, diaHoje)
        if (anterior == null) {
            registrar(diaHoje, TipoRegistro.CONCLUSAO, habito, habito.minutosRecompensa)
            banco.perfilDao().salvar(perfil.copy(saldoMinutos = perfil.saldoMinutos + habito.minutosRecompensa))
        } else {
            banco.registroDao().remover(anterior)
            banco.perfilDao().salvar(perfil.copy(saldoMinutos = (perfil.saldoMinutos - anterior.minutos).coerceAtLeast(0)))
        }
    }
    fun registrarRecaida(id: Int): Job = executar {
        val habito = banco.habitoDao().buscarPorId(id) ?: error("Hábito não encontrado.")
        require(habito.tipo == TipoHabito.EVITAR) { "Recaída se aplica a hábitos de evitar." }
        val anterior = banco.registroDao().buscarConclusao(id, diaHoje)
        if (anterior != null) {
            val perfil = checkNotNull(banco.perfilDao().buscar())
            banco.registroDao().remover(anterior)
            banco.perfilDao().salvar(perfil.copy(saldoMinutos = (perfil.saldoMinutos - anterior.minutos).coerceAtLeast(0)))
        }
        registrar(diaHoje, TipoRegistro.RECAIDA, habito)
        banco.habitoDao().atualizar(habito.copy(ultimaRecaida = diaHoje))
    }

    fun diasSem(habito: Habito): Int = diaHoje - habito.ultimaRecaida

    /** Dias seguidos concluindo o hábito (hoje conta se já foi feito; senão conta a partir de ontem). */
    fun ofensivaAtual(habitoId: Int): Int {
        var dia = if (concluidoEm(habitoId, diaHoje)) diaHoje else diaHoje - 1
        var total = 0
        while (dia >= 1 && concluidoEm(habitoId, dia)) {
            total++
            dia--
        }
        return total
    }

    /** O número mostrado no card: "dias sem" para EVITAR, ofensiva para FAZER. */
    fun sequenciaDe(habito: Habito): Int =
        if (habito.tipo == TipoHabito.EVITAR) diasSem(habito) else ofensivaAtual(habito.id)

    fun melhorOfensiva(habitoId: Int): Int {
        var melhor = 0
        var atual = 0
        for (dia in 1..diaHoje) {
            if (concluidoEm(habitoId, dia)) {
                atual++
                if (atual > melhor) melhor = atual
            } else {
                atual = 0
            }
        }
        return melhor
    }

    fun totalConclusoes(habitoId: Int): Int =
        registros.count { it.habitoId == habitoId && it.tipo == TipoRegistro.CONCLUSAO }

    /** Fração (0f a 1f) dos últimos 7 dias em que o hábito foi concluído. */
    fun taxaUltimos7Dias(habitoId: Int): Float {
        val concluidos = ((diaHoje - 6)..diaHoje).count { concluidoEm(habitoId, it) }
        return concluidos / 7f
    }

    fun minutosFocoDoHabito(habitoId: Int): Int =
        registros.filter { it.habitoId == habitoId && it.tipo == TipoRegistro.FOCO }.sumOf { it.minutos }

    // ------------------------------------------------------------------
    // Cálculos gerais (calendário, estatísticas, avatar)
    // ------------------------------------------------------------------

    /** Atividades de um dia: conclusões e sessões de foco (recaídas não contam). */
    fun atividadesNoDia(dia: Int): List<Registro> =
        registros.filter { it.dia == dia && it.tipo != TipoRegistro.RECAIDA && it.tipo != TipoRegistro.RESGATE }

    fun contagemNoDia(dia: Int): Int = atividadesNoDia(dia).size

    /** Dias seguidos com pelo menos 1 atividade (hoje conta se já houve atividade). */
    fun ofensivaGeral(): Int {
        var dia = if (contagemNoDia(diaHoje) > 0) diaHoje else diaHoje - 1
        var total = 0
        while (dia >= 1 && contagemNoDia(dia) > 0) {
            total++
            dia--
        }
        return total
    }

    fun totalConclusoesGeral(): Int = registros.count { it.tipo == TipoRegistro.CONCLUSAO }

    fun totalSessoesFoco(): Int = registros.count { it.tipo == TipoRegistro.FOCO }

    fun minutosFocoTotal(): Int =
        registros.filter { it.tipo == TipoRegistro.FOCO }.sumOf { it.minutos }

    /** Pontos de um atributo, calculados a partir do histórico. */
    fun pontosDe(atributo: Atributo): Int =
        (perfil.pontosBase(atributo) + registros.sumOf { registro ->
            when (registro.tipo) {
                TipoRegistro.CONCLUSAO ->
                    if (registro.categoria.atributo == atributo) PONTOS_POR_CONCLUSAO else 0
                TipoRegistro.FOCO ->
                    if (atributo == Atributo.FOCO) registro.minutos / MINUTOS_FOCO_POR_PONTO else 0
                TipoRegistro.RECAIDA, TipoRegistro.RESGATE -> 0
            }
        }).coerceAtLeast(0)

    fun pontosTotais(): Int = Atributo.entries.sumOf { pontosDe(it) }

    fun nivel(): Int = 1 + pontosTotais() / PONTOS_POR_NIVEL

    /** Progresso (0f a 1f) até o próximo nível. */
    fun progressoNivel(): Float =
        (pontosTotais() % PONTOS_POR_NIVEL) / PONTOS_POR_NIVEL.toFloat()


    fun registrarSessaoFoco(id: Int, minutos: Int, onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        require(minutos > 0) { "Informe uma duração positiva." }
        val habito = banco.habitoDao().buscarPorId(id) ?: error("Hábito não encontrado.")
        val perfil = checkNotNull(banco.perfilDao().buscar())
        registrar(diaHoje, TipoRegistro.FOCO, habito, minutos)
        banco.perfilDao().salvar(perfil.copy(saldoMinutos = perfil.saldoMinutos + minutos / MINUTOS_FOCO_POR_MINUTO_LAZER))
    }
    fun adicionarRecompensa(nome: String, custoMinutos: Int, emoji: String = "🎁",
        onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        require(nome.isNotBlank() && custoMinutos > 0) { "Informe nome e custo positivo." }
        banco.recompensaDao().inserir(Recompensa(nome = nome.trim(), custoMinutos = custoMinutos, emoji = emoji))
    }
    fun atualizarRecompensa(item: Recompensa, onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        require(item.nome.isNotBlank() && item.custoMinutos > 0) { "Informe nome e custo positivo." }
        check(banco.recompensaDao().atualizar(item.copy(nome = item.nome.trim())) == 1) {
            "A recompensa não existe mais."
        }
    }
    fun removerRecompensa(id: Int, onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        banco.recompensaDao().remover(banco.recompensaDao().buscarPorId(id) ?: error("Recompensa não encontrada."))
    }
    fun resgatarRecompensa(id: Int, onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        val recompensa = banco.recompensaDao().buscarPorId(id) ?: error("Recompensa não encontrada.")
        val perfil = checkNotNull(banco.perfilDao().buscar())
        require(perfil.saldoMinutos >= recompensa.custoMinutos) { "Saldo insuficiente." }
        banco.perfilDao().salvar(perfil.copy(saldoMinutos = perfil.saldoMinutos - recompensa.custoMinutos))
        banco.recompensaDao().atualizar(recompensa.copy(vezesResgatada = recompensa.vezesResgatada + 1))
        banco.registroDao().inserir(Registro(dia = diaHoje, tipo = TipoRegistro.RESGATE, habitoId = 0,
            nomeHabito = recompensa.nome, categoria = Categoria.DISCIPLINA,
            minutos = recompensa.custoMinutos, recompensaId = id))
    }
    fun minutosFaltando(recompensa: Recompensa): Int = (recompensa.custoMinutos - saldoMinutos).coerceAtLeast(0)
    fun alterarNotificacoes(ativas: Boolean): Job = executar {
        banco.perfilDao().salvar(checkNotNull(banco.perfilDao().buscar()).copy(notificacoesAtivas = ativas))
    }
    fun restaurarDadosDeExemplo(onSucesso: () -> Unit = {}): Job = executar(onSucesso) { gravarExemplos() }
}
