package com.example.cupbank.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cupbank.data.Ativo
import com.example.cupbank.data.Ordem
import com.example.cupbank.data.TipoAtivo
import com.example.cupbank.data.repository.AtivoRepository
import com.example.cupbank.data.repository.OrdemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "InvestimentosViewModel"
private const val PATRIMONIO_BASE = 8640.75

data class InvestimentosUiState(
    val ativos: List<Ativo> = emptyList(),
    val ordens: List<Ordem> = emptyList(),
    val carregado: Boolean = false,
    val tickerSelecionado: String = "",
    val quantidade: Int = 1,
    val mostrarNovoAtivo: Boolean = false
) {
    val patrimonio: Double get() = PATRIMONIO_BASE + ordens.sumOf { it.total }

    val variacaoMes: Double get() = 286.40

    val ativoSelecionado: Ativo? get() = ativoPorTicker(tickerSelecionado)

    fun ativoPorTicker(ticker: String): Ativo? = ativos.firstOrNull { it.ticker == ticker }
}

class InvestimentosViewModel(
    private val ativoRepository: AtivoRepository,
    private val ordemRepository: OrdemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestimentosUiState())
    val uiState: StateFlow<InvestimentosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(ativoRepository.buscarTodos(), ordemRepository.buscarTodos()) { ativos, ordens ->
                ativos to ordens
            }
                .catch { Log.e(TAG, "Erro ao carregar do banco", it) }
                .collect { (ativos, ordens) -> aplicarDoBanco(ativos, ordens) }
        }
    }

    private fun aplicarDoBanco(ativos: List<Ativo>, ordens: List<Ordem>) {
        _uiState.update { atual ->
            val perdeuSelecao = ativos.none { it.ticker == atual.tickerSelecionado }
            atual.copy(
                ativos = ativos,
                ordens = ordens,
                carregado = true,
                tickerSelecionado = if (perdeuSelecao) ativos.firstOrNull()?.ticker ?: "" else atual.tickerSelecionado,
                quantidade = if (perdeuSelecao) 1 else atual.quantidade
            )
        }
    }

    fun selecionar(ticker: String) {
        _uiState.update { if (ticker != it.tickerSelecionado) it.copy(tickerSelecionado = ticker, quantidade = 1) else it }
    }

    fun aumentarQuantidade() {
        _uiState.update { if (it.quantidade < 999) it.copy(quantidade = it.quantidade + 1) else it }
    }

    fun diminuirQuantidade() {
        _uiState.update { if (it.quantidade > 1) it.copy(quantidade = it.quantidade - 1) else it }
    }

    fun abrirNovoAtivo() {
        _uiState.update { it.copy(mostrarNovoAtivo = true) }
    }

    fun fecharNovoAtivo() {
        _uiState.update { it.copy(mostrarNovoAtivo = false) }
    }

    fun adicionarAtivo(ticker: String, nome: String, preco: Double?, tipo: TipoAtivo): String? {
        val codigo = ticker.trim().uppercase()
        return when {
            codigo.isEmpty() -> "Informe o código do ativo."
            nome.isBlank() -> "Informe o nome do ativo."
            preco == null || preco <= 0.0 -> "Informe um preço maior que zero."
            _uiState.value.ativos.any { it.ticker == codigo } -> "Esse ativo já está na lista."
            else -> {
                executar(TAG) {
                    ativoRepository.inserir(
                        Ativo(ticker = codigo, nome = nome.trim(), preco = preco, variacao = 0.0, tipo = tipo)
                    )
                }
                null
            }
        }
    }

    fun editarAtivo(ativo: Ativo, nome: String, preco: Double?, tipo: TipoAtivo): String? {
        return when {
            nome.isBlank() -> "Informe o nome do ativo."
            preco == null || preco <= 0.0 -> "Informe um preço maior que zero."
            else -> {
                executar(TAG) { ativoRepository.atualizar(ativo.copy(nome = nome.trim(), preco = preco, tipo = tipo)) }
                null
            }
        }
    }

    fun removerAtivo(ativo: Ativo) {
        executar(TAG) {
            ordemRepository.deletarPorTicker(ativo.ticker)
            ativoRepository.deletar(ativo)
        }
    }

    fun confirmarCompra(ativo: Ativo, quantidade: Int) {
        executar(TAG) {
            ordemRepository.inserir(Ordem(ticker = ativo.ticker, quantidade = quantidade, precoUnitario = ativo.preco))
        }
    }

    fun cancelarOrdem(ordem: Ordem) {
        executar(TAG) { ordemRepository.deletar(ordem) }
    }
}
