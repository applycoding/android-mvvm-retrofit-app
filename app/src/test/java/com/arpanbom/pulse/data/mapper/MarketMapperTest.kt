package com.arpanbom.pulse.data.mapper

import com.arpanbom.pulse.data.remote.dto.MarketDto
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MarketMapperTest {
    @Test
    fun mapsMissingNumbersToZeroAndLowercasesSymbol() {
        val entity = MarketDto(
            id = "example",
            symbol = "ABC",
            name = "Example",
            image = null,
            currentPrice = null,
            marketCapRank = null,
            lastUpdated = null,
        ).toEntity(nowEpochMs = 42L)

        assertEquals("abc", entity.symbol)
        assertEquals(0.0, entity.currentPrice, 0.0)
        assertEquals(0, entity.rank)
        assertEquals("", entity.imageUrl)
        assertEquals(42L, entity.lastUpdatedEpochMs)
    }

    @Test
    fun parsesCoinGeckoTimestamp() {
        val raw = "2026-10-16T12:00:00.000Z"
        val entity = MarketDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            currentPrice = 10.0,
            lastUpdated = raw,
        ).toEntity()

        assertEquals(Instant.parse(raw).toEpochMilli(), entity.lastUpdatedEpochMs)
        assertEquals("bitcoin", entity.toDomain().id)
    }

    @Test
    fun rejectsBlankAndGarbageTimestamps() {
        assertNull(parseIsoEpochMillis(null))
        assertNull(parseIsoEpochMillis(" "))
        assertNull(parseIsoEpochMillis("yesterday"))
    }
}
