package com.example.cupbank.investimentos

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cupbank.CupBankBackground
import com.example.cupbank.CupBankField
import com.example.cupbank.CupBankPurpleLight
import com.example.cupbank.CupBankSurface
import com.example.cupbank.CupBankTextPrimary
import com.example.cupbank.CupBankTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalheAtivoScreen(
    ticker: String,
    state: InvestimentosState,
    onVoltar: () -> Unit
) {
    val context = LocalContext.current
    val ativo = state.ativoPorTicker(ticker)

    LaunchedEffect(Unit) { if (!state.carregado) state.carregar() }

    LaunchedEffect(ticker, state.carregado) { if (state.ativoPorTicker(ticker) != null) state.selecionar(ticker) }

    Scaffold(
        containerColor = CupBankBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = ticker,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CupBankBackground,
                    titleContentColor = CupBankTextPrimary,
                    navigationIconContentColor = CupBankTextPrimary
                )
            )
        }
    ) { innerPadding ->
        if (!state.carregado) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding))
        } else if (ativo == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Ativo não encontrado.", color = CupBankTextSecondary)
            }
        } else {
            val quantidade = state.quantidade
            val total = ativo.preco * quantidade
            val precoAbertura = ativo.preco / (1 + ativo.variacao / 100)
            val variacaoDia = (ativo.preco - precoAbertura) * quantidade
            val pesoNaCarteira = total / (state.patrimonio + total) * 100
            val ordensDoAtivo = state.ordens.filter { it.ticker == ativo.ticker }
            val cotasNaCarteira = ordensDoAtivo.sumOf { it.quantidade }
            val valorNaCarteira = ordensDoAtivo.sumOf { it.total }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {
                    CartaoSuperficie {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarAtivo(ticker = ativo.ticker, destaque = true, tamanho = 52.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ativo.nome,
                                    color = CupBankTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = ativo.tipo.singular,
                                    color = CupBankTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = ativo.preco.paraReais(),
                                    color = CupBankTextPrimary,
                                    fontSize = 18.sp,
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
                        }
                    }
                }

                item {
                    CartaoSuperficie {
                        Column(modifier = Modifier.padding(18.dp)) {
                            RotuloPequeno("SIMULAÇÃO DE COMPRA")
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Quantidade", color = CupBankTextPrimary, fontSize = 15.sp)
                                SeletorQuantidade(
                                    quantidade = quantidade,
                                    onDiminuir = { state.diminuirQuantidade() },
                                    onAumentar = { state.aumentarQuantidade() }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = CupBankField)
                            LinhaInfo("Total estimado", total.paraReais(), destaque = true)
                            LinhaInfo("Preço de abertura (estimado)", precoAbertura.paraReais())
                            LinhaInfo(
                                "Variação no dia (sua compra)",
                                (if (variacaoDia >= 0) "+ " else "- ") + kotlin.math.abs(variacaoDia).paraReais(),
                                cor = corDaVariacao(variacaoDia)
                            )
                            LinhaInfo(
                                "Peso na carteira após comprar",
                                String.format(java.util.Locale.forLanguageTag("pt-BR"), "%.1f%%", pesoNaCarteira)
                            )
                        }
                    }
                }

                item {
                    CartaoSuperficie {
                        Column(modifier = Modifier.padding(18.dp)) {
                            RotuloPequeno("SUA POSIÇÃO")
                            LinhaInfo(
                                "Cotas na carteira",
                                cotasNaCarteira.toString()
                            )
                            LinhaInfo("Valor investido", valorNaCarteira.paraReais())
                        }
                    }
                }

                item {
                    val cotas = if (quantidade == 1) "cota" else "cotas"
                    Button(
                        onClick = {
                            state.confirmarCompra(ativo, quantidade)
                            Toast.makeText(
                                context,
                                "Compra de $quantidade $cotas de ${ativo.ticker} confirmada.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CupBankPurpleLight,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Confirmar compra de $quantidade $cotas",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    Column {
                        Spacer(Modifier.height(4.dp))
                        RotuloPequeno("MINHAS ORDENS DE ${ativo.ticker}")
                    }
                }

                if (ordensDoAtivo.isEmpty()) {
                    item {
                        Text(
                            text = "Você ainda não tem ordens neste ativo.",
                            color = CupBankTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    items(ordensDoAtivo, key = { it.id }) { ordem ->
                        OrdemCard(
                            ordem = ordem,
                            onCancelar = {
                                state.cancelarOrdem(ordem)
                                Toast.makeText(context, "Ordem cancelada.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoSuperficie(conteudo: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CupBankSurface)
    ) {
        conteudo()
    }
}

@Composable
private fun LinhaInfo(
    rotulo: String,
    valor: String,
    destaque: Boolean = false,
    cor: Color = CupBankTextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(rotulo, color = CupBankTextSecondary, fontSize = 13.sp)
        Text(
            text = valor,
            color = cor,
            fontSize = if (destaque) 18.sp else 14.sp,
            fontWeight = if (destaque) FontWeight.ExtraBold else FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun OrdemCard(ordem: Ordem, onCancelar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CupBankSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val cotas = if (ordem.quantidade == 1) "cota" else "cotas"
                Text(
                    text = "${ordem.quantidade} $cotas · ${ordem.precoUnitario.paraReais()}",
                    color = CupBankTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total ${ordem.total.paraReais()}",
                    color = CupBankTextSecondary,
                    fontSize = 12.sp
                )
            }
            IconButton(onClick = onCancelar) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Cancelar ordem",
                    tint = CorNegativo
                )
            }
        }
    }
}
