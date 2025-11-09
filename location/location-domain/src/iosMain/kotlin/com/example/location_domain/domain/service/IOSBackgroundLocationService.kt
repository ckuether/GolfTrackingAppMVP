package com.example.location_domain.domain.service

import com.example.shared.data.model.Location
import com.example.shared.platform.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.*
import platform.Foundation.NSError
import platform.Foundation.NSTimer
import platform.darwin.NSObject

class IOSBackgroundLocationService(
    private val logger: Logger
): NSObject(), CLLocationManagerDelegateProtocol {

    private val TAG = "IOSBackgroundLocationService"
    
    private val _isBackgroundTrackingActive = MutableStateFlow(false)
    val isBackgroundTrackingActive: StateFlow<Boolean> = _isBackgroundTrackingActive.asStateFlow()
    
    private val locationManager = CLLocationManager().apply {
        delegate = this@IOSBackgroundLocationService
        desiredAccuracy = kCLLocationAccuracyBest
        distanceFilter = 10.0 // meters
    }
    
    private var locationCallback: ((Location) -> Unit)? = null
    private var updateTimer: NSTimer? = null
    
    fun startBackgroundLocationTracking(intervalMs: Long): Flow<Location> {
        logger.info(TAG, "startBackgroundLocationTracking called with intervalMs: $intervalMs")
        return callbackFlow {
            try {
                if (_isBackgroundTrackingActive.value) {
                    logger.warn(TAG, "Background tracking already active, closing flow")
                    close()
                    return@callbackFlow
                }
                
                logger.info(TAG, "Starting background location tracking")
                _isBackgroundTrackingActive.value = true
                
                locationCallback = { locationEvent ->
                    try {
                        val result = trySend(locationEvent)
                        if (result.isFailure) {
                            logger.warn(TAG, "Failed to send location update: ${result.exceptionOrNull()?.message}")
                        }
                    } catch (e: Exception) {
                        logger.error(TAG, "Exception in location callback: ${e.message}")
                    }
                }
                
                // Request background location permission
                logger.info(TAG, "Checking location authorization status: ${locationManager.authorizationStatus}")
                when (locationManager.authorizationStatus) {
                    kCLAuthorizationStatusNotDetermined -> {
                        logger.info(TAG, "Requesting always authorization")
                        locationManager.requestAlwaysAuthorization()
                    }
                    kCLAuthorizationStatusAuthorizedAlways,
                    kCLAuthorizationStatusAuthorizedWhenInUse -> {
                        logger.info(TAG, "Location authorized, starting updates")
                        startLocationUpdates()
                    }
                    else -> {
                        logger.error(TAG, "Location permission denied")
                        _isBackgroundTrackingActive.value = false
                        close(Exception("Location permission denied"))
                        return@callbackFlow
                    }
                }
                
                awaitClose {
                    logger.info(TAG, "Flow closing - stopping background location tracking")
                    try {
                        stopLocationUpdates()
                        locationCallback = null
                        _isBackgroundTrackingActive.value = false
                    } catch (e: Exception) {
                        logger.error(TAG, "Error during cleanup: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                logger.error(TAG, "Exception in startBackgroundLocationTracking: ${e.message}")
                _isBackgroundTrackingActive.value = false
                close(e)
            }
        }
    }
    
    fun stopBackgroundLocationTracking() {
        logger.info(TAG, "stopBackgroundLocationTracking called")
        stopLocationUpdates()
        _isBackgroundTrackingActive.value = false
        locationCallback = null
        logger.info(TAG, "Background location tracking stopped")
    }
    
    private fun startLocationUpdates() {
        logger.info(TAG, "Starting location updates")
        
        // Only enable background updates if we have "always" authorization
        if (locationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways) {
            logger.info(TAG, "Setting up background location updates")
            try {
                locationManager.allowsBackgroundLocationUpdates = true
                locationManager.pausesLocationUpdatesAutomatically = false
                logger.info(TAG, "Background location updates enabled successfully")
            } catch (e: Exception) {
                logger.warn(TAG, "Failed to enable background location updates: ${e.message}. Continuing with foreground only.")
                locationManager.pausesLocationUpdatesAutomatically = false
            }
        } else {
            logger.warn(TAG, "Only 'when in use' authorization available, background updates disabled")
            locationManager.pausesLocationUpdatesAutomatically = false
        }
        
        locationManager.startUpdatingLocation()
        locationManager.startMonitoringSignificantLocationChanges()
        logger.info(TAG, "Location updates started")
    }
    
    private fun stopLocationUpdates() {
        logger.info(TAG, "Stopping location updates")
        updateTimer?.invalidate()
        updateTimer = null
        locationManager.stopUpdatingLocation()
        locationManager.stopMonitoringSignificantLocationChanges()
        
        // Only disable background updates if it was previously enabled
        if (locationManager.authorizationStatus == kCLAuthorizationStatusAuthorizedAlways) {
            try {
                locationManager.allowsBackgroundLocationUpdates = false
            } catch (e: Exception) {
                logger.warn(TAG, "Failed to disable background location updates: ${e.message}")
            }
        }
        logger.info(TAG, "Location updates stopped")
    }


    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        try {
            val locations = didUpdateLocations as List<CLLocation>
            logger.debug(TAG, "Received ${locations.size} location updates")
            locations.lastOrNull()?.let { location ->
                val coordinate = location.coordinate
                val lat = coordinate.useContents { latitude }
                val lng = coordinate.useContents { longitude }
                logger.debug(TAG, "Location received: lat=$lat, long=$lng")

                val locationEvent = Location(
                    lat = lat,
                    long = lng
                )

                locationCallback?.invoke(locationEvent)
            }
        } catch (e: Exception) {
            logger.error(TAG, "Error processing location update: ${e.message}")
        }
    }
    
    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        try {
            logger.error(TAG, "Location manager failed with error: ${didFailWithError.localizedDescription}")
            _isBackgroundTrackingActive.value = false
        } catch (e: Exception) {
            logger.error(TAG, "Error handling location manager failure: ${e.message}")
        }
    }
    
    override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: CLAuthorizationStatus) {
        try {
            logger.info(TAG, "Authorization status changed to: $didChangeAuthorizationStatus")
            when (didChangeAuthorizationStatus) {
                kCLAuthorizationStatusAuthorizedAlways,
                kCLAuthorizationStatusAuthorizedWhenInUse -> {
                    logger.info(TAG, "Location authorized")
                    if (_isBackgroundTrackingActive.value) {
                        startLocationUpdates()
                    }
                }
                else -> {
                    logger.warn(TAG, "Location not authorized, stopping updates")
                    stopLocationUpdates()
                    _isBackgroundTrackingActive.value = false
                }
            }
        } catch (e: Exception) {
            logger.error(TAG, "Error handling authorization status change: ${e.message}")
        }
    }
}


class IOSBackgroundLocationServiceWrapper(
    private val iosService: IOSBackgroundLocationService
): BackgroundLocationService {
    override val isBackgroundTrackingActive: Flow<Boolean>
        get() = iosService.isBackgroundTrackingActive

    override fun startBackgroundLocationTracking(intervalMs: Long): Flow<Location> {
        return iosService.startBackgroundLocationTracking(intervalMs)
    }

    override fun stopBackgroundLocationTracking() {
        iosService.stopBackgroundLocationTracking()
    }
}