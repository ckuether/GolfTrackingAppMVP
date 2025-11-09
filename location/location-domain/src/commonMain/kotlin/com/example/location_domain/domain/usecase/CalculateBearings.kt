package com.example.location_domain.domain.usecase

import com.example.shared.data.model.Location
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Use case for calculating the bearing (direction) between two geographic locations.
 *
 * The bearing represents the compass direction from the start location to the end location,
 * where 0° is North, 90° is East, 180° is South, and 270° is West.
 *
 * This is useful for map orientation, navigation, and positioning calculations.
 */
class CalculateBearings {

    /**
     * Calculates the bearing from start location to end location.
     *
     * @param start The starting location
     * @param end The destination location
     * @return The bearing in degrees (0-360), where 0° is North
     */
    operator fun invoke(start: Location, end: Location): Double {
        val lat1Rad = start.lat * PI / 180.0
        val lat2Rad = end.lat * PI / 180.0
        val deltaLngRad = (end.long - start.long) * PI / 180.0

        val y = sin(deltaLngRad) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) -
                sin(lat1Rad) * cos(lat2Rad) * cos(deltaLngRad)

        val bearingRad = atan2(y, x)
        val bearingDeg = bearingRad * 180.0 / PI

        // Normalize to 0-360 degrees
        return (bearingDeg + 360.0) % 360.0
    }
}