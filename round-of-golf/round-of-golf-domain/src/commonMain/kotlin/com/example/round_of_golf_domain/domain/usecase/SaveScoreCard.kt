package com.example.round_of_golf_domain.domain.usecase

import com.example.shared.data.dao.ScoreCardDao
import com.example.shared.data.entity.toEntity
import com.example.shared.data.model.ScoreCard
import com.example.shared.platform.Logger

class SaveScoreCard(
    private val scoreCardDao: ScoreCardDao,
    private val logger: Logger
) {
    companion object Companion {
        private const val TAG = "SaveScoreCardUseCase"
    }
    
    suspend operator fun invoke(scoreCard: ScoreCard): Result<Unit> {
        return try {
            val scoreCardEntity = scoreCard.toEntity()
            scoreCardDao.insertScoreCard(scoreCardEntity)
            logger.debug(TAG, "ScoreCard saved to database successfully for round ${scoreCard.roundId}")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(TAG, "Failed to save scorecard to database", e)
            Result.failure(e)
        }
    }
}