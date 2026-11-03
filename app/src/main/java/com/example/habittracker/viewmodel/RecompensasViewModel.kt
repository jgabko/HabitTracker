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

class RecompensasViewModel(
    private val repository: RecompensaRepository,
    private val perfilRepository: PerfilRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecompensasUiState())
    val uiState: StateFlow<RecompensasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.buscarTodos().collect { lista ->
                _uiState.update { it.copy(recompensas = lista) }
            }
        }
        viewModelScope.launch {
            perfilRepository.buscarTodos().collect { lista ->
                _uiState.update { it.copy(perfil = lista.firstOrNull() ?: Perfil(id = 1)) }
            }
        }
    }

    fun onNomeChange(nome: String) { _uiState.update { it.copy(nome = nome, salvo = false) } }
    fun onCustoChange(custo: String) {
        _uiState.update { it.copy(custoTexto = custo.filter(Char::isDigit).take(4), salvo = false) }
    }
    fun onAbaChange(aba: Int) { _uiState.update { it.copy(aba = aba) } }
    fun onNovo() {
        _uiState.update { it.copy(nome = "", custoTexto = "", recompensaEmEdicao = null,
            tentouSalvar = false, salvo = false, mensagem = "") }
    }
    fun onEditar(item: Recompensa) {
        if (_uiState.value.recompensaEmEdicao?.id == item.id) return
        _uiState.update { it.copy(nome = item.nome, custoTexto = item.custoMinutos.toString(),
            recompensaEmEdicao = item, tentouSalvar = false, salvo = false, mensagem = "") }
    }
    fun onSalvar() {
        _uiState.update { it.copy(tentouSalvar = true) }
        val state = _uiState.value
        if (!state.valido || state.salvando) return
        val custo = state.custoTexto.toInt()
        val anterior = state.recompensaEmEdicao
        val item = anterior?.copy(nome = state.nome.trim(), custoMinutos = custo)
            ?: Recompensa(nome = state.nome.trim(), custoMinutos = custo)
        viewModelScope.launch {
            _uiState.update { it.copy(salvando = true, erro = null) }
            try {
                if (anterior == null) {
                    repository.inserir(item)
                    onNovo()
                } else {
                    repository.atualizar(item)
                    _uiState.update { it.copy(salvo = true) }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(erro = "Não foi possível salvar a recompensa.") }
            } finally {
                _uiState.update { it.copy(salvando = false) }
            }
        }
    }

    fun removerRecompensa(id: Int, onSucesso: () -> Unit = {}) = viewModelScope.launch {
        _uiState.update { it.copy(salvando = true, erro = null) }
        try {
            val item = checkNotNull(repository.buscarPorId(id))
            repository.remover(item)
            onSucesso()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            _uiState.update { it.copy(erro = e.message ?: "Não foi possível salvar.") }
        } finally {
            _uiState.update { it.copy(salvando = false) }
        }
    }

    fun resgatarRecompensa(id: Int, onSucesso: () -> Unit = {}) = viewModelScope.launch {
        _uiState.update { it.copy(salvando = true, erro = null) }
        try {
            repository.resgatar(id, 15)
            onSucesso()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            _uiState.update { it.copy(erro = e.message ?: "Não foi possível salvar.") }
        } finally {
            _uiState.update { it.copy(salvando = false) }
        }
    }

    fun onResgatar(item: Recompensa) {
        resgatarRecompensa(item.id) {
            _uiState.update { it.copy(mensagem = "✓ Resgatado! -${item.custoMinutos} min do seu saldo.") }
        }
    }
    fun limparErro() { _uiState.update { it.copy(erro = null) } }
}
