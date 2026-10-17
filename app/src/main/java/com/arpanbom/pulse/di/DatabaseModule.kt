package com.arpanbom.pulse.di

import android.content.Context
import androidx.room.Room
import com.arpanbom.pulse.data.local.PulseDatabase
import com.arpanbom.pulse.data.local.dao.MarketDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PulseDatabase =
        Room.databaseBuilder(context, PulseDatabase::class.java, "pulse.db").build()

    @Provides
    fun provideMarketDao(database: PulseDatabase): MarketDao = database.marketDao()
}
