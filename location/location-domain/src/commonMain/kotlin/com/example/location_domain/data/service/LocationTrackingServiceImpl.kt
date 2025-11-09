package com.example.location_domain.data.service

import com.example.location_domain.domain.service.LocationTrackingService
import com.example.location_domain.domain.service.BackgroundLocationService
import com.example.shared.data.model.Location
import com.example.shared.utils.TimeMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

class LocationTrackingServiceImpl(
    private val backgroundLocationService: BackgroundLocationService,
) : LocationTrackingService {
    
    private val _isTracking = MutableStateFlow(false)
    override val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()
    
    private var trackingJob: Job? = null
    
    override suspend fun startLocationTracking(): Flow<Location> {
        if (_isTracking.value) {
            return emptyFlow()
        }
        
        return try {
            backgroundLocationService.startBackgroundLocationTracking(intervalMs = TimeMillis.FIFTEEN_SECONDS)
                .onStart { 
                    _isTracking.value = true 
                }
                .onCompletion { 
                    _isTracking.value = false 
                }
                .catch { throwable ->
                    _isTracking.value = false
                    throw throwable
                }
        } catch (e: Exception) {
            _isTracking.value = false
            throw e
        }
    }
    
    override suspend fun stopLocationTracking() {
        trackingJob?.cancel()
        trackingJob = null
        backgroundLocationService.stopBackgroundLocationTracking()
        _isTracking.value = false
    }
}