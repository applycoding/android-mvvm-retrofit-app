package com.arpanbom.pulse.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.arpanbom.pulse.data.local.entity.MarketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketDao {
    @Query(
        """
        SELECT * FROM markets
        ORDER BY CASE WHEN rank <= 0 THEN 1 ELSE 0 END, rank ASC, name ASC
        """,
    )
    fun observeAll(): Flow<List<MarketEntity>>

    @Query("SELECT * FROM markets WHERE id = :id")
    fun observeById(id: String): Flow<MarketEntity?>

    @Query("SELECT COUNT(*) FROM markets")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertAll(markets: List<MarketEntity>)

    @Query("DELETE FROM markets WHERE id NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<String>)

    @Transaction
    suspend fun replaceAll(markets: List<MarketEntity>) {
        upsertAll(markets)
        deleteNotIn(markets.map { it.id })
    }
}
