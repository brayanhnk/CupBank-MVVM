package com.example.cupbank.ui.extrato

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cupbank.data.Movimento
import com.example.cupbank.viewmodel.BancoViewModel
import kotlin.math.abs
import com.example.cupbank.ui.components.CorNegativo
import com.example.cupbank.ui.components.CorPositivo
import com.example.cupbank.ui.theme.CupBankBackground
import com.example.cupbank.ui.theme.CupBankSurface
import com.example.cupbank.ui.theme.CupBankTextPrimary
import com.example.cupbank.ui.theme.CupBankTextSecondary
import com.example.cupbank.ui.components.paraReais

@Composable
fun ExtratoScreen(viewModel: BancoViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CupBankBackground),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Text("Sua conta", color = CupBankTextSecondary, fontSize = 13.sp)
                Text(
                    "Extrato",
                    color = CupBankTextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CupBankSurface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Saldo disponível", color = CupBankTextSecondary, fontSize = 13.sp)
                    Text(
                        text = uiState.saldo.paraReais(),
                        color = CupBankTextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        if (uiState.movimentos.isEmpty()) {
            item {
                Text("Nenhuma movimentação ainda.", color = CupBankTextSecondary, fontSize = 13.sp)
            }
        } else {
            items(uiState.movimentos, key = { it.id }) { mov -> MovimentoCard(mov) }
        }
    }
}

@Composable
private fun MovimentoCard(mov: Movimento) {
    val entrada = mov.valor >= 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CupBankSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mov.titulo,
                    color = CupBankTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${mov.detalhe} • ${mov.data}",
                    color = CupBankTextSecondary,
                    fontSize = 12.sp
                )
            }
            Text(
                text = (if (entrada) "+ " else "- ") + abs(mov.valor).paraReais(),
                color = if (entrada) CorPositivo else CorNegativo,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
