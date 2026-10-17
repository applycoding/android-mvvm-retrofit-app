package com.arpanbom.pulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arpanbom.pulse.R
import com.arpanbom.pulse.domain.model.MarketCoin
import com.arpanbom.pulse.domain.model.MarketError
import com.arpanbom.pulse.ui.format.MoneyFormat
import com.arpanbom.pulse.ui.format.asMessage
import com.arpanbom.pulse.ui.markets.MarketRow
import com.arpanbom.pulse.ui.theme.IbmPlexMono
import com.arpanbom.pulse.ui.theme.LocalPulseTones

@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    val tones = LocalPulseTones.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(tones.live.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(tones.live),
        )
        Text(
            text = stringResource(R.string.live).uppercase(),
            color = tones.live,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
fun StatusBanner(
    usedSampleData: Boolean,
    isOnline: Boolean,
    hasLoaded: Boolean,
    error: MarketError?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = when {
        usedSampleData -> stringResource(R.string.sample_banner)
        !isOnline && hasLoaded -> stringResource(R.string.offline_cached)
        error != null -> error.asMessage()
        else -> null
    } ?: return
    val detail = if (usedSampleData) error?.asMessage() else null
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 8.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (detail != null) {
            Text(
                text = detail,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onRetry) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(R.string.retry),
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
fun MarketRowCard(
    row: MarketRow,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tones = LocalPulseTones.current
    val coin = row.coin
    val changeColor = when {
        coin.priceChangePercent24h > 0 -> tones.up
        coin.priceChangePercent24h < 0 -> tones.down
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, tones.line, RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RankMark(rank = coin.rank)
        CoinAvatar(coin = coin)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = coin.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = coin.symbol.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (row.isLive) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(tones.live),
                    )
                }
                Text(
                    text = MoneyFormat.usd(row.displayPrice),
                    fontFamily = IbmPlexMono,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = MoneyFormat.signedPercent(coin.priceChangePercent24h),
                fontFamily = IbmPlexMono,
                color = changeColor,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            modifier = Modifier.padding(top = 6.dp),
            fontFamily = IbmPlexMono,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun RankMark(rank: Int) {
    Text(
        text = if (rank > 0) rank.toString() else "–",
        modifier = Modifier.size(width = 28.dp, height = 20.dp),
        fontFamily = IbmPlexMono,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun CoinAvatar(coin: MarketCoin) {
    if (coin.imageUrl.isBlank()) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = coin.symbol.take(1).uppercase(),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    } else {
        AsyncImage(
            model = coin.imageUrl,
            contentDescription = stringResource(R.string.coin_image_cd, coin.name),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    }
}
