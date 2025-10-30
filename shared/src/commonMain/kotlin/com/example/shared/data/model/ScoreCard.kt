package com.example.shared.data.model

import com.example.shared.platform.getCurrentTimeMillis
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class ScoreCard(
    val roundId: Long = Random.nextLong(1000000L, 9999999L),
    val playerId: Long = 0L,
    val courseId: Long = 0L,
    val courseName: String = "",
    val coursePar: Map<Int, Int> = mapOf(),
    val scorecard: Map<Int, Int?> = mapOf(),
    val roundInProgress: Boolean = true,
    val createdTimestamp: Long = getCurrentTimeMillis(),
    val lastUpdatedTimestamp: Long = getCurrentTimeMillis()
) {
    val totalScore: Int
        get() = scorecard.values.filterNotNull().sum()
    
    val holesPlayed: Int
        get() = scorecard.size
    
    val scores: List<Int>
        get() = scorecard.values.filterNotNull()

    val totalPar: Int
        get() = coursePar.values.sum()
    
    val completedHolesPar: Int
        get() = scorecard.keys.mapNotNull { holeNumber -> 
            coursePar[holeNumber] 
        }.sum()

    val pars: Int
        get() = scorecard.count { (holeNumber, score) ->
            score != null && coursePar[holeNumber] == score
        }
    
    val birdies: Int
        get() = scorecard.count { (holeNumber, score) ->
            score != null && coursePar[holeNumber]?.let { par -> score == par - 1 } == true
        }
    
    val bogeys: Int
        get() = scorecard.count { (holeNumber, score) ->
            score != null && coursePar[holeNumber]?.let { par -> score == par + 1 } == true
        }
    
    val toPar: Int
        get() = totalScore - completedHolesPar

    fun getHoleScore(holeNumber: Int): Int?{
        return scorecard[holeNumber]
    }
}