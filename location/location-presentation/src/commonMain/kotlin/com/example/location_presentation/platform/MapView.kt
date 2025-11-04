package com.example.location_presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.shared.data.model.Location
import com.example.shared.data.model.Hole

@Composable
expect fun MapView(
    modifier: Modifier = Modifier,
    currentHole: Hole?,
    hasLocationPermission: Boolean,
    gesturesEnabled: Boolean = true,
    currentBallLocation: Location,
    onMapClick: ((Location) -> Unit)? = null,
    onMapSizeChanged: ((width: Int, height: Int) -> Unit)? = null,
    onCameraPositionChanged: ((MapCameraPosition) -> Unit)? = null,
    onMapReady: ((Any) -> Unit)? = null // Any to be platform-agnostic
)

data class MapCameraPosition(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float
)