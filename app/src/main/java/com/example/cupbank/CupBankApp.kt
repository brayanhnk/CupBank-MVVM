package com.example.cupbank

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cupbank.data.AppDatabase
import com.example.cupbank.investimentos.DetalheAtivoScreen
import com.example.cupbank.investimentos.InvestimentosState
import com.example.cupbank.investimentos.InvestirScreen

@Composable
fun CupBankApp() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val banco = remember { BancoState(db.movimentoDao()) }
    val cartao = remember { CartaoState(db.transacaoCartaoDao()) }
    val investimentos = remember { InvestimentosState(db.ativoDao(), db.ordemDao()) }

    LaunchedEffect(Unit) { banco.carregar() }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination?.route

    Scaffold(
        containerColor = CupBankBackground,
        bottomBar = {
            CupBankBottomBar(
                rotaAtual = rotaAtual,
                onAbaClick = { rota -> navController.irParaAba(rota) },
                onMaisClick = {

                    navController.irParaAba(Rotas.INVESTIR)
                    investimentos.mostrarNovoAtivo = true
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rotas.INICIO,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Rotas.INICIO) {
                BankAppScreen(
                    banco = banco,
                    onAbrirPix = { navController.navigate(Rotas.PIX) },
                    onAbrirCartao = { navController.navigate(Rotas.CARTAO) },
                    onAbrirExtrato = { navController.irParaAba(Rotas.EXTRATO) }
                )
            }

            composable(Rotas.PIX) {
                TelaTransferenciaPix(
                    banco = banco,
                    onVoltar = { navController.popBackStack() }
                )
            }

            composable(Rotas.EXTRATO) { ExtratoScreen(banco = banco) }

            composable(Rotas.INVESTIR) {
                InvestirScreen(
                    state = investimentos,
                    onAbrirDetalhe = { ticker ->
                        navController.navigate(Rotas.detalheAtivo(ticker))
                    }
                )
            }

            composable(
                route = Rotas.DETALHE_ATIVO,
                arguments = listOf(navArgument(Rotas.ARG_TICKER) { type = NavType.StringType })
            ) { entry ->
                val ticker = entry.arguments?.getString(Rotas.ARG_TICKER).orEmpty()
                DetalheAtivoScreen(
                    ticker = ticker,
                    state = investimentos,
                    onVoltar = { navController.popBackStack() }
                )
            }

            composable(Rotas.PERFIL) {
                ProfileScreen(onAbrirCartoes = { navController.navigate(Rotas.CARTAO) })
            }

            composable(Rotas.CARTAO) {
                TelaCartao(
                    state = cartao,
                    banco = banco,
                    onAbrirDetalhe = { id -> navController.navigate(Rotas.detalheTransacao(id)) },
                    onVoltar = { navController.popBackStack() }
                )
            }

            composable(
                route = Rotas.DETALHE_TRANSACAO,
                arguments = listOf(navArgument(Rotas.ARG_ID) { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt(Rotas.ARG_ID) ?: -1
                TelaDetalheTransacao(
                    id = id,
                    state = cartao,
                    onVoltar = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun NavController.irParaAba(rota: String) {
    if (currentDestination?.route == rota) return

    if (popBackStack(rota, inclusive = false)) return
    navigate(rota) {
        popUpTo(graph.findStartDestination().id)
        launchSingleTop = true
    }
}
