package com.example.location_domain.domain.service

import com.example.shared.data.model.Location
import kotlinx.coroutines.flow.Flow

interface BackgroundLocationService {
    fun startBackgroundLocationTracking(intervalMs: Long): Flow<Location>
    fun stopBackgroundLocationTracking()
    val isBackgroundTrackingActive: Flow<Boolean>
}