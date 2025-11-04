package com.example.round_of_golf_domain.domain.usecase

import com.example.round_of_golf_domain.data.model.EventType
import com.example.round_of_golf_domain.data.model.HoleChanged
import com.example.round_of_golf_domain.data.repository.RoundOfGolfEventLocalRepository

/**
 * Use case for tracking hole changed events
 * Single Responsibility: Check if hole changed event exists, if not create one
 */
class TrackHoleChangedEventUseCase(
    private val eventRepository: RoundOfGolfEventLocalRepository
) {
    
    /**
     * Track a hole changed event only if it doesn't already exist for the specific hole
     */
    suspend fun execute(
        roundId: Long,
        playerId: Long,
        holeNumber: Int
    ) {
        // Get all HoleChanged events for this round
        val existingHoleChangedEvents = eventRepository.getEventsByType(
            roundId = roundId,
            eventType = EventType.HOLE_CHANGED.name
        )
        
        // Check if any existing event is for this specific hole number
        val hasHoleChangedEventForThisHole = existingHoleChangedEvents.any { eventEntity ->
            // Parse the event data to check if it contains this hole number
            val eventData = eventEntity.eventData
            eventData.contains("\"holeNumber\":$holeNumber")
        }
        
        // Only add HoleChanged event if it doesn't already exist for this hole
        if (!hasHoleChangedEventForThisHole) {
            val holeChangedEvent = HoleChanged(holeNumber = holeNumber)
            eventRepository.insertEvent(
                event = holeChangedEvent,
                roundId = roundId,
                playerId = playerId
            )
        }
    }
}