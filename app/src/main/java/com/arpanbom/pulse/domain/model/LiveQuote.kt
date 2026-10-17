package com.arpanbom.pulse.domain.model

data class LiveQuote(
    val symbol: String,
    val price: Double,
    val eventTimeEpochMs: Long,
)
