package com.example.shared.usecase

import com.example.shared.data.model.Course
import com.example.shared.data.repository.GolfCourseRepository
import com.example.shared.platform.Logger

class LoadGolfCourse(
    private val golfCourseRepository: GolfCourseRepository,
    private val logger: Logger
) {
    companion object Companion {
        private const val TAG = "LoadGolfCourseUseCase"
    }

    suspend operator fun invoke(): Result<Course?> {
        return try {
            val course = golfCourseRepository.loadGolfCourse()
            logger.debug(TAG, "Golf course loaded: ${course?.name}")
            Result.success(course)
        } catch (e: Exception) {
            logger.error(TAG, "Failed to load golf course", e)
            Result.failure(e)
        }
    }
}