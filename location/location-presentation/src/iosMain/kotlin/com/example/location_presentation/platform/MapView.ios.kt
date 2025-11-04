package com.example.location_presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import platform.Foundation.NSLog
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import com.example.shared.data.model.Hole
import org.koin.compose.koinInject
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import platform.CoreLocation.CLLocationCoordinate2D
import cocoapods.GoogleMaps.*
import com.example.location_domain.domain.usecase.CalculateMapCameraPositionUseCase
import kotlinx.cinterop.useContents
import platform.darwin.NSObject
import com.example.shared.data.model.Location
import platform.UIKit.UIScreen

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
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

    // Get the actual device scale factor for points-to-pixels conversion
    val deviceScale = remember { UIScreen.mainScreen.scale.toInt() }
    val calculateCameraPositionUseCase: CalculateMapCameraPositionUseCase = koinInject()
    val clickHandlerState = remember { mutableStateOf<((Location) -> Unit)?>(null) }
    val mapViewRef = remember { mutableStateOf<GMSMapView?>(null) }
    val delegateRef = remember { mutableStateOf<GMSMapViewDelegateProtocol?>(null) }
    val cameraControllerRef = remember { mutableStateOf<MapCameraController?>(null) }
    val currentCameraPosition = remember { mutableStateOf<MapCameraPosition?>(null) }
    val mapSizeReported = remember { mutableStateOf(false) }

    // Track camera position changes
    LaunchedEffect(currentCameraPosition.value) {
        currentCameraPosition.value?.let { position ->
            onCameraPositionChanged?.invoke(position)
        }
    }

    // Handle camera updates when hole or ball location changes
    LaunchedEffect(currentHole, currentBallLocation) {
        mapViewRef.value?.let { mapView ->
            cameraControllerRef.value?.let { cameraController ->
                when {
                    currentHole != null -> {
                        // Calculate camera position using current ball location and flag
                        val cameraPosition = calculateCameraPositionUseCase(currentBallLocation, currentHole.flagLocation)
                        
                        // Apply camera positioning using platform-specific controller
                        cameraController.applyHoleCameraPosition(currentHole, currentBallLocation, cameraPosition)
                    }
                }
            }
        }
    }
    

    // Debug LaunchedEffect to track click handler changes
    LaunchedEffect(onMapClick) {
        clickHandlerState.value = onMapClick
    }

    UIKitView(
        modifier = modifier,
        interactive = true,
        factory = {
            // Set default camera position (will be updated by camera controller if hole exists)
            val initialCamera = GMSCameraPosition.cameraWithLatitude(39.7392, -104.9903, 10.0f)

            // Create the map view
            val mapView = GMSMapView()
            mapView.setCamera(initialCamera)

            // Configure map settings
            mapView.setMapType(kGMSTypeHybrid)
            mapView.setMyLocationEnabled(hasLocationPermission)

            // Configure gestures - enable/disable based on dragging state
            mapView.settings.setAllGesturesEnabled(gesturesEnabled)

            // Create and set delegate for map interactions
            val mapDelegate = object : NSObject(), GMSMapViewDelegateProtocol {

                override fun mapView(
                    mapView: GMSMapView,
                    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
                    didTapAtCoordinate: CValue<CLLocationCoordinate2D>
                ) {

                    clickHandlerState.value?.let { clickHandler ->
                        didTapAtCoordinate.useContents {
                            val location = Location(
                                lat = this.latitude,
                                long = this.longitude
                            )
                            NSLog("GoogleMaps: Invoking click handler for lat=${location.lat}, lng=${location.long}")
                            clickHandler(location)
                        }
                    } ?: run {
                        NSLog("GoogleMaps: No click handler available - onMapClick is null")
                    }
                }

                // Marker dragging is now handled by Compose components

                override fun mapView(
                    mapView: GMSMapView,
                    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
                    didChangeCameraPosition: GMSCameraPosition
                ) {
                    // Track camera position changes
                    didChangeCameraPosition.target.useContents {
                        val position = MapCameraPosition(
                            latitude = this.latitude,
                            longitude = this.longitude,
                            zoom = didChangeCameraPosition.zoom
                        )
                        currentCameraPosition.value = position
                    }
                    
                    // Report map size once when camera moves (indicating map is ready)
                    if (!mapSizeReported.value) {
                        val bounds = mapView.bounds
                        val width = bounds.useContents { this.size.width.toInt() } * deviceScale
                        val height = bounds.useContents { this.size.height.toInt() } * deviceScale
                        if (width > 0 && height > 0) {
                            onMapSizeChanged?.invoke(width, height)
                            mapSizeReported.value = true
                        }
                    }
                }
            }

            // Store delegate reference to prevent deallocation
            delegateRef.value = mapDelegate
            mapView.setDelegate(mapDelegate)
            mapViewRef.value = mapView

            // Initialize camera controller
            cameraControllerRef.value = MapCameraController(mapView)

            // Set initial click handler
            clickHandlerState.value = onMapClick

            // Trigger initial camera position callback (similar to Android's onMapLoaded)
            mapView.camera.target.useContents {
                val initialPosition = MapCameraPosition(
                    latitude = this.latitude,
                    longitude = this.longitude,
                    zoom = mapView.camera.zoom
                )
                currentCameraPosition.value = initialPosition
            }

            // Trigger onMapReady callback with the map instance
            onMapReady?.invoke(mapView)

            mapView
        },
        update = { mapView ->
            // Update click handler and ensure it's properly set
            clickHandlerState.value = onMapClick
            
            // Update gesture settings dynamically
            mapView.settings.setAllGesturesEnabled(gesturesEnabled)
        }
    )
}