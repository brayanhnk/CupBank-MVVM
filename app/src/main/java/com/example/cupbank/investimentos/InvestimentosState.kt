package com.example.cupbank.investimentos

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.cupbank.data.AtivoDao
import com.example.cupbank.data.OrdemDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "InvestimentosState"

class InvestimentosState(
    private val ativoDao: AtivoDao,
    private val ordemDao: OrdemDao
) {

    var ativos by mutableStateOf<List<Ativo>>(emptyList())
        private set

    var ordens by mutableStateOf<List<Ordem>>(emptyList())
        private set

    var carregado by mutableStateOf(false)
        private set

    val patrimonio: Double get() = PATRIMONIO_BASE + ordens.sumOf { it.total }

    val variacaoMes: Double = 286.40

    var tickerSelecionado by mutableStateOf("")
        private set

    var quantidade by mutableIntStateOf(1)
        private set

    var mostrarNovoAtivo by mutableStateOf(false)

    val ativoSelecionado: Ativo? get() = ativoPorTicker(tickerSelecionado)

    fun ativoPorTicker(ticker: String): Ativo? = ativos.firstOrNull { it.ticker == ticker }

    suspend fun carregar() {
        try {
            val doBanco = ativoDao.buscarTodos()
            val ordensDoBanco = ordemDao.buscarTodos()
            ativos = doBanco
            ordens = ordensDoBanco
            if (doBanco.none { it.ticker == tickerSelecionado }) {
                tickerSelecionado = doBanco.firstOrNull()?.ticker ?: ""
                quantidade = 1
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao carregar do banco", e)
        } finally {
            carregado = true
        }
    }

    private fun noBanco(operacao: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                operacao()
            } catch (e: Exception) {
                Log.e(TAG, "Erro na operação do banco", e)
            }
            carregar()
        }
    }

    fun selecionar(ticker: String) {
        if (ticker != tickerSelecionado) {
            tickerSelecionado = ticker
            quantidade = 1
        }
    }

    fun aumentarQuantidade() {
        if (quantidade < 999) quantidade++
    }

    fun diminuirQuantidade() {
        if (quantidade > 1) quantidade--
    }

    fun adicionarAtivo(ticker: String, nome: String, preco: Double?, tipo: TipoAtivo): String? {
        val codigo = ticker.trim().uppercase()
        return when {
            codigo.isEmpty() -> "Informe o código do ativo."
            nome.isBlank() -> "Informe o nome do ativo."
            preco == null || preco <= 0.0 -> "Informe um preço maior que zero."
            ativos.any { it.ticker == codigo } -> "Esse ativo já está na lista."
            else -> {
                noBanco { ativoDao.inserir(Ativo(ticker = codigo, nome = nome.trim(), preco = preco, variacao = 0.0, tipo = tipo)) }
                null
            }
        }
    }

    fun editarAtivo(ativo: Ativo, nome: String, preco: Double?, tipo: TipoAtivo): String? {
        return when {
            nome.isBlank() -> "Informe o nome do ativo."
            preco == null || preco <= 0.0 -> "Informe um preço maior que zero."
            else -> {
                noBanco { ativoDao.atualizar(ativo.copy(nome = nome.trim(), preco = preco, tipo = tipo)) }
                null
            }
        }
    }

    fun removerAtivo(ativo: Ativo) {
        noBanco {
            ordemDao.deletarPorTicker(ativo.ticker)
            ativoDao.deletar(ativo)
        }
    }

    fun confirmarCompra(ativo: Ativo, quantidade: Int) {
        noBanco { ordemDao.inserir(Ordem(ticker = ativo.ticker, quantidade = quantidade, precoUnitario = ativo.preco)) }
    }

    fun cancelarOrdem(ordem: Ordem) {
        noBanco { ordemDao.deletar(ordem) }
    }

    private companion object {
        const val PATRIMONIO_BASE = 8640.75
    }
}
