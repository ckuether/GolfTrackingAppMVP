package com.example.location_presentation.platform

import com.example.shared.data.model.Hole
import com.example.shared.data.model.Location
import com.example.shared.data.model.MapCameraPosition

/**
 * Platform-specific camera controller for handling map camera positioning and target interactions.
 * Implements bounds-based zoom and bearing logic per platform.
 */
expect class MapCameraController {

    /**
     * Applies camera positioning for a golf hole with platform-specific logic.
     *
     * @param hole The golf hole containing tee and flag locations
     * @param ballLocation The current ball location (defaults to tee if not provided)
     * @param mapCameraPosition The calculated camera position from use case
     */
    suspend fun applyHoleCameraPosition(hole: Hole, ballLocation: Location, mapCameraPosition: MapCameraPosition)
}