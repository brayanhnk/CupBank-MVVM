package com.example.cupbank.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

internal fun ViewModel.executar(tag: String, operacao: suspend () -> Unit) {
    viewModelScope.launch {
        try {
            operacao()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(tag, "Erro na operação do banco", e)
        }
    }
}
