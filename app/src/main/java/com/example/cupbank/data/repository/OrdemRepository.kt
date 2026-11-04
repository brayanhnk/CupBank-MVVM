package com.example.cupbank.data.repository

import com.example.cupbank.data.Ordem
import com.example.cupbank.data.OrdemDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class OrdemRepository(private val dao: OrdemDao) {

    fun buscarTodos(): Flow<List<Ordem>> = dao.buscarTodos()

    suspend fun inserir(ordem: Ordem) = withContext(Dispatchers.IO) { dao.inserir(ordem) }

    suspend fun atualizar(ordem: Ordem) = withContext(Dispatchers.IO) { dao.atualizar(ordem) }

    suspend fun deletar(ordem: Ordem) = withContext(Dispatchers.IO) { dao.deletar(ordem) }

    suspend fun deletarPorTicker(ticker: String) = withContext(Dispatchers.IO) { dao.deletarPorTicker(ticker) }
}
