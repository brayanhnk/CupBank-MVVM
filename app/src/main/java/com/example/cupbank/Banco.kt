package com.example.cupbank

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.cupbank.data.MovimentoDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "movimentos")
data class Movimento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val detalhe: String,
    val valor: Double,
    val data: String
)

private const val TAG_BANCO = "BancoState"

class BancoState(private val dao: MovimentoDao) {

    var movimentos by mutableStateOf<List<Movimento>>(emptyList())
        private set

    val saldo: Double get() = SALDO_BASE + movimentos.sumOf { it.valor }

    suspend fun carregar() {
        try {
            movimentos = dao.buscarTodos()
        } catch (e: Exception) {
            Log.e(TAG_BANCO, "Erro ao carregar o extrato", e)
        }
    }

    fun receberPix(origem: String, valor: Double) {
        registrar("PIX recebido", "De: $origem", valor)
    }

    fun enviarPix(destino: String, valor: Double?, descricao: String): String? {
        return when {
            destino.isBlank() -> "Informe a chave PIX ou o contato."
            valor == null || valor <= 0.0 -> "Informe um valor maior que zero."
            valor > saldo -> "Saldo insuficiente."
            else -> {
                val detalhe = if (descricao.isBlank()) "Para: $destino" else "Para: $destino · $descricao"
                registrar("PIX enviado", detalhe, -valor)
                null
            }
        }
    }

    fun pagarFatura(valor: Double): Boolean {
        if (valor > saldo) return false
        registrar("Fatura do cartão", "Cartão final 9010", -valor)
        return true
    }

    private fun registrar(titulo: String, detalhe: String, valor: Double) {
        val agora = SimpleDateFormat("dd/MM HH:mm", Locale.forLanguageTag("pt-BR")).format(Date())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                dao.inserir(Movimento(titulo = titulo, detalhe = detalhe, valor = valor, data = agora))
            } catch (e: Exception) {
                Log.e(TAG_BANCO, "Erro ao salvar movimento", e)
            }
            carregar()
        }
    }

    private companion object {

        const val SALDO_BASE = 376.80
    }
}
