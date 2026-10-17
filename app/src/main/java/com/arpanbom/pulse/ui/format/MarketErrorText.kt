package com.arpanbom.pulse.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.arpanbom.pulse.R
import com.arpanbom.pulse.domain.model.MarketError

@Composable
fun MarketError.asMessage(): String = when (this) {
    MarketError.Offline -> stringResource(R.string.error_offline)
    MarketError.RateLimited -> stringResource(R.string.error_rate_limited)
    MarketError.Server -> stringResource(R.string.error_server)
    MarketError.Unknown -> stringResource(R.string.error_unknown)
    else -> {
        val unexpected: Nothing = this
        unexpected
    }
}
