package com.example.round_of_golf_domain.data.model

import com.example.round_of_golf_domain.data.entity.RoundOfGolfEventEntity
import com.example.shared.data.model.GolfClubType
import com.example.shared.data.model.Location
import com.example.shared.platform.getCurrentTimeMillis
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
open class RoundOfGolfEvent {
    val timestamp: Long = getCurrentTimeMillis()
}

@Serializable
data class LocationUpdated(
    val location: Location
): RoundOfGolfEvent()

@Serializable
data class ShotTracked(
    val holeNumber: Int,
    val club: GolfClubType,
    val location: Location,
): RoundOfGolfEvent()

@Serializable
data class HoleChanged(
    val holeNumber: Int
): RoundOfGolfEvent()

@Serializable
class FinishRound: RoundOfGolfEvent()

fun RoundOfGolfEvent.toEntity(
    roundId: Long,
    playerId: Long,
): RoundOfGolfEventEntity {
    
    val eventData = when(this) {
        is LocationUpdated -> Json.encodeToString(this)
        is ShotTracked -> Json.encodeToString(this)
        is HoleChanged -> Json.encodeToString(this)
        is FinishRound -> Json.encodeToString(this)
        else -> "{}"
    }
    
    return RoundOfGolfEventEntity(
        timestamp = timestamp,
        roundId = roundId,
        eventType = eventType.name,
        eventData = eventData,
        playerId = playerId
    )
}

val RoundOfGolfEvent.eventType: EventType
    get() = when(this){
        is LocationUpdated -> EventType.LOCATION_UPDATED
        is ShotTracked -> EventType.SHOT_TRACKED
        is HoleChanged -> EventType.HOLE_CHANGED
        is FinishRound -> EventType.FINISH_ROUND
        else -> EventType.ERROR
    }

@Serializable
enum class EventType {
    LOCATION_UPDATED,
    SHOT_TRACKED,
    HOLE_CHANGED,
    FINISH_ROUND,
    ERROR
}