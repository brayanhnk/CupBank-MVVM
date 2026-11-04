package com.example.cupbank.data.repository

import com.example.cupbank.data.Ativo
import com.example.cupbank.data.AtivoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AtivoRepository(private val dao: AtivoDao) {

    fun buscarTodos(): Flow<List<Ativo>> = dao.buscarTodos()

    suspend fun buscarPorId(id: Int): Ativo? = withContext(Dispatchers.IO) { dao.buscarPorId(id) }

    suspend fun buscarPorTicker(ticker: String): Ativo? = withContext(Dispatchers.IO) { dao.buscarPorTicker(ticker) }

    suspend fun inserir(ativo: Ativo) = withContext(Dispatchers.IO) { dao.inserir(ativo) }

    suspend fun atualizar(ativo: Ativo) = withContext(Dispatchers.IO) { dao.atualizar(ativo) }

    suspend fun deletar(ativo: Ativo) = withContext(Dispatchers.IO) { dao.deletar(ativo) }
}
