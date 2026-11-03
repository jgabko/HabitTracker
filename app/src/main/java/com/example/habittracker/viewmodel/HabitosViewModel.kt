package com.example.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.*
import com.example.habittracker.data.repository.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HabitosViewModel(
    private val habitosRepository: HabitoRepository,
    private val recompensasRepository: RecompensaRepository,
    private val registrosRepository: RegistroRepository,
    private val perfilRepository: PerfilRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HabitosUiState())
    val uiState: StateFlow<HabitosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                perfilRepository.inicializar()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(erro = "Não foi possível abrir o banco.") }
            }
            _uiState.update { it.copy(carregando = false) }
        }
        viewModelScope.launch {
            habitosRepository.buscarTodos().collect { lista ->
                _uiState.update { it.copy(habitos = lista) }
            }
        }
        viewModelScope.launch {
            recompensasRepository.buscarTodos().collect { lista ->
                _uiState.update { it.copy(recompensas = lista) }
            }
        }
        viewModelScope.launch {
            registrosRepository.buscarTodos().collect { lista ->
                _uiState.update { it.copy(registros = lista) }
            }
        }
        viewModelScope.launch {
            perfilRepository.buscarTodos().collect { lista ->
                _uiState.update { it.copy(perfil = lista.firstOrNull() ?: Perfil(id = 1)) }
            }
        }
    }

    fun onNomeChange(nome: String) { _uiState.update { it.copy(nome = nome, salvo = false) } }
    fun onTipoChange(tipo: TipoHabito) { _uiState.update { it.copy(tipo = tipo) } }
    fun onCategoriaChange(categoria: Categoria) { _uiState.update { it.copy(categoria = categoria, salvo = false) } }
    fun onPeriodoChange(periodo: Periodo) { _uiState.update { it.copy(periodo = periodo, salvo = false) } }
    fun onMinutosChange(minutos: Int) { _uiState.update { it.copy(minutos = minutos, salvo = false) } }
    fun onDiaAlternar(dia: Int) {
        val dias = _uiState.value.dias.toMutableList()
        if (dia in dias) dias.remove(dia) else dias.add(dia)
        _uiState.update { it.copy(dias = dias.sorted(), salvo = false) }
    }
    fun onNovo() { limparCampos() }
    fun onCancelarNovo() { limparCampos() }
    private fun limparCampos() {
        _uiState.update { it.copy(nome = "", tipo = TipoHabito.FAZER, categoria = Categoria.MENTE,
            dias = (0..6).toList(), periodo = Periodo.MANHA, minutos = 10,
            habitoEmEdicao = null, tentouSalvar = false, salvo = false) }
    }
    fun onEditar(habito: Habito) {
        if (_uiState.value.habitoEmEdicao?.id == habito.id) return
        _uiState.update { it.copy(habitoEmEdicao = habito, nome = habito.nome,
            tipo = habito.tipo, categoria = habito.categoria, dias = habito.diasSemana,
            periodo = habito.periodo, minutos = habito.minutosRecompensa,
            tentouSalvar = false, salvo = false) }
    }

    fun onSalvar(onSucesso: () -> Unit = {}) {
        _uiState.update { it.copy(tentouSalvar = true) }
        val state = _uiState.value
        if (state.nome.isBlank() || state.dias.isEmpty() || state.salvando) return
        val anterior = state.habitoEmEdicao
        val habito = if (anterior == null) {
            Habito(nome = state.nome.trim(), tipo = state.tipo, categoria = state.categoria,
                diasSemana = state.dias, periodo = state.periodo, minutosRecompensa = state.minutos,
                ultimaRecaida = if (state.tipo == TipoHabito.EVITAR) state.diaHoje else 0)
        } else {
            val emoji = if (anterior.emoji == anterior.categoria.emoji) state.categoria.emoji else anterior.emoji
            anterior.copy(nome = state.nome.trim(), categoria = state.categoria, emoji = emoji,
                diasSemana = state.dias, periodo = state.periodo, minutosRecompensa = state.minutos)
        }
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null) }
            try {
                if (anterior == null) {
                    habitosRepository.inserir(habito)
                    limparCampos()
                } else {
                    habitosRepository.atualizar(habito)
                    _uiState.update { it.copy(salvo = true) }
                }
                onSucesso()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(erro = "Não foi possível salvar o hábito.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun onNomeRapidoChange(nome: String) { _uiState.update { it.copy(nomeRapido = nome, erroRapido = false) } }
    fun onCategoriaRapidaChange(categoria: Categoria) { _uiState.update { it.copy(categoriaRapida = categoria) } }
    fun onAdicionarRapido() {
        val state = _uiState.value
        if (state.nomeRapido.isBlank()) {
            _uiState.update { it.copy(erroRapido = true) }
            return
        }
        if (state.salvando) return
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null) }
            try {
                habitosRepository.inserir(Habito(nome = state.nomeRapido.trim(),
                    tipo = TipoHabito.FAZER, categoria = state.categoriaRapida))
                _uiState.update { it.copy(nomeRapido = "", erroRapido = false) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(erro = "Não foi possível adicionar o hábito.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun removerHabito(id: Int, onSucesso: () -> Unit = {}) = viewModelScope.launch {
        _uiState.update { it.copy(salvando = true, erro = null) }
        try {
            val habito = checkNotNull(habitosRepository.buscarPorId(id))
            habitosRepository.remover(habito)
            onSucesso()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            _uiState.update { it.copy(erro = e.message ?: "Não foi possível salvar.") }
        } finally {
            _uiState.update { it.copy(salvando = false) }
        }
    }

    fun alternarConclusaoHoje(id: Int) = viewModelScope.launch {
        _uiState.update { it.copy(salvando = true, erro = null) }
        try {
            habitosRepository.alternarConclusao(id, _uiState.value.diaHoje)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            _uiState.update { it.copy(erro = e.message ?: "Não foi possível salvar.") }
        } finally {
            _uiState.update { it.copy(salvando = false) }
        }
    }

    fun registrarRecaida(id: Int) = viewModelScope.launch {
        _uiState.update { it.copy(salvando = true, erro = null) }
        try {
            habitosRepository.registrarRecaida(id, _uiState.value.diaHoje)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            _uiState.update { it.copy(erro = e.message ?: "Não foi possível salvar.") }
        } finally {
            _uiState.update { it.copy(salvando = false) }
        }
    }

    fun registrarSessaoFoco(id: Int, minutos: Int, onSucesso: () -> Unit = {}) = viewModelScope.launch {
        _uiState.update { it.copy(salvando = true, erro = null) }
        try {
            habitosRepository.registrarFoco(id, minutos, _uiState.value.diaHoje)
            onSucesso()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            _uiState.update { it.copy(erro = e.message ?: "Não foi possível salvar.") }
        } finally {
            _uiState.update { it.copy(salvando = false) }
        }
    }

    fun alterarNotificacoes(ativas: Boolean) = viewModelScope.launch {
        _uiState.update { it.copy(salvando = true, erro = null) }
        try {
            perfilRepository.alterarNotificacoes(ativas)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            _uiState.update { it.copy(erro = e.message ?: "Não foi possível salvar.") }
        } finally {
            _uiState.update { it.copy(salvando = false) }
        }
    }

    fun onAbrirFoco(id: Int) {
        if (_uiState.value.rotaFoco == id) return
        _uiState.update { it.copy(rotaFoco = id, habitoFoco = id,
            duracao = 25, emAndamento = false, decorridos = 0, concluida = false) }
    }
    fun onSelecionarFoco(id: Int) { _uiState.update { it.copy(habitoFoco = id) } }
    fun onDuracaoChange(minutos: Int) { _uiState.update { it.copy(duracao = minutos) } }
    fun onIniciarFoco() { _uiState.update { it.copy(emAndamento = true) } }
    fun onResetarFoco() { _uiState.update { it.copy(emAndamento = false, decorridos = 0, concluida = false) } }
    fun onAvancarFoco() {
        val state = _uiState.value
        if (state.salvando || !state.emAndamento) return
        val minutos = (state.decorridos + 5).coerceAtMost(state.duracao)
        _uiState.update { it.copy(decorridos = minutos) }
        if (minutos == state.duracao) {
            val habito = state.habitos.find { it.id == state.habitoFoco } ?: state.habitos.firstOrNull() ?: return
            registrarSessaoFoco(habito.id, state.duracao) {
                _uiState.update { it.copy(emAndamento = false, concluida = true) }
            }
        }
    }
    fun limparErro() { _uiState.update { it.copy(erro = null) } }
    fun onConfirmarReset() { _uiState.update { it.copy(confirmandoReset = true, resetFeito = false) } }
    fun onCancelarReset() { _uiState.update { it.copy(confirmandoReset = false) } }
    fun onResetarDados() = viewModelScope.launch {
        _uiState.update { it.copy(salvando = true, erro = null) }
        try {
            _uiState.update { it.copy(confirmandoReset = false) }
            perfilRepository.restaurarExemplos()
            _uiState.update { it.copy(resetFeito = true) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            _uiState.update { it.copy(erro = e.message ?: "Não foi possível salvar.") }
        } finally {
            _uiState.update { it.copy(salvando = false) }
        }
    }

}
