package com.example.location_domain.domain.service

import android.Manifest
import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import com.example.shared.data.model.Location
import com.example.shared.platform.Logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow

class AndroidBackgroundLocationService(
    private val context: Context,
    private val logger: Logger
) : BackgroundLocationService {
    
    companion object {
        private const val TAG = "AndroidBackgroundLocationService"
    }
    
    private val _isBackgroundTrackingActive = MutableStateFlow(false)
    override val isBackgroundTrackingActive: StateFlow<Boolean> = _isBackgroundTrackingActive.asStateFlow()
    
    private val locationManager by lazy { 
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager 
    }
    
    override fun startBackgroundLocationTracking(intervalMs: Long): Flow<Location> {
        logger.info(TAG, "startBackgroundLocationTracking called with intervalMs: $intervalMs")
        return callbackFlow {
            if (_isBackgroundTrackingActive.value) {
                logger.warn(TAG, "Background tracking already active, closing flow")
                close()
                return@callbackFlow
            }
            
            // Check permissions first
            try {
                val hasCoarsePermission = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val hasFinePermission = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val hasBackgroundPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                } else {
                    true // Not needed on older versions
                }
                
                logger.info(TAG, "Permission check - Coarse: $hasCoarsePermission, Fine: $hasFinePermission, Background: $hasBackgroundPermission")
                
                if (!hasCoarsePermission && !hasFinePermission) {
                    logger.error(TAG, "No location permissions granted")
                    _isBackgroundTrackingActive.value = false
                    close(SecurityException("Location permissions not granted"))
                    return@callbackFlow
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasBackgroundPermission) {
                    logger.warn(TAG, "Background location permission not granted - tracking may be limited")
                }
            } catch (e: Exception) {
                logger.error(TAG, "Error checking permissions", e)
                _isBackgroundTrackingActive.value = false
                close(e)
                return@callbackFlow
            }
            
            logger.info(TAG, "Starting background location tracking")
            _isBackgroundTrackingActive.value = true
            
            val locationListener = LocationListener { location ->
                logger.debug(TAG, "Location received: lat=${location.latitude}, long=${location.longitude}")
                val locationEvent = Location(
                    lat = location.latitude,
                    long = location.longitude
                )

                trySend(locationEvent)
            }
            
            try {
                // Check if location services are enabled
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                logger.info(TAG, "Provider status - GPS: $isGpsEnabled, Network: $isNetworkEnabled")
                
                if (!isGpsEnabled && !isNetworkEnabled) {
                    logger.error(TAG, "No location providers are enabled")
                    _isBackgroundTrackingActive.value = false
                    close(Exception("Location services are disabled"))
                    return@callbackFlow
                }
                
                logger.info(TAG, "Starting foreground service")
                // Start foreground service for background location tracking
                val serviceIntent = Intent(context, LocationForegroundService::class.java)
                val serviceComponent = context.startForegroundService(serviceIntent)
                logger.info(TAG, "Foreground service started: $serviceComponent")
                
                logger.info(TAG, "Requesting location updates from GPS and Network providers")
                // Request location updates
                if (isGpsEnabled) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        intervalMs,
                        0f,
                        locationListener
                    )
                    logger.info(TAG, "GPS location updates requested")
                }
                
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        intervalMs,
                        0f,
                        locationListener
                    )
                    logger.info(TAG, "Network location updates requested")
                }
                
                logger.info(TAG, "Location tracking setup complete")
            } catch (e: SecurityException) {
                logger.error(TAG, "Security exception starting location tracking", e)
                _isBackgroundTrackingActive.value = false
                close(e)
            } catch (e: Exception) {
                logger.error(TAG, "Exception starting location tracking", e)
                _isBackgroundTrackingActive.value = false
                close(e)
            }
            
            awaitClose {
                logger.info(TAG, "Stopping background location tracking")
                try {
                    locationManager.removeUpdates(locationListener)
                    val serviceIntent = Intent(context, LocationForegroundService::class.java)
                    context.stopService(serviceIntent)
                    logger.info(TAG, "Location tracking stopped successfully")
                } catch (e: Exception) {
                    logger.error(TAG, "Error stopping location tracking", e)
                }
                _isBackgroundTrackingActive.value = false
            }
        }
    }
    
    override fun stopBackgroundLocationTracking() {
        logger.info(TAG, "stopBackgroundLocationTracking called")
        _isBackgroundTrackingActive.value = false
        val serviceIntent = Intent(context, LocationForegroundService::class.java)
        context.stopService(serviceIntent)
        logger.info(TAG, "Background location tracking stopped")
    }
}

class LocationForegroundService : Service() {
    companion object {
        const val NOTIFICATION_ID = 12345
        const val CHANNEL_ID = "location_tracking_channel"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Tracks location in the background"
            setShowBadge(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification() = Notification.Builder(this, CHANNEL_ID)
        .setContentTitle("Location Tracking Active")
        .setContentText("Tracking your location for golf round")
        .setSmallIcon(R.drawable.ic_menu_mylocation)
        .setOngoing(true)
        .build()
}

