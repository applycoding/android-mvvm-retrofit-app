package com.arpanbom.pulse.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arpanbom.pulse.ui.detail.CoinDetailRoute
import com.arpanbom.pulse.ui.markets.MarketsRoute

@Composable
fun PulseNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Markets) {
        composable(Markets) {
            MarketsRoute(onCoinClick = { coinId -> navController.navigate(coinRoute(coinId)) })
        }
        composable(
            route = Coin,
            arguments = listOf(navArgument(CoinArg) { type = NavType.StringType }),
        ) {
            CoinDetailRoute(onBack = { navController.popBackStack() })
        }
    }
}

internal const val Markets = "markets"
internal const val CoinArg = "coinId"
internal const val Coin = "coin/{$CoinArg}"

internal fun coinRoute(coinId: String): String = "coin/${Uri.encode(coinId)}"
