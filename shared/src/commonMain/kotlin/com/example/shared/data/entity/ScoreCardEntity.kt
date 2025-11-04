package com.example.shared.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.shared.data.model.HoleStats
import com.example.shared.data.model.ScoreCard
import kotlinx.serialization.json.Json

@Entity(tableName = "score_cards")
data class ScoreCardEntity(
    @PrimaryKey
    val roundId: Long,
    val courseId: Long,
    val courseName: String,
    val courseParJson: String, // Serialized coursePar map
    val playerId: Long,
    val scorecardJson: String, // Serialized scorecard map
    val roundInProgress: Boolean = true,
    val createdTimestamp: Long,
    val lastUpdatedTimestamp: Long
)

fun ScoreCardEntity.toScoreCard(): ScoreCard {
    val scorecardMap = Json.decodeFromString<Map<Int, HoleStats>>(scorecardJson)
    val courseParMap = Json.decodeFromString<Map<Int, Int>>(courseParJson)
    return ScoreCard(
        roundId = roundId,
        playerId = playerId,
        courseId = courseId,
        courseName = courseName,
        courseParMap = courseParMap,
        holeStatsMap = scorecardMap,
        roundInProgress = roundInProgress,
        createdTimestamp = createdTimestamp,
        lastUpdatedTimestamp = lastUpdatedTimestamp
    )
}

fun ScoreCard.toEntity(): ScoreCardEntity {
    return ScoreCardEntity(
        roundId = roundId,
        playerId = playerId,
        courseId = courseId,
        courseName = courseName,
        courseParJson = Json.encodeToString(courseParMap),
        scorecardJson = Json.encodeToString(holeStatsMap),
        roundInProgress = roundInProgress,
        createdTimestamp = createdTimestamp,
        lastUpdatedTimestamp = lastUpdatedTimestamp
    )
}