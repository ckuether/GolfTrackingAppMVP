package com.example.round_of_golf_domain.domain.usecase

import com.example.round_of_golf_domain.data.dao.RoundOfGolfEventDao
import com.example.round_of_golf_domain.data.entity.toEvent
import com.example.round_of_golf_domain.data.model.EventType
import com.example.round_of_golf_domain.data.model.ShotTracked
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTrackedShotsForHole(
    private val eventDao: RoundOfGolfEventDao
) {
    
    operator fun invoke(roundId: Long, holeNumber: Int): Flow<List<ShotTracked>> {
        return eventDao.getEventsByType(roundId, EventType.SHOT_TRACKED.name)
            .map { events ->
                events.mapNotNull { entity ->
                    val event = entity.toEvent()
                    if (event is ShotTracked && event.holeNumber == holeNumber) {
                        event
                    } else {
                        null
                    }
                }
            }
    }
    
    suspend fun getShotsForHoleSuspend(roundId: Long, holeNumber: Int): List<ShotTracked> {
        val events = eventDao.getEventsByTypeSuspend(roundId, EventType.SHOT_TRACKED.name)
        return events.mapNotNull { entity ->
            val event = entity.toEvent()
            if (event is ShotTracked && event.holeNumber == holeNumber) {
                event
            } else {
                null
            }
        }
    }
}