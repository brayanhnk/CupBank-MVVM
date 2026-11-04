package com.example.cupbank.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cupbank.data.AppDatabase
import com.example.cupbank.data.repository.AtivoRepository
import com.example.cupbank.data.repository.MovimentoRepository
import com.example.cupbank.data.repository.OrdemRepository
import com.example.cupbank.data.repository.TransacaoCartaoRepository

class CupBankViewModelFactory(
    private val ativoRepository: AtivoRepository,
    private val ordemRepository: OrdemRepository,
    private val transacaoCartaoRepository: TransacaoCartaoRepository,
    private val movimentoRepository: MovimentoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(BancoViewModel::class.java) ->
            BancoViewModel(movimentoRepository)

        modelClass.isAssignableFrom(CartaoViewModel::class.java) ->
            CartaoViewModel(transacaoCartaoRepository, movimentoRepository)

        modelClass.isAssignableFrom(InvestimentosViewModel::class.java) ->
            InvestimentosViewModel(ativoRepository, ordemRepository)

        else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
    } as T

    companion object {
        fun criar(context: Context): CupBankViewModelFactory {
            val db = AppDatabase.getDatabase(context)
            return CupBankViewModelFactory(
                ativoRepository = AtivoRepository(db.ativoDao()),
                ordemRepository = OrdemRepository(db.ordemDao()),
                transacaoCartaoRepository = TransacaoCartaoRepository(db.transacaoCartaoDao()),
                movimentoRepository = MovimentoRepository(db.movimentoDao())
            )
        }
    }
}
