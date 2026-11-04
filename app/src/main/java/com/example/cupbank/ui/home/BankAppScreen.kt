package com.example.cupbank.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.example.cupbank.viewmodel.BancoViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cupbank.ui.components.paraReais

private val BackgroundColor = Color(0xFF0D0E17)
private val SurfaceDark = Color(0xFF1B1C29)
private val TextGray = Color(0xFF8A8A9A)
private val AccentPurple = Color(0xFF7B5CFA)
private val AccentGreen = Color(0xFF3DDC84)
private val AccentOrange = Color(0xFFFFB020)

@Composable
fun BankAppScreen(
    viewModel: BancoViewModel,
    onAbrirPix: () -> Unit,
    onAbrirCartao: () -> Unit,
    onAbrirExtrato: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var valorPixTexto by remember { mutableStateOf("") }
    val focoValorPix = remember { FocusRequester() }
    val ultimoMovimento = uiState.movimentos.firstOrNull()

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
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Olá,", color = TextGray, fontSize = 16.sp)
                    Text(text = "Lucas", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "L", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AccentPurple)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Saldo Disponível", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.saldo.paraReais(),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Conta corrente • BancoPix", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "AÇÕES RÁPIDAS", color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickAction(label = "PIX", corIcone = AccentOrange, onClick = onAbrirPix)
                QuickAction(label = "Transferir", corIcone = Color.White, onClick = onAbrirPix)
                QuickAction(label = "Pagar", corIcone = Color.White, onClick = onAbrirCartao)
                QuickAction(label = "Receber", corIcone = Color.White, onClick = { focoValorPix.requestFocus() })
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceDark
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Simular recebimento de PIX", color = Color.White, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = valorPixTexto,
                        onValueChange = { novoTexto -> valorPixTexto = novoTexto },
                        label = { Text("Valor (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focoValorPix),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentPurple,
                            unfocusedBorderColor = TextGray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val valor = valorPixTexto.replace(",", ".").toDoubleOrNull()
                            if (valor != null && valor > 0) {
                                viewModel.receberPix("Maria Souza", valor)
                                valorPixTexto = ""
                                Toast.makeText(context, "PIX de R$ $valor recebido!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Digite um valor válido", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text(text = "Receber PIX", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "EXTRATO RECENTE", color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Ver tudo",
                    color = AccentPurple,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { onAbrirExtrato() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (ultimoMovimento == null) {
                Text(text = "Nenhuma movimentação ainda.", color = TextGray, fontSize = 13.sp)
            } else {
                val entrada = ultimoMovimento.valor >= 0
                val corMov = if (entrada) AccentGreen else Color(0xFFFF4444)
                Card(
                    onClick = onAbrirExtrato,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(corMov.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (entrada) "↓" else "↑",
                                color = corMov,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = ultimoMovimento.titulo, color = Color.White, fontWeight = FontWeight.Medium)
                            Text(text = ultimoMovimento.detalhe, color = TextGray, fontSize = 12.sp)
                        }
                        Text(
                            text = (if (entrada) "+" else "-") + kotlin.math.abs(ultimoMovimento.valor).paraReais(),
                            color = corMov,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickAction(label: String, corIcone: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(corIcone)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = TextGray, fontSize = 12.sp)
    }
}
