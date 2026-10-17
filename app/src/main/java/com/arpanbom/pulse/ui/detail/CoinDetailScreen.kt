package com.arpanbom.pulse.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arpanbom.pulse.R
import com.arpanbom.pulse.domain.model.MarketCoin
import com.arpanbom.pulse.ui.components.LiveBadge
import com.arpanbom.pulse.ui.components.StatTile
import com.arpanbom.pulse.ui.format.MoneyFormat
import com.arpanbom.pulse.ui.theme.IbmPlexMono
import com.arpanbom.pulse.ui.theme.LocalPulseTones

@Composable
fun CoinDetailRoute(
    onBack: () -> Unit,
    viewModel: CoinDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CoinDetailScreen(state = state, onBack = onBack)
}

@Composable
fun CoinDetailScreen(
    state: CoinDetailUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.padding(top = 4.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                state.coin == null || state.displayPrice == null -> MissingCoin()
                else -> CoinBody(coin = state.coin, price = state.displayPrice, isLive = state.isLive)
            }
        }
    }
}

@Composable
private fun CoinBody(coin: MarketCoin, price: Double, isLive: Boolean) {
    val tones = LocalPulseTones.current
    val changeColor = when {
        coin.priceChangePercent24h > 0 -> tones.up
        coin.priceChangePercent24h < 0 -> tones.down
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text(
            text = coin.symbol.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = coin.name,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 4.dp),
        )
        if (coin.rank > 0) {
            Text(
                text = stringResource(R.string.rank) + " " + coin.rank,
                modifier = Modifier.padding(top = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Row(
            modifier = Modifier.padding(top = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = MoneyFormat.usd(price),
                fontFamily = IbmPlexMono,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.headlineLarge,
            )
            if (isLive) LiveBadge()
        }
        Text(
            text = MoneyFormat.signedPercent(coin.priceChangePercent24h),
            modifier = Modifier.padding(top = 6.dp),
            fontFamily = IbmPlexMono,
            color = changeColor,
            style = MaterialTheme.typography.titleMedium,
        )
        Column(
            modifier = Modifier.padding(top = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    label = stringResource(R.string.market_cap),
                    value = MoneyFormat.compactUsd(coin.marketCap),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = stringResource(R.string.volume),
                    value = MoneyFormat.compactUsd(coin.volume24h),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    label = stringResource(R.string.high_24h),
                    value = MoneyFormat.usd(coin.high24h),
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    label = stringResource(R.string.low_24h),
                    value = MoneyFormat.usd(coin.low24h),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MissingCoin() {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = stringResource(R.string.not_in_cache_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.not_in_cache_body),
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
