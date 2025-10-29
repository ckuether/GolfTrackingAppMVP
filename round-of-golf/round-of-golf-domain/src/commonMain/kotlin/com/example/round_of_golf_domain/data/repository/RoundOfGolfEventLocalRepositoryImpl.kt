package com.example.round_of_golf_domain.data.repository

import com.example.round_of_golf_domain.data.dao.RoundOfGolfEventDao
import com.example.round_of_golf_domain.data.model.RoundOfGolfEvent
import com.example.round_of_golf_domain.data.model.toEntity
import kotlinx.coroutines.flow.Flow

class RoundOfGolfEventLocalRepositoryImpl(
    private val eventDao: RoundOfGolfEventDao
) : RoundOfGolfEventLocalRepository {

    override suspend fun insertEvent(
        event: RoundOfGolfEvent,
        roundId: Long,
        playerId: Long,
    ) {
        val eventEntity = event.toEntity(roundId = roundId, playerId = playerId)
        eventDao.insertEvent(eventEntity)
    }

    override suspend fun deleteEventsForRound(roundId: Long) {
        eventDao.deleteEventsForRound(roundId)
    }

    override suspend fun getEventCountForRound(roundId: Long): Int {
        return eventDao.getEventCountForRound(roundId)
    }

    override fun getAllRoundIds(): Flow<List<Long>> {
        return eventDao.getAllRoundIds()
    }
}