package com.example.cupbank.ui.components

import java.util.Locale

private val ptBR: Locale = Locale.forLanguageTag("pt-BR")

fun Double.paraReais(): String = "R$ " + String.format(ptBR, "%,.2f", this)

fun Double.paraPercentual(): String = String.format(ptBR, "%+.2f%%", this)
