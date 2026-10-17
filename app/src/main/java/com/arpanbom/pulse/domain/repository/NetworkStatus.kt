package com.arpanbom.pulse.domain.repository

import kotlinx.coroutines.flow.Flow

interface NetworkStatus {
    fun isOnline(): Flow<Boolean>
}
