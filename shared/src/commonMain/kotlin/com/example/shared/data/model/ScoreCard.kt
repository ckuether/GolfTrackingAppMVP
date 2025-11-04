package com.example.shared.data.model

import com.example.shared.platform.getCurrentTimeMillis
import kotlinx.serialization.Serializable
import kotlin.collections.component2
import kotlin.random.Random

@Serializable
data class ScoreCard(
    val roundId: Long = Random.nextLong(1000000L, 9999999L),
    val playerId: Long = 0L,
    val courseId: Long = 0L,
    val courseName: String = "",
    val courseParMap: Map<Int, Int> = mapOf(),
    val holeStatsMap: Map<Int, HoleStats> = mapOf(),
    val roundInProgress: Boolean = true,
    val createdTimestamp: Long = getCurrentTimeMillis(),
    val lastUpdatedTimestamp: Long = getCurrentTimeMillis()
) {
    
    val holesPlayed: Int
        get() = holeStatsMap.size

    val scores: Map<Int, Int?>
        get() = holeStatsMap.mapValues { it.value.score }

    val totalPar: Int
        get() = courseParMap.values.sum()
    
    val completedHolesPar: Int
        get() = holeStatsMap.keys.mapNotNull { holeNumber ->
            courseParMap[holeNumber]
        }.sum()

    val pars: Int
        get() = holeStatsMap.count { (holeNumber, holeStats) ->
            holeStats.score != null && courseParMap[holeNumber] == holeStats.score
        }
    
    val birdies: Int
        get() = holeStatsMap.count { (holeNumber, holeStats) ->
            holeStats.score != null && courseParMap[holeNumber]?.let { par -> holeStats.score == par - 1 } == true
        }
    
    val bogeys: Int
        get() = holeStatsMap.count { (holeNumber, holeStats) ->
            courseParMap[holeNumber]?.let { par -> holeStats.score == par + 1 } == true
        }
    
    val toPar: Int
        get() = totalScore - completedHolesPar

    val totalScore: Int
        get() = scores.values.filterNotNull().sum()

    fun getHoleScore(holeNumber: Int): Int? {
        return holeStatsMap[holeNumber]?.score
    }
}