package com.example.shared.usecase

import com.example.shared.data.dao.ScoreCardDao
import com.example.shared.data.entity.toScoreCard
import com.example.shared.data.model.ScoreCard
import com.example.shared.platform.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetAllScoreCards(
    private val scoreCardDao: ScoreCardDao,
    private val logger: Logger
) {
    companion object Companion {
        private const val TAG = "GetAllScoreCardsUseCase"
    }

    operator fun invoke(): Flow<List<ScoreCard>> {
        logger.debug(TAG, "Getting all scorecards from database")
        return scoreCardDao.getAllScoreCards()
            .map { entities ->
                entities.map { it.toScoreCard() }
            }
    }
}