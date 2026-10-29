package com.example.cupbank.investimentos

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cupbank.CupBankBackground
import com.example.cupbank.CupBankField
import com.example.cupbank.CupBankPurpleLight
import com.example.cupbank.CupBankSurface
import com.example.cupbank.CupBankTextPrimary
import com.example.cupbank.CupBankTextSecondary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InvestirScreen(
    state: InvestimentosState,
    onAbrirDetalhe: (String) -> Unit
) {
    val context = LocalContext.current
    var filtro by remember { mutableStateOf<TipoAtivo?>(null) }
    var ativoSelecionadoNoMenu by remember { mutableStateOf<Ativo?>(null) }
    var ativoParaRemover by remember { mutableStateOf<Ativo?>(null) }
    var ativoParaEditar by remember { mutableStateOf<Ativo?>(null) }

    LaunchedEffect(Unit) { state.carregar() }

    val visiveis = state.ativos.filter { filtro == null || it.tipo == filtro }
    val selecionado = state.ativoSelecionado

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CupBankBackground),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sua carteira", color = CupBankTextSecondary, fontSize = 13.sp)
                    Text(
                        "Investimentos",
                        color = CupBankTextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { state.mostrarNovoAtivo = true },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CupBankPurpleLight,
                        contentColor = Color.White
                    )
                ) {
                    Text("+ Investir", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CupBankSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Patrimônio investido", color = CupBankTextSecondary, fontSize = 13.sp)
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = state.patrimonio.paraReais(),
                            color = CupBankTextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "+ " + state.variacaoMes.paraReais(),
                            color = CorPositivo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("este mês", color = CupBankTextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        if (selecionado != null) {

            item {
                Card(
                    onClick = { onAbrirDetalhe(selecionado.ticker) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CupBankSurface),
                    border = BorderStroke(1.dp, CupBankPurpleLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CupBankPurpleLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        val cotas = if (state.quantidade == 1) "cota" else "cotas"
                        Text(
                            text = "Ordem de ${state.quantidade} $cotas de ${selecionado.ticker} pronta para revisão.",
                            color = CupBankTextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CupBankSurface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                RotuloPequeno("ATIVO SELECIONADO")
                                Spacer(Modifier.size(4.dp))
                                Text(
                                    text = "${selecionado.ticker} · ${selecionado.nome}",
                                    color = CupBankTextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Compra",
                                color = CorPositivo,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.size(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                RotuloPequeno("QUANTIDADE")
                                Spacer(Modifier.size(6.dp))
                                SeletorQuantidade(
                                    quantidade = state.quantidade,
                                    onDiminuir = { state.diminuirQuantidade() },
                                    onAumentar = { state.aumentarQuantidade() }
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                RotuloPequeno("TOTAL ESTIMADO")
                                Spacer(Modifier.size(6.dp))
                                Text(
                                    text = (selecionado.preco * state.quantidade).paraReais(),
                                    color = CupBankTextPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FiltroChip("Todos", filtro == null) { filtro = null }
                FiltroChip(TipoAtivo.ACAO.rotulo, filtro == TipoAtivo.ACAO) { filtro = TipoAtivo.ACAO }
                FiltroChip(TipoAtivo.FUNDO.rotulo, filtro == TipoAtivo.FUNDO) { filtro = TipoAtivo.FUNDO }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RotuloPequeno("OPÇÕES DA BOLSA")
                Text("Toque para comprar", color = CupBankTextSecondary, fontSize = 11.sp)
            }
        }

        if (visiveis.isEmpty()) {
            item {
                Text(
                    text = "Nenhum ativo nesta categoria. Use \"+ Investir\" para adicionar um.",
                    color = CupBankTextSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            items(visiveis, key = { it.ticker }) { ativo ->
                AtivoCard(
                    ativo = ativo,
                    selecionado = ativo.ticker == state.tickerSelecionado,
                    onClick = {
                        state.selecionar(ativo.ticker)
                        onAbrirDetalhe(ativo.ticker)
                    },
                    onLongClick = { ativoSelecionadoNoMenu = ativo }
                )
            }
        }

        item {
            Text(
                text = "Segure um ativo para editá-lo ou removê-lo da lista.",
                color = CupBankTextSecondary,
                fontSize = 11.sp
            )
        }
    }

    ativoSelecionadoNoMenu?.let { ativo ->
        AlertDialog(
            onDismissRequest = { ativoSelecionadoNoMenu = null },
            containerColor = CupBankSurface,
            titleContentColor = CupBankTextPrimary,
            title = { Text("${ativo.ticker} · ${ativo.nome}") },
            confirmButton = {
                TextButton(onClick = {
                    ativoParaEditar = ativo
                    ativoSelecionadoNoMenu = null
                }) { Text("Editar", color = CupBankPurpleLight, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        ativoParaRemover = ativo
                        ativoSelecionadoNoMenu = null
                    }) { Text("Remover", color = CorNegativo) }
                    TextButton(onClick = { ativoSelecionadoNoMenu = null }) {
                        Text("Cancelar", color = CupBankTextSecondary)
                    }
                }
            }
        )
    }

    ativoParaRemover?.let { ativo ->
        AlertDialog(
            onDismissRequest = { ativoParaRemover = null },
            containerColor = CupBankSurface,
            titleContentColor = CupBankTextPrimary,
            textContentColor = CupBankTextSecondary,
            title = { Text("Remover ${ativo.ticker}?") },
            text = { Text("O ativo sai da lista e as ordens dele também serão removidas da carteira.") },
            confirmButton = {
                TextButton(onClick = {
                    state.removerAtivo(ativo)
                    ativoParaRemover = null
                    Toast.makeText(context, "${ativo.ticker} removido.", Toast.LENGTH_SHORT).show()
                }) { Text("Remover", color = CorNegativo) }
            },
            dismissButton = {
                TextButton(onClick = { ativoParaRemover = null }) {
                    Text("Cancelar", color = CupBankPurpleLight)
                }
            }
        )
    }

    ativoParaEditar?.let { ativo ->
        NovoAtivoDialog(
            ativoExistente = ativo,
            onCancelar = { ativoParaEditar = null },
            onSalvar = { _, nome, preco, tipo ->
                val erro = state.editarAtivo(ativo, nome, preco, tipo)
                if (erro == null) {
                    ativoParaEditar = null
                    Toast.makeText(context, "Ativo atualizado.", Toast.LENGTH_SHORT).show()
                }
                erro
            }
        )
    }

    if (state.mostrarNovoAtivo) {
        NovoAtivoDialog(
            onCancelar = { state.mostrarNovoAtivo = false },
            onSalvar = { ticker, nome, preco, tipo ->
                val erro = state.adicionarAtivo(ticker, nome, preco, tipo)
                if (erro == null) {
                    state.mostrarNovoAtivo = false
                    Toast.makeText(context, "Ativo adicionado.", Toast.LENGTH_SHORT).show()
                }
                erro
            }
        )
    }
}

@Composable
private fun FiltroChip(rotulo: String, selecionado: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selecionado) CupBankPurpleLight else CupBankSurface
    ) {
        Text(
            text = rotulo,
            color = if (selecionado) Color.White else CupBankTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AtivoCard(
    ativo: Ativo,
    selecionado: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CupBankSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarAtivo(ticker = ativo.ticker, destaque = selecionado)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ativo.ticker,
                    color = CupBankTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = ativo.nome, color = CupBankTextSecondary, fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = ativo.preco.paraReais(),
                    color = CupBankTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = ativo.variacao.paraPercentual(),
                    color = corDaVariacao(ativo.variacao),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver detalhes",
                tint = CupBankTextSecondary
            )
        }
    }
}

@Composable
private fun NovoAtivoDialog(
    onCancelar: () -> Unit,

    ativoExistente: Ativo? = null,

    onSalvar: (ticker: String, nome: String, preco: Double?, tipo: TipoAtivo) -> String?
) {
    val editando = ativoExistente != null
    var ticker by remember { mutableStateOf(ativoExistente?.ticker ?: "") }
    var nome by remember { mutableStateOf(ativoExistente?.nome ?: "") }
    var precoTexto by remember {
        mutableStateOf(ativoExistente?.preco?.toString() ?: "")
    }
    var tipo by remember { mutableStateOf(ativoExistente?.tipo ?: TipoAtivo.ACAO) }
    var erro by remember { mutableStateOf<String?>(null) }

    val camposColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = CupBankTextPrimary,
        unfocusedTextColor = CupBankTextPrimary,
        focusedBorderColor = CupBankPurpleLight,
        unfocusedBorderColor = CupBankField,
        focusedLabelColor = CupBankPurpleLight,
        unfocusedLabelColor = CupBankTextSecondary,
        cursorColor = CupBankPurpleLight
    )

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = CupBankSurface,
        titleContentColor = CupBankTextPrimary,
        title = { Text(if (editando) "Editar ativo" else "Novo ativo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = ticker,
                    onValueChange = { ticker = it.uppercase().take(8) },
                    label = { Text("Código (ex: WEGE3)") },
                    singleLine = true,
                    enabled = !editando,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = camposColors
                )
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    colors = camposColors
                )
                OutlinedTextField(
                    value = precoTexto,
                    onValueChange = { precoTexto = it },
                    label = { Text("Preço (R$)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = camposColors
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoAtivo.entries.forEach { opcao ->
                        FiltroChip(opcao.rotulo, tipo == opcao) { tipo = opcao }
                    }
                }
                erro?.let { Text(it, color = CorNegativo, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val preco = precoTexto.replace(',', '.').toDoubleOrNull()
                erro = onSalvar(ticker, nome, preco, tipo)
            }) { Text(if (editando) "Salvar" else "Adicionar", color = CupBankPurpleLight, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar", color = CupBankTextSecondary) }
        }
    )
}
