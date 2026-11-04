package com.example.cupbank.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cupbank.data.Movimento
import com.example.cupbank.data.repository.MovimentoRepository
import com.example.cupbank.data.saldoAtual
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "BancoViewModel"

data class BancoUiState(
    val movimentos: List<Movimento> = emptyList()
) {
    val saldo: Double get() = movimentos.saldoAtual()
}

class BancoViewModel(private val repository: MovimentoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BancoUiState())
    val uiState: StateFlow<BancoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.buscarTodos()
                .catch { Log.e(TAG, "Erro ao carregar o extrato", it) }
                .collect { lista -> _uiState.update { it.copy(movimentos = lista) } }
        }
    }

    fun receberPix(origem: String, valor: Double) {
        registrar("PIX recebido", "De: $origem", valor)
    }

    fun enviarPix(destino: String, valor: Double?, descricao: String): String? {
        return when {
            destino.isBlank() -> "Informe a chave PIX ou o contato."
            valor == null || valor <= 0.0 -> "Informe um valor maior que zero."
            valor > _uiState.value.saldo -> "Saldo insuficiente."
            else -> {
                val detalhe = if (descricao.isBlank()) "Para: $destino" else "Para: $destino · $descricao"
                registrar("PIX enviado", detalhe, -valor)
                null
            }
        }
    }

    private fun registrar(titulo: String, detalhe: String, valor: Double) {
        executar(TAG) { repository.registrar(titulo, detalhe, valor) }
    }
}
