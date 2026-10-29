package com.example.cupbank

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.cupbank.data.TransacaoCartaoDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.Edit

data class Cartao(
    val numero: String,
    val nomeTitular: String,
    val faturaAtual: Double,
    val dataVencimento: String,
    val limiteTotal: Double,
    val limiteDisponivel: Double
)

@Entity(tableName = "transacoes_cartao")
data class TransacaoCartao(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val descricao: String,
    val valor: Double,
    val data: String,
    val categoria: String,
    val pago: Boolean = false
)

private val BackgroundColor = Color(0xFF0D0E17)
private val SurfaceDark = Color(0xFF1B1C29)
private val TextGray = Color(0xFF8A8A9A)
private val AccentPurple = Color(0xFF7B5CFA)
private val AccentGreen = Color(0xFF3DDC84)
private val AccentRed = Color(0xFFFF4444)

class CartaoState(private val dao: TransacaoCartaoDao) {

    private val dadosDoCartao = Cartao(
        numero = "9010",
        nomeTitular = "LUCAS SOUZA",
        faturaAtual = 0.0,
        dataVencimento = "10 de out.",
        limiteTotal = 9000.00,
        limiteDisponivel = 4500.00
    )

    var transacoes by mutableStateOf<List<TransacaoCartao>>(emptyList())
        private set

    val cartao: Cartao
        get() = dadosDoCartao.copy(faturaAtual = transacoes.filter { !it.pago }.sumOf { it.valor })

    fun transacaoPorId(id: Int): TransacaoCartao? = transacoes.firstOrNull { it.id == id }

    suspend fun carregar() {
        try {
            transacoes = dao.buscarTodos()
        } catch (e: Exception) {
            Log.e(TAG_CARTAO, "Erro ao carregar transações", e)
        }
    }

    suspend fun buscarPorId(id: Int): TransacaoCartao? = withContext(Dispatchers.IO) {
        try {
            dao.buscarPorId(id)
        } catch (e: Exception) {
            Log.e(TAG_CARTAO, "Erro ao buscar transação $id", e)
            null
        }
    }

    private fun noBanco(operacao: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                operacao()
            } catch (e: Exception) {
                Log.e(TAG_CARTAO, "Erro na operação do banco", e)
            }
            carregar()
        }
    }

    fun adicionar(descricao: String, valor: Double, data: String, categoria: String) {
        noBanco { dao.inserir(TransacaoCartao(descricao = descricao, valor = valor, data = data, categoria = categoria)) }
    }

    fun editar(id: Int, descricao: String, valor: Double, data: String, categoria: String): String {
        val transacao = transacaoPorId(id) ?: return "Transação não encontrada"
        if (transacao.pago) return "Transação já foi paga"
        noBanco { dao.atualizar(transacao.copy(descricao = descricao, valor = valor, data = data, categoria = categoria)) }
        return "Transação atualizada"
    }

    fun remover(id: Int): String {
        val transacao = transacaoPorId(id) ?: return "Transação não encontrada"
        if (transacao.pago) return "Transação já foi paga"
        noBanco { dao.deletar(transacao) }
        return "Transação removida"
    }

    fun quitarFatura() {
        val pendentes = transacoes.filter { !it.pago }
        noBanco { pendentes.forEach { dao.atualizar(it.copy(pago = true)) } }
    }
}

private const val TAG_CARTAO = "CartaoState"

@Composable
fun TelaCartao(
    state: CartaoState,
    banco: BancoState,
    onAbrirDetalhe: (Int) -> Unit,
    onVoltar: () -> Unit
) {
    val context = LocalContext.current
    var mostrarDialogAdicionar by remember { mutableStateOf(false) }
    var transacaoParaEditar by remember { mutableStateOf<TransacaoCartao?>(null) }

    LaunchedEffect(Unit) { state.carregar() }

    val pagarFatura: () -> Unit = {
        val valor = state.cartao.faturaAtual
        if (valor <= 0) {
            Toast.makeText(context, "Não há fatura para pagar", Toast.LENGTH_SHORT).show()
        } else if (!banco.pagarFatura(valor)) {
            Toast.makeText(context, "Saldo insuficiente para pagar a fatura", Toast.LENGTH_SHORT).show()
        } else {
            state.quitarFatura()
            Toast.makeText(context, "Fatura paga com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    CartaoScreen(
        cartao = state.cartao,
        transacoes = state.transacoes,
        onVoltar = onVoltar,
        onPagarFatura = pagarFatura,
        onAdicionarTransacao = { mostrarDialogAdicionar = true },
        onRemoverTransacao = { id ->
            Toast.makeText(context, state.remover(id), Toast.LENGTH_SHORT).show()
        },
        onEditarTransacao = { transacaoParaEditar = it },
        onClickTransacao = { onAbrirDetalhe(it.id) }
    )

    transacaoParaEditar?.let { transacao ->
        DialogTransacao(
            inicial = transacao,
            onDismiss = { transacaoParaEditar = null },
            onConfirmar = { desc, valor, data, cat ->
                Toast.makeText(context, state.editar(transacao.id, desc, valor, data, cat), Toast.LENGTH_SHORT).show()
                transacaoParaEditar = null
            }
        )
    }

    if (mostrarDialogAdicionar) {
        DialogTransacao(
            onDismiss = { mostrarDialogAdicionar = false },
            onConfirmar = { desc, valor, data, cat ->
                state.adicionar(desc, valor, data, cat)
                Toast.makeText(context, "Transação adicionada", Toast.LENGTH_SHORT).show()
                mostrarDialogAdicionar = false
            }
        )
    }
}

@Composable
private fun HeaderCartao(onVoltar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
                .clickable { onVoltar() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Cartão",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CartaoVirtual(numero: String, nomeTitular: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF9575FA),
                            Color(0xFF7B5CFA),
                            Color(0xFF6A4FE8)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "nu",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "VISA",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "**** **** **** $numero",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nomeTitular,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "))))",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SecaoFatura(
    valorFatura: Double,
    dataVencimento: String,
    onPagar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(
                    text = "Fatura",
                    color = TextGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "R$ %.2f".format(valorFatura),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Vence em $dataVencimento",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onPagar,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Pagar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SecaoLimite(limiteUsado: Double, limiteTotal: Double) {
    val porcentagemUsada = (limiteUsado / limiteTotal * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Limite do cartão",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "R$ %.2f".format(limiteUsado),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "de R$ %.2f".format(limiteTotal),
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "$porcentagemUsada%",
                    color = AccentPurple,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2A2B3A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(porcentagemUsada / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(AccentPurple)
                )
            }
        }
    }
}

@Composable
private fun CartaoScreen(
    cartao: Cartao,
    transacoes: List<TransacaoCartao>,
    onVoltar: () -> Unit,
    onPagarFatura: () -> Unit,
    onAdicionarTransacao: () -> Unit,
    onRemoverTransacao: (Int) -> Unit,
    onEditarTransacao: (TransacaoCartao) -> Unit,
    onClickTransacao: (TransacaoCartao) -> Unit
) {
    Scaffold(
        containerColor = BackgroundColor,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdicionarTransacao,
                containerColor = AccentPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar transação")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            HeaderCartao(onVoltar)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                item { Spacer(modifier = Modifier.height(20.dp)) }

                item {
                    CartaoVirtual(
                        numero = cartao.numero,
                        nomeTitular = cartao.nomeTitular
                    )
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }

                item {
                    SecaoFatura(
                        valorFatura = cartao.faturaAtual,
                        dataVencimento = cartao.dataVencimento,
                        onPagar = onPagarFatura
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    SecaoLimite(
                        limiteUsado = cartao.limiteTotal - cartao.limiteDisponivel,
                        limiteTotal = cartao.limiteTotal
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    Text(
                        text = "Transações",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                if (transacoes.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nenhuma transação",
                                    color = TextGray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(transacoes, key = { it.id }) { transacao ->
                        ItemTransacao(
                            transacao = transacao,
                            onClick = { onClickTransacao(transacao) },
                            onEditar = { onEditarTransacao(transacao) },
                            onRemover = { onRemoverTransacao(transacao.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun ItemTransacao(
    transacao: TransacaoCartao,
    onClick: () -> Unit,
    onEditar: () -> Unit,
    onRemover: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (transacao.pago) SurfaceDark.copy(alpha = 0.5f) else SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = transacao.categoria.first().toString(),
                        color = AccentPurple,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transacao.descricao,
                        color = if (transacao.pago) TextGray else Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = transacao.categoria,
                            color = TextGray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = " • ${transacao.data}",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                        if (transacao.pago) {
                            Text(
                                text = " • Pago",
                                color = AccentGreen,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "R$ %.2f".format(transacao.valor),
                    color = if (transacao.pago) TextGray else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (!transacao.pago) {
                    IconButton(
                        onClick = onEditar,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = AccentPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onRemover,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remover",
                            tint = AccentRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogTransacao(
    onDismiss: () -> Unit,
    onConfirmar: (String, Double, String, String) -> Unit,

    inicial: TransacaoCartao? = null
) {
    var descricao by remember { mutableStateOf(inicial?.descricao ?: "") }
    var valor by remember { mutableStateOf(inicial?.valor?.toString() ?: "") }
    var data by remember { mutableStateOf(inicial?.data ?: "") }
    var categoria by remember { mutableStateOf(inicial?.categoria ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (inicial == null) "Nova Transação" else "Editar Transação",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = TextGray,
                        focusedLabelColor = AccentPurple,
                        unfocusedLabelColor = TextGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = valor,
                    onValueChange = { valor = it },
                    label = { Text("Valor (R$)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = TextGray,
                        focusedLabelColor = AccentPurple,
                        unfocusedLabelColor = TextGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = data,
                    onValueChange = { data = it },
                    label = { Text("Data (ex: 10 de out.)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = TextGray,
                        focusedLabelColor = AccentPurple,
                        unfocusedLabelColor = TextGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoria") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = TextGray,
                        focusedLabelColor = AccentPurple,
                        unfocusedLabelColor = TextGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val valorDouble = valor.toDoubleOrNull()
                            if (descricao.isNotBlank() && valorDouble != null && data.isNotBlank() && categoria.isNotBlank()) {
                                onConfirmar(descricao, valorDouble, data, categoria)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        enabled = descricao.isNotBlank() && valor.toDoubleOrNull() != null && data.isNotBlank() && categoria.isNotBlank()
                    ) {
                        Text(if (inicial == null) "Adicionar" else "Salvar")
                    }
                }
            }
        }
    }
}

@Composable
fun TelaDetalheTransacao(
    id: Int,
    state: CartaoState,
    onVoltar: () -> Unit
) {
    var transacao by remember(id) { mutableStateOf<TransacaoCartao?>(null) }
    var carregando by remember(id) { mutableStateOf(true) }

    LaunchedEffect(id) {
        transacao = state.buscarPorId(id)
        carregando = false
    }

    val encontrada = transacao
    if (encontrada != null) {
        DetalheTransacaoConteudo(encontrada, onVoltar)
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (!carregando) Text("Transação não encontrada", color = TextGray)
        }
    }
}

@Composable
private fun DetalheTransacaoConteudo(
    transacao: TransacaoCartao,
    onVoltar: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SurfaceDark)
                        .clickable { onVoltar() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Detalhes da Transação",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AccentPurple.copy(alpha = 0.2f))
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transacao.categoria.first().toString(),
                    color = AccentPurple,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "R$ %.2f".format(transacao.valor),
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (transacao.pago) "Transação Paga" else "Pendente",
                color = if (transacao.pago) AccentGreen else TextGray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            DetalheItem(label = "Estabelecimento", valor = transacao.descricao)
            Spacer(modifier = Modifier.height(12.dp))

            DetalheItem(label = "Categoria", valor = transacao.categoria)
            Spacer(modifier = Modifier.height(12.dp))

            DetalheItem(label = "Data", valor = transacao.data)
            Spacer(modifier = Modifier.height(12.dp))

            DetalheItem(label = "ID da Transação", valor = "#${transacao.id.toString().padStart(6, '0')}")

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AccentPurple.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "ℹ️ Informação",
                        color = AccentPurple,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (transacao.pago) {
                            "Esta transação já foi paga e incluída na última fatura quitada."
                        } else {
                            "Esta transação será incluída na próxima fatura."
                        },
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DetalheItem(label: String, valor: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextGray,
                fontSize = 14.sp
            )
            Text(
                text = valor,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
