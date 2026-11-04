package com.example.cupbank.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cupbank.data.Cartao
import com.example.cupbank.data.TransacaoCartao
import com.example.cupbank.data.repository.MovimentoRepository
import com.example.cupbank.data.repository.TransacaoCartaoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "CartaoViewModel"

enum class ResultadoFatura(val mensagem: String) {
    SEM_FATURA("Não há fatura para pagar"),
    SALDO_INSUFICIENTE("Saldo insuficiente para pagar a fatura"),
    PAGA("Fatura paga com sucesso!")
}

data class CartaoUiState(
    val transacoes: List<TransacaoCartao> = emptyList(),
    val saldo: Double = 0.0,
    val transacaoDetalhe: TransacaoCartao? = null,
    val carregandoDetalhe: Boolean = false
) {
    val cartao: Cartao
        get() = Cartao(
            numero = "9010",
            nomeTitular = "LUCAS SOUZA",
            faturaAtual = transacoes.filter { !it.pago }.sumOf { it.valor },
            dataVencimento = "10 de out.",
            limiteTotal = 9000.00,
            limiteDisponivel = 4500.00
        )
}

class CartaoViewModel(
    private val transacaoRepository: TransacaoCartaoRepository,
    private val movimentoRepository: MovimentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartaoUiState())
    val uiState: StateFlow<CartaoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(transacaoRepository.buscarTodos(), movimentoRepository.saldo()) { transacoes, saldo ->
                transacoes to saldo
            }
                .catch { Log.e(TAG, "Erro ao carregar transações", it) }
                .collect { (transacoes, saldo) ->
                    _uiState.update { it.copy(transacoes = transacoes, saldo = saldo) }
                }
        }
    }

    fun carregarTransacao(id: Int) {
        _uiState.update { it.copy(transacaoDetalhe = null, carregandoDetalhe = true) }
        viewModelScope.launch {
            val encontrada = try {
                transacaoRepository.buscarPorId(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar transação $id", e)
                null
            }
            _uiState.update { it.copy(transacaoDetalhe = encontrada, carregandoDetalhe = false) }
        }
    }

    fun adicionar(descricao: String, valor: Double, data: String, categoria: String) {
        executar(TAG) {
            transacaoRepository.inserir(
                TransacaoCartao(descricao = descricao, valor = valor, data = data, categoria = categoria)
            )
        }
    }

    fun editar(id: Int, descricao: String, valor: Double, data: String, categoria: String): String {
        val transacao = _uiState.value.transacoes.firstOrNull { it.id == id } ?: return "Transação não encontrada"
        if (transacao.pago) return "Transação já foi paga"
        executar(TAG) {
            transacaoRepository.atualizar(
                transacao.copy(descricao = descricao, valor = valor, data = data, categoria = categoria)
            )
        }
        return "Transação atualizada"
    }

    fun remover(id: Int): String {
        val transacao = _uiState.value.transacoes.firstOrNull { it.id == id } ?: return "Transação não encontrada"
        if (transacao.pago) return "Transação já foi paga"
        executar(TAG) { transacaoRepository.deletar(transacao) }
        return "Transação removida"
    }

    fun pagarFatura(): ResultadoFatura {
        val estado = _uiState.value
        val valor = estado.cartao.faturaAtual
        if (valor <= 0) return ResultadoFatura.SEM_FATURA
        if (valor > estado.saldo) return ResultadoFatura.SALDO_INSUFICIENTE
        executar(TAG) {
            movimentoRepository.registrar("Fatura do cartão", "Cartão final ${estado.cartao.numero}", -valor)
            transacaoRepository.quitarPendentes()
        }
        return ResultadoFatura.PAGA
    }
}
