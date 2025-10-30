package com.example.shared.data.model

import com.example.shared.platform.getCurrentTimeMillis
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class ScoreCard(
    val roundId: Long = Random.nextLong(1000000L, 9999999L),
    val courseId: Long = 0L,
    val courseName: String = "",
    val playerId: Long = 0L,
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
    
    val pars: Int
        get() = scores.count { it == 4 } // Assuming par 4 for simplicity
    
    val birdies: Int
        get() = scores.count { it == 3 } // Assuming par 4, so 3 is birdie
    
    val bogeys: Int
        get() = scores.count { it == 5 } // Assuming par 4, so 5 is bogey
    
    val toPar: Int
        get() = totalScore - (holesPlayed * 4) // Assuming all holes are par 4

    fun getHoleScore(holeNumber: Int): Int?{
        return scorecard[holeNumber]
    }
}