package com.example.location_presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.shared.data.model.Hole
import com.example.shared.data.model.Location
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.location_domain.domain.usecase.CalculateMapCameraPositionUseCase
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import org.koin.compose.koinInject

@Composable
actual fun MapView(
    modifier: Modifier,
    currentHole: Hole?,
    hasLocationPermission: Boolean,
    gesturesEnabled: Boolean,
    currentBallLocation: Location,
    onMapClick: ((Location) -> Unit)?,
    onMapSizeChanged: ((width: Int, height: Int) -> Unit)?,
    onCameraPositionChanged: ((MapCameraPosition) -> Unit)?,
    onMapReady: ((Any) -> Unit)?
) {
    val cameraPositionState = rememberCameraPositionState()
    
    // Inject use case for camera position calculation
    val calculateCameraPositionUseCase: CalculateMapCameraPositionUseCase = koinInject()
    
    // Create camera controller
    val cameraController = remember { MapCameraController(cameraPositionState) }

    // Track camera position changes
    LaunchedEffect(cameraPositionState.position) {
        onCameraPositionChanged?.invoke(
            MapCameraPosition(
                latitude = cameraPositionState.position.target.latitude,
                longitude = cameraPositionState.position.target.longitude,
                zoom = cameraPositionState.position.zoom
            )
        )
    }

    // Handle currentHole and currentBallLocation updates
    LaunchedEffect(currentHole, currentBallLocation) {
        currentHole?.let { hole ->
            // Calculate camera position using current ball location and flag
            val cameraPosition = calculateCameraPositionUseCase(currentBallLocation, hole.flagLocation)
            
            // Apply camera positioning using platform-specific controller
            cameraController.applyHoleCameraPosition(hole, currentBallLocation, cameraPosition)
        }
    }
    
    GoogleMap(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .onSizeChanged { size -> 
                onMapSizeChanged?.invoke(size.width, size.height)
            },
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.HYBRID,
            isMyLocationEnabled = hasLocationPermission
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            zoomGesturesEnabled = gesturesEnabled,
            scrollGesturesEnabled = gesturesEnabled,
            tiltGesturesEnabled = gesturesEnabled,
            rotationGesturesEnabled = gesturesEnabled
        ),
        onMapClick = { latLng ->
            onMapClick?.invoke(
                Location(
                    lat = latLng.latitude,
                    long = latLng.longitude
                )
            )
        },
        onMapLoaded = {
            // Trigger initial camera position callback
            onCameraPositionChanged?.invoke(
                MapCameraPosition(
                    latitude = cameraPositionState.position.target.latitude,
                    longitude = cameraPositionState.position.target.longitude,
                    zoom = cameraPositionState.position.zoom
                )
            )
        }
    ) {
        // Use MapEffect to access the GoogleMap instance
        MapEffect { googleMap ->
            onMapReady?.invoke(googleMap)
        }
        // Markers and polylines are now rendered using Compose components with screen projection in RoundOfGolf
    }
}

