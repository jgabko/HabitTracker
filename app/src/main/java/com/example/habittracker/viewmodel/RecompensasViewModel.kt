package com.example.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habittracker.data.*
import com.example.habittracker.data.repository.DadosRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class RecompensasViewModel(private val repository: DadosRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RecompensasUiState())
    val uiState: StateFlow<RecompensasUiState> = _uiState.asStateFlow()

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

    fun adicionarRecompensa(nome: String, custoMinutos: Int, emoji: String = "🎁",
        onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        require(nome.isNotBlank() && custoMinutos > 0) { "Informe nome e custo positivo." }
        repository.recompensas.inserir(Recompensa(nome = nome.trim(), custoMinutos = custoMinutos, emoji = emoji))
    }
    fun atualizarRecompensa(item: Recompensa, onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        require(item.nome.isNotBlank() && item.custoMinutos > 0) { "Informe nome e custo positivo." }
        check(repository.recompensas.atualizar(item.copy(nome = item.nome.trim())) == 1) {
            "A recompensa não existe mais."
        }
    }
    fun removerRecompensa(id: Int, onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        repository.recompensas.remover(repository.recompensas.buscarPorId(id) ?: error("Recompensa não encontrada."))
    }
    fun resgatarRecompensa(id: Int, onSucesso: () -> Unit = {}): Job = executar(onSucesso) {
        val recompensa = repository.recompensas.buscarPorId(id) ?: error("Recompensa não encontrada.")
        val perfil = checkNotNull(repository.perfil.buscar())
        require(perfil.saldoMinutos >= recompensa.custoMinutos) { "Saldo insuficiente." }
        repository.perfil.salvar(perfil.copy(saldoMinutos = perfil.saldoMinutos - recompensa.custoMinutos))
        repository.recompensas.atualizar(recompensa.copy(vezesResgatada = recompensa.vezesResgatada + 1))
        repository.registros.inserir(Registro(dia = _uiState.value.dados.diaHoje, tipo = TipoRegistro.RESGATE, habitoId = 0,
            nomeHabito = recompensa.nome, categoria = Categoria.DISCIPLINA,
            minutos = recompensa.custoMinutos, recompensaId = id))
    }


    private fun alterarFormulario(editando: Boolean, mudar: (FormularioRecompensa) -> FormularioRecompensa) {
        _uiState.update { if (editando) it.copy(edicao = mudar(it.edicao).copy(salvo = false))
            else it.copy(formulario = mudar(it.formulario).copy(salvo = false)) }
    }
    fun onNomeChange(nome: String, editando: Boolean = false) = alterarFormulario(editando) { it.copy(nome = nome) }
    fun onCustoChange(custo: String, editando: Boolean = false) = alterarFormulario(editando) {
        it.copy(custoTexto = custo.filter(Char::isDigit).take(4))
    }
    fun onAbaChange(aba: Int) { _uiState.update { it.copy(aba = aba) } }
    fun onEditar(recompensa: Recompensa) {
        if (_uiState.value.edicao.item?.id == recompensa.id) return
        _uiState.update { it.copy(edicao = FormularioRecompensa(item = recompensa, nome = recompensa.nome,
            custoTexto = recompensa.custoMinutos.toString()), mensagem = "") }
    }
    fun onSalvar(editando: Boolean = false) {
        alterarFormulario(editando) { it.copy(tentouSalvar = true) }
        val estado = _uiState.value
        val form = if (editando) estado.edicao else estado.formulario
        if (!form.valido || estado.salvando) return
        val item = form.item
        if (item == null) adicionarRecompensa(form.nome, form.custo) {
            _uiState.update { it.copy(formulario = FormularioRecompensa()) }
        } else atualizarRecompensa(item.copy(nome = form.nome, custoMinutos = form.custo)) {
            _uiState.update { it.copy(edicao = it.edicao.copy(salvo = true)) }
        }
    }
    fun onResgatar(recompensa: Recompensa) {
        resgatarRecompensa(recompensa.id) {
            _uiState.update { it.copy(mensagem = "✓ Resgatado! -${recompensa.custoMinutos} min do seu saldo.") }
        }
    }
}
