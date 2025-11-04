package com.example.location_presentation.platform


import cocoapods.GoogleMaps.GMSCameraPosition
import cocoapods.GoogleMaps.GMSCoordinateBounds
import cocoapods.GoogleMaps.GMSMapView
import cocoapods.GoogleMaps.animateToCameraPosition
import com.example.shared.data.model.Hole
import com.example.shared.data.model.Location
import com.example.shared.data.model.MapCameraPosition
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.UIKit.UIEdgeInsetsMake

/**
 * iOS implementation of MapCameraController using Google Maps iOS SDK.
 * Mimics Android's bounds-based zoom calculation.
 */
@OptIn(ExperimentalForeignApi::class)
actual class MapCameraController(
    private val mapView: GMSMapView
) {

    companion object {
        private const val MAP_PADDING_TOP = 64.0
        private const val MAP_PADDING = 32.0
    }

    actual suspend fun applyHoleCameraPosition(hole: Hole, ballLocation: Location, mapCameraPosition: MapCameraPosition) {
        try {
            // Create bounds similar to Android's LatLngBounds using ball location and flag
            val ballLoc = CLLocationCoordinate2DMake(ballLocation.lat, ballLocation.long)
            val flagLoc = CLLocationCoordinate2DMake(hole.flagLocation.lat, hole.flagLocation.long)
            val bounds = GMSCoordinateBounds(ballLoc, flagLoc)

            val insets = UIEdgeInsetsMake(MAP_PADDING_TOP, MAP_PADDING, MAP_PADDING, MAP_PADDING)
            val boundsCamera = mapView.cameraForBounds(bounds, insets)

            if (boundsCamera != null) {
                // Create final camera with bounds zoom but add bearing rotation
                val finalCamera = GMSCameraPosition.Companion.cameraWithLatitude(
                    mapCameraPosition.centerLat,
                    mapCameraPosition.centerLng,
                    boundsCamera.zoom,
                    mapCameraPosition.bearing.toDouble(),
                    0.0
                )

                mapView.animateToCameraPosition(finalCamera)
            } else {
                // Fallback if bounds calculation fails
                val fallbackCamera = GMSCameraPosition.Companion.cameraWithLatitude(
                    mapCameraPosition.centerLat,
                    mapCameraPosition.centerLng,
                    16.0f,
                    mapCameraPosition.bearing.toDouble(),
                    0.0
                )

                mapView.animateToCameraPosition(fallbackCamera)
            }

        } catch (e: Exception) {
            // Fallback to fixed zoom with bearing
            val camera = GMSCameraPosition.Companion.cameraWithLatitude(
                mapCameraPosition.centerLat,
                mapCameraPosition.centerLng,
                15.0f,
                mapCameraPosition.bearing.toDouble(),
                0.0
            )

            mapView.animateToCameraPosition(camera)
        }
    }
}