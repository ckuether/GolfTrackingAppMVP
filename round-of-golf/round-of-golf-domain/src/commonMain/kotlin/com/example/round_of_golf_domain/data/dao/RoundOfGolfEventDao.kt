package com.example.round_of_golf_domain.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.round_of_golf_domain.data.entity.RoundOfGolfEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundOfGolfEventDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertEvent(event: RoundOfGolfEventEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertEvents(events: List<RoundOfGolfEventEntity>)

    @Query("SELECT * FROM round_of_golf_events WHERE roundId = :roundId ORDER BY timestamp ASC")
    fun getEventsForRound(roundId: Long): Flow<List<RoundOfGolfEventEntity>>

    @Query("SELECT * FROM round_of_golf_events WHERE roundId = :roundId ORDER BY timestamp ASC")
    suspend fun getEventsForRoundSuspend(roundId: Long): List<RoundOfGolfEventEntity>

    @Query("SELECT * FROM round_of_golf_events WHERE roundId = :roundId AND eventType = :eventType ORDER BY timestamp ASC")
    fun getEventsByType(roundId: Long, eventType: String): Flow<List<RoundOfGolfEventEntity>>

    @Query("SELECT * FROM round_of_golf_events WHERE roundId = :roundId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getEventsByTimeRange(roundId: Long, startTime: Long, endTime: Long): Flow<List<RoundOfGolfEventEntity>>

    @Query("DELETE FROM round_of_golf_events WHERE roundId = :roundId")
    suspend fun deleteEventsForRound(roundId: Long)

    @Query("SELECT COUNT(*) FROM round_of_golf_events WHERE roundId = :roundId")
    suspend fun getEventCountForRound(roundId: Long): Int

    @Query("SELECT DISTINCT roundId FROM round_of_golf_events ORDER BY roundId DESC")
    fun getAllRoundIds(): Flow<List<Long>>
}