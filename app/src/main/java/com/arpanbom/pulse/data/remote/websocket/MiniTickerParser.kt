package com.arpanbom.pulse.data.remote.websocket

import com.arpanbom.pulse.domain.model.LiveQuote
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

class MiniTickerParser @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): LiveQuote? {
        val root = runCatching { json.parseToJsonElement(text).jsonObject }.getOrNull() ?: return null
        val payload = root["data"]?.let { element ->
            runCatching { element.jsonObject }.getOrNull()
        } ?: root
        val event = payload.string("e") ?: return null
        if (event != MINI_TICKER_EVENT) return null
        val rawSymbol = payload.string("s") ?: return null
        val upper = rawSymbol.uppercase()
        if (!upper.endsWith(USDT_SUFFIX)) return null
        val asset = upper.removeSuffix(USDT_SUFFIX)
        if (asset.isBlank()) return null
        val price = payload.string("c")?.toDoubleOrNull() ?: return null
        val time = payload.long("E") ?: 0L
        return LiveQuote(symbol = asset, price = price, eventTimeEpochMs = time)
    }

    private fun JsonObject.string(key: String): String? = runCatching {
        this[key]?.jsonPrimitive?.contentOrNull
    }.getOrNull()

    private fun JsonObject.long(key: String): Long? = runCatching {
        this[key]?.jsonPrimitive?.longOrNull
    }.getOrNull()

    private companion object {
        const val MINI_TICKER_EVENT = "24hrMiniTicker"
        const val USDT_SUFFIX = "USDT"
    }
}
