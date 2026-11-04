package com.example.cupbank.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cupbank.ui.theme.CupBankPurpleLight
import com.example.cupbank.ui.theme.CupBankTextSecondary

private data class Aba(val rota: String, val rotulo: String, val icone: ImageVector)

private val AbaInicio = Aba(Rotas.INICIO, "Início", Icons.Filled.Home)
private val AbaExtrato = Aba(Rotas.EXTRATO, "Extrato", Icons.AutoMirrored.Filled.List)
private val AbaInvestir = Aba(Rotas.INVESTIR, "Investir", Icons.AutoMirrored.Filled.TrendingUp)
private val AbaPerfil = Aba(Rotas.PERFIL, "Perfil", Icons.Filled.Person)

@Composable
fun CupBankBottomBar(
    rotaAtual: String?,
    onAbaClick: (String) -> Unit,
    onMaisClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        NavigationBar(
            modifier = Modifier.padding(top = 26.dp),
            containerColor = Color(0xFF0F1424)
        ) {
            ItemAba(AbaInicio, rotaAtual, onAbaClick)
            ItemAba(AbaExtrato, rotaAtual, onAbaClick)

            Spacer(modifier = Modifier.weight(1f))
            ItemAba(AbaInvestir, rotaAtual, onAbaClick)
            ItemAba(AbaPerfil, rotaAtual, onAbaClick)
        }

        Surface(
            onClick = onMaisClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(60.dp),
            shape = RoundedCornerShape(20.dp),
            color = CupBankPurpleLight
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.AddCircle,
                    contentDescription = "Novo investimento",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun RowScope.ItemAba(
    aba: Aba,
    rotaAtual: String?,
    onClick: (String) -> Unit
) {

    val selecionada = Rotas.abaDaRota(rotaAtual) == aba.rota

    NavigationBarItem(
        selected = selecionada,
        onClick = { onClick(aba.rota) },
        icon = { Icon(imageVector = aba.icone, contentDescription = aba.rotulo) },
        label = { Text(text = aba.rotulo, fontSize = 11.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = CupBankPurpleLight,
            selectedTextColor = CupBankPurpleLight,
            indicatorColor = Color.Transparent,
            unselectedIconColor = CupBankTextSecondary,
            unselectedTextColor = CupBankTextSecondary
        )
    )
}
