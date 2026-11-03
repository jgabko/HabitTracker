package com.example.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.*
import com.example.habittracker.data.repository.DadosRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class HabitosViewModel(private val repository: DadosRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HabitosUiState())
    val uiState: StateFlow<HabitosUiState> = _uiState.asStateFlow()

    private var observacao: Job? = null
    init { observar() }
    private fun observar() {
        observacao?.cancel()
        observacao = viewModelScope.launch {
            try {
                repository.inicializar()
                repository.observarDados().collect { dados ->
                    _uiState.update { it.copy(dados = AppUiState(dados.habitos,
                        dados.recompensas, dados.registros, dados.perfil), carregando = false) }
                }
            } catch (cancelamento: CancellationException) {
                throw cancelamento
            } catch (falha: Exception) {
                _uiState.update { it.copy(carregando = false, erro = falha.message ?: "Não foi possível carregar os dados.") }
            }
        }
    }

    fun tentarNovamente() {
        _uiState.update { it.copy(carregando = true, erro = null) }
        observar()
    }
    fun limparErro() { _uiState.update { it.copy(erro = null) } }

    private fun executar(onSucesso: () -> Unit = {}, acao: suspend () -> Unit): Job =
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null) }
            try {
                repository.transacao(acao)
                onSucesso()
            } catch (cancelamento: CancellationException) {
                throw cancelamento
            } catch (falha: Exception) {
                _uiState.update { it.copy(erro = falha.message ?: "Não foi possível salvar os dados.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }

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
            ultimaRecaida = if (tipo == TipoHabito.EVITAR) _uiState.value.dados.diaHoje else 0)
        validar(habito)
        repository.habitos.inserir(habito)
    }
    fun atualizarHabito(habito: Habito, onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        validar(habito)
        check(repository.habitos.atualizar(habito.copy(nome = habito.nome.trim())) == 1) {
            "O hábito não existe mais."
        }
    }
    fun removerHabito(id: Int, onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        val habito = repository.habitos.buscarPorId(id) ?: error("Hábito não encontrado.")
        repository.habitos.remover(habito)
    }
    private suspend fun registrar(dia: Int, tipo: TipoRegistro, habito: Habito, minutos: Int = 0) {
        repository.registros.inserir(Registro(dia = dia, tipo = tipo, habitoId = habito.id,
            nomeHabito = habito.nome, categoria = habito.categoria, minutos = minutos))
    }
    fun alternarConclusaoHoje(id: Int): Job = executar {
        val habito = repository.habitos.buscarPorId(id) ?: error("Hábito não encontrado.")
        val perfil = checkNotNull(repository.perfil.buscar())
        val anterior = repository.registros.buscarConclusao(id, _uiState.value.dados.diaHoje)
        if (anterior == null) {
            registrar(_uiState.value.dados.diaHoje, TipoRegistro.CONCLUSAO, habito, habito.minutosRecompensa)
            repository.perfil.salvar(perfil.copy(saldoMinutos = perfil.saldoMinutos + habito.minutosRecompensa))
        } else {
            repository.registros.remover(anterior)
            repository.perfil.salvar(perfil.copy(saldoMinutos = (perfil.saldoMinutos - anterior.minutos).coerceAtLeast(0)))
        }
    }
    fun registrarRecaida(id: Int): Job = executar {
        val habito = repository.habitos.buscarPorId(id) ?: error("Hábito não encontrado.")
        require(habito.tipo == TipoHabito.EVITAR) { "Recaída se aplica a hábitos de evitar." }
        val anterior = repository.registros.buscarConclusao(id, _uiState.value.dados.diaHoje)
        if (anterior != null) {
            val perfil = checkNotNull(repository.perfil.buscar())
            repository.registros.remover(anterior)
            repository.perfil.salvar(perfil.copy(saldoMinutos = (perfil.saldoMinutos - anterior.minutos).coerceAtLeast(0)))
        }
        registrar(_uiState.value.dados.diaHoje, TipoRegistro.RECAIDA, habito)
        repository.habitos.atualizar(habito.copy(ultimaRecaida = _uiState.value.dados.diaHoje))
    }

    fun registrarSessaoFoco(id: Int, minutos: Int, onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        require(minutos > 0) { "Informe uma duração positiva." }
        val habito = repository.habitos.buscarPorId(id) ?: error("Hábito não encontrado.")
        val perfil = checkNotNull(repository.perfil.buscar())
        registrar(_uiState.value.dados.diaHoje, TipoRegistro.FOCO, habito, minutos)
        repository.perfil.salvar(perfil.copy(saldoMinutos = perfil.saldoMinutos + minutos / AppUiState.MINUTOS_FOCO_POR_MINUTO_LAZER))
    }
    fun alterarNotificacoes(ativas: Boolean): Job = executar {
        repository.perfil.salvar(checkNotNull(repository.perfil.buscar()).copy(notificacoesAtivas = ativas))
    }
    fun restaurarDadosDeExemplo(onSucesso: () -> Unit = {}): Job = executar(onSucesso) { repository.restaurarExemplos() }


    private fun alterarFormulario(destino: String, mudar: (FormularioHabito) -> FormularioHabito) {
        _uiState.update { estado -> when (destino) {
            "edicao" -> estado.copy(edicao = mudar(estado.edicao).copy(salvo = false))
            "rapido" -> estado.copy(rapido = mudar(estado.rapido).copy(salvo = false))
            else -> estado.copy(formulario = mudar(estado.formulario).copy(salvo = false))
        } }
    }
    fun onNomeChange(nome: String, destino: String = "novo") = alterarFormulario(destino) { it.copy(nome = nome, tentouSalvar = false) }
    fun onCategoriaChange(categoria: Categoria, destino: String = "novo") = alterarFormulario(destino) { it.copy(categoria = categoria) }
    fun onTipoChange(tipo: TipoHabito) = alterarFormulario("novo") { it.copy(tipo = tipo) }
    fun onPeriodoChange(periodo: Periodo, destino: String = "novo") = alterarFormulario(destino) { it.copy(periodo = periodo) }
    fun onMinutosChange(minutos: Int) = alterarFormulario("edicao") { it.copy(minutos = minutos) }
    fun onDiaAlternar(dia: Int, destino: String = "novo") = alterarFormulario(destino) {
        it.copy(dias = if (dia in it.dias) it.dias - dia else (it.dias + dia).sorted())
    }
    fun onCancelarNovo() { _uiState.update { it.copy(formulario = FormularioHabito()) } }
    fun onEditar(habito: Habito) {
        if (_uiState.value.edicao.item?.id == habito.id) return
        _uiState.update { it.copy(edicao = FormularioHabito(item = habito, nome = habito.nome,
            tipo = habito.tipo, categoria = habito.categoria, dias = habito.diasSemana,
            periodo = habito.periodo, minutos = habito.minutosRecompensa)) }
    }
    fun onSalvar(destino: String = "novo", onSucesso: () -> Unit = {}) {
        alterarFormulario(destino) { it.copy(tentouSalvar = true) }
        val estado = _uiState.value
        val form = when (destino) { "edicao" -> estado.edicao; "rapido" -> estado.rapido; else -> estado.formulario }
        if (!form.valido || estado.salvando) return
        val item = form.item
        if (item == null) {
            adicionarHabito(form.nome, form.categoria, form.tipo, form.periodo, form.dias, form.minutos) {
                _uiState.update { if (destino == "rapido") it.copy(rapido = FormularioHabito(categoria = form.categoria))
                    else it.copy(formulario = FormularioHabito()) }
                onSucesso()
            }
        } else {
            val emoji = if (item.emoji == item.categoria.emoji) form.categoria.emoji else item.emoji
            atualizarHabito(item.copy(nome = form.nome, categoria = form.categoria, emoji = emoji,
                diasSemana = form.dias, periodo = form.periodo, minutosRecompensa = form.minutos)) {
                _uiState.update { it.copy(edicao = it.edicao.copy(salvo = true)) }
                onSucesso()
            }
        }
    }

    fun onAbrirFoco(id: Int) {
        if (_uiState.value.foco.rotaHabitoId == id) return
        _uiState.update { it.copy(foco = FocoUiState(rotaHabitoId = id, habitoId = id.takeIf { valor -> valor >= 0 })) }
    }
    fun onSelecionarFoco(id: Int) { _uiState.update { it.copy(foco = it.foco.copy(habitoId = id)) } }
    fun onDuracaoChange(minutos: Int) { _uiState.update { it.copy(foco = it.foco.copy(duracao = minutos)) } }
    fun onIniciarFoco() { _uiState.update { it.copy(foco = it.foco.copy(emAndamento = true)) } }
    fun onResetarFoco() { _uiState.update { it.copy(foco = it.foco.copy(emAndamento = false, concluida = false, decorridos = 0)) } }
    fun onAvancarFoco() {
        val estado = _uiState.value
        if (estado.salvando || !estado.foco.emAndamento) return
        val foco = estado.foco.copy(decorridos = (estado.foco.decorridos + 5).coerceAtMost(estado.foco.duracao))
        _uiState.update { it.copy(foco = foco) }
        if (foco.decorridos >= foco.duracao) {
            val habito = estado.dados.habitos.firstOrNull { it.id == foco.habitoId } ?: estado.dados.habitos.firstOrNull() ?: return
            registrarSessaoFoco(habito.id, foco.duracao) {
                _uiState.update { it.copy(foco = it.foco.copy(emAndamento = false, concluida = true)) }
            }
        }
    }

    fun onConfirmarReset() { _uiState.update { it.copy(confirmandoReset = true, resetFeito = false) } }
    fun onCancelarReset() { _uiState.update { it.copy(confirmandoReset = false) } }
    fun onResetarDados() {
        onCancelarReset()
        restaurarDadosDeExemplo { _uiState.update { it.copy(resetFeito = true) } }
    }
}
