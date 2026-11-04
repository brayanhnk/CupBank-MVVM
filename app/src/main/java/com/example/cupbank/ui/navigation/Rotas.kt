package com.example.cupbank.ui.navigation

object Rotas {

    const val INICIO = "inicio"
    const val EXTRATO = "extrato"
    const val INVESTIR = "investir"
    const val PERFIL = "perfil"

    const val PIX = "pix"
    const val CARTAO = "cartao"

    const val ARG_TICKER = "ticker"
    const val DETALHE_ATIVO = "detalhe_ativo/{$ARG_TICKER}"

    const val ARG_ID = "id"
    const val DETALHE_TRANSACAO = "cartao_transacao/{$ARG_ID}"

    fun detalheAtivo(ticker: String) = "detalhe_ativo/$ticker"
    fun detalheTransacao(id: Int) = "cartao_transacao/$id"

    fun abaDaRota(rota: String?): String? = when (rota) {
        INICIO, PIX -> INICIO
        EXTRATO -> EXTRATO
        INVESTIR, DETALHE_ATIVO -> INVESTIR
        PERFIL, CARTAO, DETALHE_TRANSACAO -> PERFIL
        else -> null
    }
}
