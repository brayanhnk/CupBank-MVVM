package com.example.cupbank.data.repository

import com.example.cupbank.data.Movimento
import com.example.cupbank.data.MovimentoDao
import com.example.cupbank.data.saldoAtual
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MovimentoRepository(private val dao: MovimentoDao) {

    fun buscarTodos(): Flow<List<Movimento>> = dao.buscarTodos()

    fun saldo(): Flow<Double> = dao.buscarTodos().map { it.saldoAtual() }

    suspend fun inserir(movimento: Movimento) = withContext(Dispatchers.IO) { dao.inserir(movimento) }

    suspend fun atualizar(movimento: Movimento) = withContext(Dispatchers.IO) { dao.atualizar(movimento) }

    suspend fun deletar(movimento: Movimento) = withContext(Dispatchers.IO) { dao.deletar(movimento) }

    suspend fun registrar(titulo: String, detalhe: String, valor: Double) {
        val agora = SimpleDateFormat("dd/MM HH:mm", Locale.forLanguageTag("pt-BR")).format(Date())
        inserir(Movimento(titulo = titulo, detalhe = detalhe, valor = valor, data = agora))
    }
}
