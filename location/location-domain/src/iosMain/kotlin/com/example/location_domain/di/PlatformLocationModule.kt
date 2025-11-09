package com.example.location_domain.di

import com.example.location_domain.data.repository.IOSLocationProvider
import com.example.location_domain.data.repository.LocationProvider
import com.example.location_domain.permissions.IOSCheckLocationPermission
import com.example.location_domain.permissions.IOSRequestLocationPermission
import com.example.location_domain.domain.permissions.CheckLocationPermission
import com.example.location_domain.domain.permissions.RequestLocationPermission
import com.example.location_domain.domain.service.BackgroundLocationService
import com.example.location_domain.domain.service.IOSBackgroundLocationService
import com.example.location_domain.domain.service.IOSBackgroundLocationServiceWrapper
import com.example.shared.platform.createLogger
import org.koin.dsl.module

actual val platformLocationModule = module {
    single<LocationProvider> { IOSLocationProvider() }
    single<CheckLocationPermission> { IOSCheckLocationPermission() }
    single<RequestLocationPermission> { IOSRequestLocationPermission() }
    single<BackgroundLocationService> {
        // Create the iOS service directly without registering it as a separate dependency
        // to avoid KClass reflection issues with Objective-C subclasses
        IOSBackgroundLocationServiceWrapper(IOSBackgroundLocationService(createLogger()))
    }
}