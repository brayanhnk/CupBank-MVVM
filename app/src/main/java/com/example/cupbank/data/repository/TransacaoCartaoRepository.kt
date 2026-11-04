package com.example.cupbank.data.repository

import com.example.cupbank.data.TransacaoCartao
import com.example.cupbank.data.TransacaoCartaoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TransacaoCartaoRepository(private val dao: TransacaoCartaoDao) {

    fun buscarTodos(): Flow<List<TransacaoCartao>> = dao.buscarTodos()

    suspend fun buscarPorId(id: Int): TransacaoCartao? = withContext(Dispatchers.IO) { dao.buscarPorId(id) }

    suspend fun inserir(transacao: TransacaoCartao) = withContext(Dispatchers.IO) { dao.inserir(transacao) }

    suspend fun atualizar(transacao: TransacaoCartao) = withContext(Dispatchers.IO) { dao.atualizar(transacao) }

    suspend fun deletar(transacao: TransacaoCartao) = withContext(Dispatchers.IO) { dao.deletar(transacao) }

    suspend fun quitarPendentes() = withContext(Dispatchers.IO) { dao.quitarPendentes() }
}
