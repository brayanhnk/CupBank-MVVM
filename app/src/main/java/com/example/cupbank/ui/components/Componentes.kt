package com.example.cupbank.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cupbank.ui.theme.CupBankField
import com.example.cupbank.ui.theme.CupBankPurpleLight
import com.example.cupbank.ui.theme.CupBankTextPrimary
import com.example.cupbank.ui.theme.CupBankTextSecondary

val CorPositivo = Color(0xFF32D583)
val CorNegativo = Color(0xFFF97066)

fun corDaVariacao(variacao: Double): Color = if (variacao >= 0) CorPositivo else CorNegativo

@Composable
fun AvatarAtivo(ticker: String, destaque: Boolean, tamanho: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(tamanho)
            .clip(CircleShape)
            .background(CupBankField),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ticker.take(2),
            color = if (destaque) CupBankPurpleLight else CupBankTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SeletorQuantidade(
    quantidade: Int,
    onDiminuir: () -> Unit,
    onAumentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CupBankField),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onDiminuir) {
            Text(
                text = "−",
                color = CupBankPurpleLight,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = quantidade.toString(),
            color = CupBankTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        IconButton(onClick = onAumentar) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Aumentar quantidade",
                tint = CupBankPurpleLight
            )
        }
    }
}

@Composable
fun RotuloPequeno(texto: String) {
    Text(
        text = texto,
        color = CupBankTextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.8.sp
    )
}
