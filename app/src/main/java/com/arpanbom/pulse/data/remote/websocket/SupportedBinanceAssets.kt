package com.arpanbom.pulse.data.remote.websocket

/**
 * USDT pairs that Binance's public combined stream is expected to serve.
 * Unknown symbols are left off the socket so one bad name cannot close the connection.
 */
internal val SupportedBinanceAssets: Set<String> = setOf(
    "BTC", "ETH", "SOL", "BNB", "XRP", "ADA", "DOGE", "AVAX", "DOT", "LINK",
    "TRX", "TON", "SHIB", "LTC", "BCH", "UNI", "ATOM", "XLM", "NEAR", "APT",
    "FIL", "ARB", "OP", "SUI", "HBAR", "INJ", "AAVE", "SEI", "TIA", "PEPE",
)
