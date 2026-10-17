package com.arpanbom.pulse.domain.model

enum class MarketError {
    Offline,
    RateLimited,
    Server,
    Unknown,
}

sealed interface RefreshResult {
    data object Success : RefreshResult

    data class Failed(
        val error: MarketError,
        val usedSampleData: Boolean,
    ) : RefreshResult
}
