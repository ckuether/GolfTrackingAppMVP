package com.example.round_of_golf_domain.data.repository

import com.example.round_of_golf_domain.data.model.RoundOfGolfEvent
import kotlinx.coroutines.flow.Flow

interface RoundOfGolfEventLocalRepository {

    /**
     * Insert a single event
     */
    suspend fun insertEvent(
        event: RoundOfGolfEvent,
        roundId: Long,
        playerId: Long,
    )

    /**
     * Delete all events for a round
     */
    suspend fun deleteEventsForRound(roundId: Long)

    /**
     * Get count of events for a round
     */
    suspend fun getEventCountForRound(roundId: Long): Int

    /**
     * Get all round IDs that have events
     */
    fun getAllRoundIds(): Flow<List<Long>>
}