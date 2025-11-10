package com.example.location_domain.domain.usecase

import com.example.shared.data.model.Location
import com.example.shared.data.model.MapCameraPosition

/**
 * Use case for calculating map camera position based on golf hole data.
 * Returns platform-agnostic camera positioning data that can be used by platform-specific map implementations.
 */
class CalculateMapCameraPosition(
    private val calculateBearings: CalculateBearings
) {
    
    companion object {
        const val DEFAULT_ZOOM_LEVEL = 16.0f
    }

    /**
     * Calculates camera position for two specific locations.
     *
     * @param startLocation The starting location (e.g., tee)
     * @param endLocation The ending location (e.g., flag)
     * @param defaultZoom The default zoom level to use
     * @return CameraPosition containing center coordinates, zoom, and bearing
     */
    operator fun invoke(
        startLocation: Location,
        endLocation: Location,
        defaultZoom: Float = DEFAULT_ZOOM_LEVEL
    ): MapCameraPosition {
        val centerLat = (startLocation.lat + endLocation.lat) / 2
        val centerLng = (startLocation.long + endLocation.long) / 2
        val bearing = calculateBearings(startLocation, endLocation)

        return MapCameraPosition(
            centerLat = centerLat,
            centerLng = centerLng,
            zoom = defaultZoom,
            bearing = bearing.toFloat()
        )
    }
}