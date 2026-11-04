package com.example.cupbank.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import com.example.cupbank.viewmodel.BancoViewModel
import com.example.cupbank.viewmodel.CartaoViewModel
import com.example.cupbank.viewmodel.CupBankViewModelFactory
import com.example.cupbank.viewmodel.InvestimentosViewModel
import com.example.cupbank.ui.cartao.TelaCartao
import com.example.cupbank.ui.cartao.TelaDetalheTransacao
import com.example.cupbank.ui.extrato.ExtratoScreen
import com.example.cupbank.ui.home.BankAppScreen
import com.example.cupbank.ui.investimentos.DetalheAtivoScreen
import com.example.cupbank.ui.investimentos.InvestirScreen
import com.example.cupbank.ui.perfil.ProfileScreen
import com.example.cupbank.ui.pix.TelaTransferenciaPix
import com.example.cupbank.ui.theme.CupBankBackground

@Composable
fun CupBankApp() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val factory = remember { CupBankViewModelFactory.criar(context) }
    val bancoViewModel: BancoViewModel = viewModel(factory = factory)
    val cartaoViewModel: CartaoViewModel = viewModel(factory = factory)
    val investimentosViewModel: InvestimentosViewModel = viewModel(factory = factory)

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
                    investimentosViewModel.abrirNovoAtivo()
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
                    viewModel = bancoViewModel,
                    onAbrirPix = { navController.navigate(Rotas.PIX) },
                    onAbrirCartao = { navController.navigate(Rotas.CARTAO) },
                    onAbrirExtrato = { navController.irParaAba(Rotas.EXTRATO) }
                )
            }

            composable(Rotas.PIX) {
                TelaTransferenciaPix(
                    viewModel = bancoViewModel,
                    onVoltar = { navController.popBackStack() }
                )
            }

            composable(Rotas.EXTRATO) { ExtratoScreen(viewModel = bancoViewModel) }

            composable(Rotas.INVESTIR) {
                InvestirScreen(
                    viewModel = investimentosViewModel,
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
                    viewModel = investimentosViewModel,
                    onVoltar = { navController.popBackStack() }
                )
            }

            composable(Rotas.PERFIL) {
                ProfileScreen(onAbrirCartoes = { navController.navigate(Rotas.CARTAO) })
            }

            composable(Rotas.CARTAO) {
                TelaCartao(
                    viewModel = cartaoViewModel,
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
                    viewModel = cartaoViewModel,
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
