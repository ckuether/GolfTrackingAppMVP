package com.example.location_domain.permissions

import com.example.location_domain.domain.permissions.CheckLocationPermission
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse

class IOSCheckLocationPermission : CheckLocationPermission {

    override suspend fun invoke(): Boolean {
        val status = CLLocationManager.Companion.authorizationStatus()
        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
               status == kCLAuthorizationStatusAuthorizedAlways
    }
}