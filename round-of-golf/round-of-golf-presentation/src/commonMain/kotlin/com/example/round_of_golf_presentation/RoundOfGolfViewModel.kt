package com.example.round_of_golf_presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.location_domain.data.model.LocationTrackingUiState
import com.example.location_domain.domain.usecase.CheckLocationPermissionUseCase
import com.example.location_domain.domain.usecase.RequestLocationPermissionUseCase
import com.example.location_domain.domain.usecase.PermissionResult
import com.example.location_domain.domain.service.LocationTrackingService
import com.example.round_of_golf_domain.data.model.LocationUpdated
import com.example.shared.data.model.Course
import com.example.shared.data.model.ScoreCard
import com.example.shared.platform.getCurrentTimeMillis
import com.example.round_of_golf_domain.domain.usecase.SaveScoreCardUseCase
import com.example.round_of_golf_domain.domain.usecase.TrackSingleRoundEventUseCase
import com.example.round_of_golf_presentation.utils.RoundOfGolfUiEvent
import com.example.round_of_golf_presentation.utils.TrackShotUiEvent
import com.example.shared.data.model.Player
import com.example.shared.platform.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class RoundOfGolfViewModel(
    private val course: Course,
    private val currentPlayer: Player,
    private val locationTrackingService: LocationTrackingService,
    private val trackEventUseCase: TrackSingleRoundEventUseCase,
    private val checkLocationPermissionUseCase: CheckLocationPermissionUseCase,
    private val requestLocationPermissionUseCase: RequestLocationPermissionUseCase,
    private val saveScoreCardUseCase: SaveScoreCardUseCase,
    private val logger: Logger
) : ViewModel() {

    companion object Companion {
        private const val TAG = "LocationTrackingViewModel"
    }

    private val _locationState = MutableStateFlow(LocationTrackingUiState())
    val locationState: StateFlow<LocationTrackingUiState> = _locationState.asStateFlow()

    private val _currentScoreCard = MutableStateFlow(ScoreCard())
    val currentScoreCard: StateFlow<ScoreCard> = _currentScoreCard.asStateFlow()

    private val roundId: Long get() = _currentScoreCard.value.roundId

    private var trackingJob: Job? = null

    private var _roundOfGolfUiEvent = MutableStateFlow<RoundOfGolfUiEvent?>(null)
    val roundOfGolfUiEvent: StateFlow<RoundOfGolfUiEvent?> = _roundOfGolfUiEvent.asStateFlow()

    fun updateRoundOfGolfUiEvent(uiEvent: RoundOfGolfUiEvent?) {
        _roundOfGolfUiEvent.value = uiEvent
    }

    fun clearRoundOfGolfUiEvent() {
        _roundOfGolfUiEvent.value = null
    }

    private var _trackShotUiEvent = MutableStateFlow<TrackShotUiEvent?>(null)
    val trackShotUiEvent: StateFlow<TrackShotUiEvent?> = _trackShotUiEvent.asStateFlow()

    fun updateTrackShotUiEvent(uiEvent: TrackShotUiEvent?) {
        _trackShotUiEvent.value = uiEvent
    }

    fun clearTrackShotUiEvent() {
        _trackShotUiEvent.value = null
    }

    init {
        checkPermissionStatus()
    }

    fun startLocationTracking() {
        logger.info(TAG, "startLocationTracking() called")
        viewModelScope.launch {
            // Check permission first
            if (!checkLocationPermissionUseCase()) {
                logger.warn(TAG, "Location permission not granted")
                _locationState.value = _locationState.value.copy(
                    error = "Location permission required. Please grant permission first.",
                    hasPermission = false
                )
                return@launch
            }

            logger.info(TAG, "Permission granted, proceeding with tracking")

            // Cancel any existing tracking job but don't stop the service yet
            trackingJob?.cancel()
            trackingJob = null

            try {
                logger.info(TAG, "Setting UI state and starting location service")
                _locationState.value =
                    _locationState.value.copy(isLoading = true, isTracking = true, error = null)

                logger.info(TAG, "Calling locationTrackingService.startLocationTracking()")
                trackingJob = locationTrackingService.startLocationTracking().onEach { location ->
                    logger.info(TAG, "startLocationTracking() Location Triggered")
                    // Only save to database, no UI updates to prevent recomposition
                    try {
                        try {
                            val locationEvent = LocationUpdated(
                                location = location
                            )

                            trackEventUseCase.execute(
                                event = locationEvent,
                                roundId = roundId,
                                playerId = currentPlayer.id
                            )

                            logger.debug(
                                TAG,
                                "Location event saved to unified event system successfully"
                            )
                        } catch (error: Exception) {
                            logger.error(
                                TAG,
                                "Failed to save location event to unified system",
                                error
                            )
                        }

                        logger.info(TAG, "Coroutine launched successfully")
                    } catch (error: Exception) {
                        logger.error(TAG, "Failed to launch coroutine", error)
                    }
                }.catch { throwable ->
                    val errorMessage = when (throwable) {
                        else -> "Location tracking error: ${throwable.message}"
                    }
                    _locationState.value = _locationState.value.copy(
                        isLoading = false,
                        isTracking = false,
                        error = errorMessage
                    )
                }.launchIn(viewModelScope)
            } catch (e: Exception) {
                _locationState.value = _locationState.value.copy(
                    isLoading = false,
                    isTracking = false,
                    error = e.message ?: "Failed to start location tracking"
                )
            }
        }
    }

    fun stopLocationTracking() {
        logger.info(TAG, "stopLocationTracking() called")
        viewModelScope.launch {
            try {
                trackingJob?.cancel()
                trackingJob = null
                locationTrackingService.stopLocationTracking()
                _locationState.value = _locationState.value.copy(
                    isLoading = false,
                    isTracking = false,
                    error = null
                )
                logger.info(TAG, "Location tracking stopped successfully")
            } catch (e: Exception) {
                logger.error(TAG, "Error stopping location tracking", e)
                _locationState.value = _locationState.value.copy(
                    error = e.message ?: "Failed to stop location tracking"
                )
            }
        }
    }

    fun requestLocationPermission() {
        viewModelScope.launch {
            _locationState.value = _locationState.value.copy(isRequestingPermission = true, error = null)

            try {
                val result = requestLocationPermissionUseCase()

                when (result) {
                    is PermissionResult.Granted -> {
                        _locationState.value = _locationState.value.copy(
                            hasPermission = true,
                            isRequestingPermission = false,
                            error = null
                        )
                        // Automatically start location tracking after permission is granted
                        logger.info(TAG, "Permission granted, starting location tracking")
                        startLocationTracking()
                    }

                    is PermissionResult.Denied -> {
                        _locationState.value = _locationState.value.copy(
                            hasPermission = false,
                            isRequestingPermission = false,
                            error = "Location permission denied. Please try again."
                        )
                    }

                    is PermissionResult.PermanentlyDenied -> {
                        _locationState.value = _locationState.value.copy(
                            hasPermission = false,
                            isRequestingPermission = false,
                            error = "Location permission permanently denied. Please enable it in settings."
                        )
                    }
                }
            } catch (e: Exception) {
                _locationState.value = _locationState.value.copy(
                    isRequestingPermission = false,
                    error = e.message ?: "Error requesting permission"
                )
            }
        }
    }

    fun saveHoleScore(holeNumber: Int, score: Int) {
        val currentCard = _currentScoreCard.value
        val updatedScorecard = currentCard.scorecard.toMutableMap()
        updatedScorecard[holeNumber] = score

        val updatedCard = currentCard.copy(
            scorecard = updatedScorecard,
            lastUpdatedTimestamp = getCurrentTimeMillis()
        )
        _currentScoreCard.value = updatedCard

        // Save to database using UseCase
        viewModelScope.launch(Dispatchers.IO) {
            saveScoreCardUseCase(updatedCard).fold(
                onSuccess = {
                    logger.info(
                        TAG,
                        "Updated hole $holeNumber score to $score and saved to database"
                    )
                },
                onFailure = { error ->
                    logger.error(TAG, "Failed to save scorecard to database", error)
                }
            )
        }
    }

    fun getHoleScore(holeNumber: Int): Int? {
        return _currentScoreCard.value.scorecard[holeNumber]
    }

    fun getTotalScore(): Int {
        return _currentScoreCard.value.scorecard.values.filterNotNull().sum()
    }

    fun getCompletedHolesPar(): Int {
        val completedHoles = _currentScoreCard.value.scorecard.keys
        return course.holes.filter { it.id in completedHoles }.sumOf { it.par }
    }

    fun getScoreToPar(): String {
        val totalScore = getTotalScore()
        val completedPar = getCompletedHolesPar()

        return if (totalScore > 0 && completedPar > 0) {
            val difference = totalScore - completedPar
            when {
                difference > 0 -> "+$difference"
                difference < 0 -> "$difference"
                else -> "E"
            }
        } else {
            "E"
        }
    }

    fun checkPermissionStatus() {
        viewModelScope.launch {
            try {
                val hasPermission = checkLocationPermissionUseCase()
                _locationState.value = _locationState.value.copy(
                    hasPermission = hasPermission,
                    isRequestingPermission = false // Reset requesting flag
                )

                // Automatically start location tracking if permission is granted
                if (hasPermission && !_locationState.value.isTracking) {
                    logger.info(TAG, "Permission granted, starting location tracking automatically")
                    startLocationTracking()
                }
            } catch (e: Exception) {
                _locationState.value = _locationState.value.copy(
                    isRequestingPermission = false, // Reset requesting flag on error too
                    error = e.message ?: "Error checking permission"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
    }
}