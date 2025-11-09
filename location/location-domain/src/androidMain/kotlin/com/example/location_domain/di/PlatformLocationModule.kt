package com.example.location_domain.di

import com.example.location_domain.data.repository.AndroidLocationProvider
import com.example.location_domain.data.repository.LocationProvider
import com.example.location_domain.permissions.AndroidCheckLocationPermission
import com.example.location_domain.permissions.AndroidRequestLocationPermission
import com.example.location_domain.domain.permissions.CheckLocationPermission
import com.example.location_domain.domain.permissions.RequestLocationPermission
import com.example.location_domain.domain.service.AndroidBackgroundLocationService
import com.example.location_domain.domain.service.BackgroundLocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformLocationModule = module {
    single<LocationProvider> { AndroidLocationProvider(androidContext()) }
    single<CheckLocationPermission> { AndroidCheckLocationPermission(androidContext()) }
    single<RequestLocationPermission> {
        AndroidRequestLocationPermission(
            androidContext(),
            get()
        )
    }
    single<BackgroundLocationService> { AndroidBackgroundLocationService(androidContext(), get()) }
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
}