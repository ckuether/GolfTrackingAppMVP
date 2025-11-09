package com.example.shared.usecase

import com.example.shared.data.model.Player
import com.example.shared.data.repository.UserRepository
import com.example.shared.platform.Logger

class LoadCurrentUser(
    private val userRepository: UserRepository,
    private val logger: Logger
) {
    companion object Companion {
        private const val TAG = "LoadCurrentUserUseCase"
    }

    suspend operator fun invoke(): Result<Player> {
        return try {
            val player = userRepository.getCurrentUser()
            if (player != null) {
                logger.debug(TAG, "Current player loaded: ${player.name} (ID: ${player.id})")
                Result.success(player)
            } else {
                logger.warn(TAG, "No user data found, returning default player")
                val defaultPlayer = Player(name = "Player")
                Result.success(defaultPlayer)
            }
        } catch (e: Exception) {
            logger.error(TAG, "Failed to load current user, returning default player", e)
            val defaultPlayer = Player(name = "Player")
            Result.success(defaultPlayer)
        }
    }
}