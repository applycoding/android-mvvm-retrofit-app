package com.arpanbom.pulse.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arpanbom.pulse.data.local.dao.MarketDao
import com.arpanbom.pulse.data.local.entity.MarketEntity

@Database(
    entities = [MarketEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun marketDao(): MarketDao
}
