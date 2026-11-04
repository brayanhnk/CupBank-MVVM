package com.example.cupbank.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TipoAtivo(val rotulo: String, val singular: String) {
    ACAO("Ações", "Ação"),
    FUNDO("Fundos", "Fundo")
}

@Entity(
    tableName = "ativos",
    indices = [Index(value = ["ticker"], unique = true)]
)
data class Ativo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticker: String,
    val nome: String,
    val preco: Double,
    val variacao: Double,
    val tipo: TipoAtivo
)

@Entity(tableName = "ordens")
data class Ordem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticker: String,
    val quantidade: Int,
    val precoUnitario: Double
) {
    val total: Double get() = precoUnitario * quantidade
}

@Entity(tableName = "transacoes_cartao")
data class TransacaoCartao(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val descricao: String,
    val valor: Double,
    val data: String,
    val categoria: String,
    val pago: Boolean = false
)

data class Cartao(
    val numero: String,
    val nomeTitular: String,
    val faturaAtual: Double,
    val dataVencimento: String,
    val limiteTotal: Double,
    val limiteDisponivel: Double
)

@Entity(tableName = "movimentos")
data class Movimento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val detalhe: String,
    val valor: Double,
    val data: String
)

const val SALDO_BASE = 376.80

fun List<Movimento>.saldoAtual(): Double = SALDO_BASE + sumOf { it.valor }
