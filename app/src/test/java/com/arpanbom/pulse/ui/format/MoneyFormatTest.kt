package com.arpanbom.pulse.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatTest {
    @Test
    fun formatsDollarsAndSubDollarPrices() {
        assertEquals("$65,000.00", MoneyFormat.usd(65_000.0))
        assertEquals("$0.00", MoneyFormat.usd(0.0))
        assertEquals("$0.1234", MoneyFormat.usd(0.1234))
    }

    @Test
    fun compactsLargeAmounts() {
        assertEquals("$1.3T", MoneyFormat.compactUsd(1_270_000_000_000.0))
        assertEquals("$1.5B", MoneyFormat.compactUsd(1_500_000_000.0))
        assertEquals("$840M", MoneyFormat.compactUsd(840_000_000.0))
        assertEquals("$12.4K", MoneyFormat.compactUsd(12_400.0))
    }

    @Test
    fun signsPercentChanges() {
        assertEquals("+1.80%", MoneyFormat.signedPercent(1.8))
        assertEquals("-0.60%", MoneyFormat.signedPercent(-0.6))
        assertEquals("0.00%", MoneyFormat.signedPercent(0.0))
    }
}
