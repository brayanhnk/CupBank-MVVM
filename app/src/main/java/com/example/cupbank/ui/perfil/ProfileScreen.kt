package com.example.cupbank.ui.perfil

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cupbank.ui.theme.CupBankBackground
import com.example.cupbank.ui.theme.CupBankPurple
import com.example.cupbank.ui.theme.CupBankPurpleLight
import com.example.cupbank.ui.theme.CupBankSurface
import com.example.cupbank.ui.theme.CupBankTextPrimary
import com.example.cupbank.ui.theme.CupBankTextSecondary

@Composable
fun ProfileScreen(onAbrirCartoes: () -> Unit) {
    var mostrarDadosPessoais by remember { mutableStateOf(false) }

    var contaVerificada by remember {
        mutableStateOf(true)
    }

    Scaffold(
        containerColor = CupBankBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileHeader(
                    nome = "Lucas Pereira",
                    email = "lucas.pereira@email.com",
                    contaVerificada = contaVerificada
                )

                BankInformationCard()

                Spacer(modifier = Modifier.height(24.dp))

                ProfileMenuButton(
                    title = "Dados Pessoais",
                    symbol = "♟",
                    onClick = { mostrarDadosPessoais = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileMenuButton(
                    title = "Cartões",
                    symbol = "▣",
                    onClick = onAbrirCartoes
                )
            }
        }
    }

    if (mostrarDadosPessoais) {
        AlertDialog(
            onDismissRequest = { mostrarDadosPessoais = false },
            containerColor = CupBankSurface,
            titleContentColor = CupBankTextPrimary,
            textContentColor = CupBankTextSecondary,
            title = { Text("Dados Pessoais") },
            text = {
                Column {
                    Text("Nome: Lucas Pereira")
                    Text("E-mail: lucas.pereira@email.com")
                    Text("Conta: verificada")
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDadosPessoais = false }) {
                    Text("Fechar", color = CupBankPurpleLight)
                }
            }
        )
    }
}

@Composable
fun ProfileHeader(
    nome: String,
    email: String,
    contaVerificada: Boolean
) {
    Spacer(modifier = Modifier.height(38.dp))

    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(RoundedCornerShape(46.dp))
            .background(CupBankPurple),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "L",
            color = CupBankTextPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = nome,
        color = CupBankTextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = email,
        color = CupBankTextSecondary,
        fontSize = 13.sp
    )

    Spacer(modifier = Modifier.height(13.dp))

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF123A2B))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (contaVerificada) "✓" else "!",
            color = Color(0xFF32D583),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (contaVerificada) {
                "Conta verificada"
            } else {
                "Conta não verificada"
            },
            color = Color(0xFF32D583),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BankInformationCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CupBankSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 17.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BankInfoItem(
                title = "Agência",
                value = "0001"
            )

            BankInfoItem(
                title = "Conta",
                value = "58.234-7"
            )

            BankInfoItem(
                title = "Tipo",
                value = "Corrente"
            )
        }
    }
}

@Composable
fun BankInfoItem(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = CupBankTextSecondary,
            fontSize = 10.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            color = CupBankTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProfileMenuButton(
    title: String,
    symbol: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CupBankSurface,
            contentColor = CupBankTextPrimary
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = symbol,
                color = CupBankPurpleLight,
                fontSize = 18.sp
            )

            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                color = CupBankTextPrimary,
                fontSize = 14.sp,
                textAlign = TextAlign.Start
            )

            Text(
                text = "›",
                color = CupBankTextSecondary,
                fontSize = 24.sp
            )
        }
    }
}
