package com.example.round_of_golf_domain.data.model

import com.example.round_of_golf_domain.data.entity.RoundOfGolfEventEntity
import com.example.shared.data.model.GolfClubType
import com.example.shared.data.model.Location
import com.example.shared.platform.getCurrentTimeMillis
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed interface RoundOfGolfEvent {
    val timestamp: Long

    val eventType
        get() = when (this) {
            is LocationUpdated -> EventType.LOCATION_UPDATED
            is ShotTracked -> EventType.SHOT_TRACKED
            is HoleChanged -> EventType.HOLE_CHANGED
            is FinishRound -> EventType.FINISH_ROUND
        }

    @Serializable
    data class LocationUpdated(
        override val timestamp: Long = getCurrentTimeMillis(),
        val location: Location,
    ): RoundOfGolfEvent

    @Serializable
    data class ShotTracked(
        override val timestamp: Long = getCurrentTimeMillis(),
        val holeNumber: Int,
        val club: GolfClubType,
        val location: Location
    ): RoundOfGolfEvent

    @Serializable
    data class HoleChanged(
        override val timestamp: Long = getCurrentTimeMillis()
    ): RoundOfGolfEvent

    @Serializable
    data class FinishRound(
        override val timestamp: Long = getCurrentTimeMillis()
    ): RoundOfGolfEvent
}

fun RoundOfGolfEvent.toEntity(
    roundId: Long,
    playerId: Long,
): RoundOfGolfEventEntity {
    val eventData = Json.encodeToString(this)

    return RoundOfGolfEventEntity(
        timestamp = this.timestamp,
        roundId = roundId,
        eventType = eventType,
        eventData = eventData,
        playerId = playerId
    )
}

object EventType {
    const val LOCATION_UPDATED = "LocationUpdated"
    const val SHOT_TRACKED = "ShotTracked"
    const val HOLE_CHANGED = "HoleChanged"
    const val FINISH_ROUND = "FinishRound"
}