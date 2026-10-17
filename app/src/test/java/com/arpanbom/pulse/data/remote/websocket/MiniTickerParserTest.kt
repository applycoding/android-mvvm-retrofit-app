package com.arpanbom.pulse.data.remote.websocket

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MiniTickerParserTest {
    private val parser = MiniTickerParser()

    @Test
    fun parsesCombinedStreamPayload() {
        val quote = parser.parse(
            """
            {"stream":"btcusdt@miniTicker","data":{"e":"24hrMiniTicker","E":1710000000000,"s":"BTCUSDT","c":"65000.5"}}
            """.trimIndent(),
        )
        assertEquals("BTC", quote?.symbol)
        assertEquals(65000.5, quote?.price ?: 0.0, 0.001)
        assertEquals(1710000000000L, quote?.eventTimeEpochMs)
    }

    @Test
    fun parsesRawPayload() {
        val quote = parser.parse("""{"e":"24hrMiniTicker","E":9,"s":"ethusdt","c":"3100"}""")
        assertEquals("ETH", quote?.symbol)
        assertEquals(3100.0, quote?.price ?: 0.0, 0.001)
    }

    @Test
    fun ignoresOtherEventsMalformedPayloadsAndNonUsdtSymbols() {
        assertNull(parser.parse("""{"e":"trade","s":"BTCUSDT","c":"1"}"""))
        assertNull(parser.parse("not-json"))
        assertNull(parser.parse("""{"e":"24hrMiniTicker","s":"BTCUSD","c":"1"}"""))
        assertNull(parser.parse("""{"e":"24hrMiniTicker","s":"BTCUSDT","c":"nope"}"""))
    }
}
