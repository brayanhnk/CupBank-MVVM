package com.example.cupbank.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cupbank.Movimento
import com.example.cupbank.TransacaoCartao
import com.example.cupbank.investimentos.Ativo
import com.example.cupbank.investimentos.Ordem

@Dao
interface AtivoDao {
    @Insert
    suspend fun inserir(ativo: Ativo)

    @Query("SELECT * FROM ativos ORDER BY id")
    suspend fun buscarTodos(): List<Ativo>

    @Update
    suspend fun atualizar(ativo: Ativo)

    @Delete
    suspend fun deletar(ativo: Ativo)

    @Query("SELECT * FROM ativos WHERE id = :id")
    suspend fun buscarPorId(id: Int): Ativo?

    @Query("SELECT * FROM ativos WHERE ticker = :ticker")
    suspend fun buscarPorTicker(ticker: String): Ativo?
}

@Dao
interface OrdemDao {
    @Insert
    suspend fun inserir(ordem: Ordem)

    @Query("SELECT * FROM ordens ORDER BY id DESC")
    suspend fun buscarTodos(): List<Ordem>

    @Update
    suspend fun atualizar(ordem: Ordem)

    @Delete
    suspend fun deletar(ordem: Ordem)

    @Query("DELETE FROM ordens WHERE ticker = :ticker")
    suspend fun deletarPorTicker(ticker: String)
}

@Dao
interface TransacaoCartaoDao {
    @Insert
    suspend fun inserir(transacao: TransacaoCartao)

    @Query("SELECT * FROM transacoes_cartao ORDER BY id")
    suspend fun buscarTodos(): List<TransacaoCartao>

    @Update
    suspend fun atualizar(transacao: TransacaoCartao)

    @Delete
    suspend fun deletar(transacao: TransacaoCartao)

    @Query("SELECT * FROM transacoes_cartao WHERE id = :id")
    suspend fun buscarPorId(id: Int): TransacaoCartao?
}

@Dao
interface MovimentoDao {
    @Insert
    suspend fun inserir(movimento: Movimento)

    @Query("SELECT * FROM movimentos ORDER BY id DESC")
    suspend fun buscarTodos(): List<Movimento>

    @Update
    suspend fun atualizar(movimento: Movimento)

    @Delete
    suspend fun deletar(movimento: Movimento)
}
