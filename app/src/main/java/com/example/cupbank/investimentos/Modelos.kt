package com.example.cupbank.investimentos

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
