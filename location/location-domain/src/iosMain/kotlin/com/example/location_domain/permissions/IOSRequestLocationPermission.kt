package com.example.location_domain.permissions

import com.example.location_domain.domain.permissions.PermissionResult
import com.example.location_domain.domain.permissions.RequestLocationPermission
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

class IOSRequestLocationPermission : RequestLocationPermission {

    private val locationManager = CLLocationManager()

    override suspend fun invoke(): PermissionResult {
        val currentStatus = CLLocationManager.Companion.authorizationStatus()

        return when (currentStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> {
                PermissionResult.Granted
            }
            kCLAuthorizationStatusDenied -> {
                PermissionResult.PermanentlyDenied
            }
            kCLAuthorizationStatusRestricted -> {
                PermissionResult.PermanentlyDenied
            }
            kCLAuthorizationStatusNotDetermined -> {
                requestPermission()
            }
            else -> {
                PermissionResult.Denied
            }
        }
    }

    private suspend fun requestPermission(): PermissionResult {
        return suspendCancellableCoroutine { continuation ->
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                    val status = manager.authorizationStatus()
                    val result = when (status) {
                        kCLAuthorizationStatusAuthorizedWhenInUse,
                        kCLAuthorizationStatusAuthorizedAlways -> {
                            PermissionResult.Granted
                        }

                        kCLAuthorizationStatusDenied -> {
                            PermissionResult.PermanentlyDenied
                        }

                        kCLAuthorizationStatusRestricted -> {
                            PermissionResult.PermanentlyDenied
                        }

                        else -> {
                            PermissionResult.Denied
                        }
                    }

                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }

                override fun locationManager(
                    manager: CLLocationManager,
                    didFailWithError: NSError
                ) {
                    if (continuation.isActive) {
                        continuation.resume(PermissionResult.Denied)
                    }
                }
            }

            locationManager.delegate = delegate
            locationManager.requestWhenInUseAuthorization()

            continuation.invokeOnCancellation {
                locationManager.delegate = null
            }
        }
    }
}