package com.arpanbom.pulse.ui.format

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object MoneyFormat {
    fun usd(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        val digits = if (abs(amount) >= 1.0 || amount == 0.0) 2 else 4
        format.minimumFractionDigits = 2
        format.maximumFractionDigits = digits
        return format.format(amount)
    }

    fun compactUsd(amount: Double): String {
        val absolute = abs(amount)
        val sign = if (amount < 0) "-" else ""
        val (divisor, suffix) = when {
            absolute >= 1_000_000_000_000 -> 1_000_000_000_000.0 to "T"
            absolute >= 1_000_000_000 -> 1_000_000_000.0 to "B"
            absolute >= 1_000_000 -> 1_000_000.0 to "M"
            absolute >= 1_000 -> 1_000.0 to "K"
            else -> return usd(amount)
        }
        return sign + "$" + trim(absolute / divisor) + suffix
    }

    fun signedPercent(value: Double): String {
        val body = String.format(Locale.US, "%.2f", abs(value))
        return when {
            value > 0.0 -> "+$body%"
            value < 0.0 -> "-$body%"
            else -> "0.00%"
        }
    }

    private fun trim(value: Double): String =
        String.format(Locale.US, "%.1f", value).removeSuffix(".0")
}
