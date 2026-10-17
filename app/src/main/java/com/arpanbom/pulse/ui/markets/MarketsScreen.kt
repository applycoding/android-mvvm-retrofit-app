package com.arpanbom.pulse.ui.markets

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arpanbom.pulse.R
import com.arpanbom.pulse.ui.components.LiveBadge
import com.arpanbom.pulse.ui.components.MarketRowCard
import com.arpanbom.pulse.ui.components.StatusBanner

@Composable
fun MarketsRoute(
    onCoinClick: (String) -> Unit,
    viewModel: MarketsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    MarketsScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onRefresh = viewModel::refresh,
        onCoinClick = onCoinClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketsScreen(
    state: MarketsUiState,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onCoinClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            ) {
                MarketsHeader(
                    isLive = state.isLive,
                    modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
                )
                SearchField(
                    query = state.query,
                    onQueryChange = onQueryChange,
                )
                StatusBanner(
                    usedSampleData = state.usedSampleData,
                    isOnline = state.isOnline,
                    hasLoaded = state.hasLoaded,
                    error = state.error,
                    onRetry = onRefresh,
                    modifier = Modifier.padding(top = 12.dp),
                )
                MarketList(
                    state = state,
                    onCoinClick = onCoinClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun MarketsHeader(isLive: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text(
                text = stringResource(R.string.markets_kicker).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = stringResource(R.string.markets_title),
                style = MaterialTheme.typography.headlineLarge,
            )
        }
        if (isLive) {
            LiveBadge(modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.clear_search),
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun MarketList(
    state: MarketsUiState,
    onCoinClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        !state.hasLoaded && state.coins.isEmpty() -> LoadingPane(modifier)
        state.coins.isEmpty() && state.query.isNotBlank() -> MessagePane(
            message = stringResource(R.string.empty_search),
            modifier = modifier,
        )
        state.coins.isEmpty() -> MessagePane(
            message = stringResource(R.string.empty_markets),
            modifier = modifier,
        )
        else -> LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(items = state.coins, key = { it.coin.id }) { row ->
                MarketRowCard(row = row, onClick = { onCoinClick(row.coin.id) })
            }
        }
    }
}

@Composable
private fun LoadingPane(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = stringResource(R.string.loading),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessagePane(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
