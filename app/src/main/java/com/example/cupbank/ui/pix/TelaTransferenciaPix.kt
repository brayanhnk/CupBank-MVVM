package com.example.cupbank.ui.pix

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.cupbank.viewmodel.BancoViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val corFundo = Color(0xFF0E0E1A)
private val corCampo = Color(0xFF1C1C2E)
private val corRoxo = Color(0xFF6C4CE0)
private val corTextoCinza = Color(0xFF9A9AAE)

@Composable
fun TelaTransferenciaPix(viewModel: BancoViewModel, onVoltar: () -> Unit) {
    val context = LocalContext.current

    var abaSelecionada by remember { mutableStateOf("Chave PIX") }
    var chavePix by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(corFundo)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
        ) {
            Text(
                text = "<",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier
                    .clickable { onVoltar() }
                    .padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Transferência PIX",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { abaSelecionada = "Chave PIX" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (abaSelecionada == "Chave PIX") corRoxo else corCampo
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Chave PIX", color = Color.White)
            }
            Button(
                onClick = { abaSelecionada = "Contato" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (abaSelecionada == "Contato") corRoxo else corCampo
                )
            ) {
                Text("Contato", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (abaSelecionada == "Chave PIX") "CHAVE PIX" else "CONTATO",
            color = corTextoCinza,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = chavePix,
            onValueChange = { chavePix = it },
            placeholder = {
                Text(
                    if (abaSelecionada == "Chave PIX") "CPF, e-mail, telefone ou chave aleatória" else "Nome do contato",
                    color = corTextoCinza
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = corCampo,
                unfocusedContainerColor = corCampo,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "VALOR", color = corTextoCinza, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = valor,
            onValueChange = { valor = it },
            placeholder = { Text("R$ 0,00", color = corTextoCinza) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = corCampo,
                unfocusedContainerColor = corCampo,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "DESCRIÇÃO (OPCIONAL)", color = corTextoCinza, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        TextField(
            value = descricao,
            onValueChange = { descricao = it },
            placeholder = { Text("Ex: aluguel, jantar...", color = corTextoCinza) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = corCampo,
                unfocusedContainerColor = corCampo,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                val valorNumerico = valor.replace("R$", "").trim().replace(",", ".").toDoubleOrNull()
                val erro = viewModel.enviarPix(chavePix, valorNumerico, descricao)
                if (erro == null) {
                    Toast.makeText(context, "PIX enviado! Veja no Extrato.", Toast.LENGTH_SHORT).show()
                    onVoltar()
                } else {
                    Toast.makeText(context, erro, Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = corRoxo),
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(text = "Continuar", color = Color.White, fontSize = 16.sp)
        }

    }
}
