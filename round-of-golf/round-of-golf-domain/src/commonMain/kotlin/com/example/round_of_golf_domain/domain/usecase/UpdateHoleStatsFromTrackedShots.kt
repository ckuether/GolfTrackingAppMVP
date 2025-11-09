package com.example.round_of_golf_domain.domain.usecase

import com.example.shared.data.model.GolfClubType
import com.example.shared.data.model.HoleStats
import com.example.shared.data.model.ScoreCard
import com.example.shared.platform.Logger

class UpdateHoleStatsFromTrackedShots(
    private val getTrackedShotsForHole: GetTrackedShotsForHole,
    private val logger: Logger
) {
    companion object Companion {
        private const val TAG = "UpdateHoleStatsFromTrackedShotsUseCase"
    }
    
    suspend operator fun invoke(
        scoreCard: ScoreCard,
        holeNumber: Int
    ): Result<ScoreCard> {
        return try {
            val trackedShots = getTrackedShotsForHole.getShotsForHoleSuspend(scoreCard.roundId, holeNumber)
            val totalShotsTracked = trackedShots.size
            val putterShotsTracked = trackedShots.count { it.club == GolfClubType.Putter }
            
            val currentScore = scoreCard.getHoleScore(holeNumber)
            val currentPutts = scoreCard.getHolePutts(holeNumber)
            
            // Update score if null or less than total shots tracked
            val newScore = if (currentScore == null || currentScore < totalShotsTracked) {
                totalShotsTracked
            } else currentScore
            
            // Update putts if null or less than putter shots tracked  
            val newPutts = if (putterShotsTracked > 0) {
                if (currentPutts == null || currentPutts < putterShotsTracked) {
                    putterShotsTracked
                } else currentPutts
            } else currentPutts
            
            // Only update if there's a change
            if (newScore != currentScore || newPutts != currentPutts) {
                val holeStats = scoreCard.holeStatsMap.toMutableMap()
                val holeStatsForHole = holeStats.getOrPut(holeNumber) { HoleStats() }
                holeStatsForHole.score = newScore
                holeStatsForHole.putts = newPutts
                
                val updatedScoreCard = scoreCard.copy(holeStatsMap = holeStats)
                
                logger.debug(TAG, "Updated hole $holeNumber: score $currentScore -> $newScore, putts $currentPutts -> $newPutts")
                Result.success(updatedScoreCard)
            } else {
                logger.debug(TAG, "No updates needed for hole $holeNumber")
                Result.success(scoreCard)
            }
        } catch (e: Exception) {
            logger.error(TAG, "Failed to update hole stats from tracked shots for hole $holeNumber", e)
            Result.failure(e)
        }
    }
}