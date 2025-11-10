package com.example.location_domain.domain.usecase

import com.example.shared.data.model.Location
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalculateMapCameraPositionTest {

    // Use real CalculateBearings for integration testing
    private val calculateBearings = CalculateBearings()

    private val calculateMapCameraPosition = CalculateMapCameraPosition(calculateBearings)

    // Test 1: testCalculateCenterPoint
    @Test
    fun testCalculateCenterPoint() {
        // Given: Two golf course locations
        val teeLocation = Location(lat = 40.0, long = -74.0)
        val flagLocation = Location(lat = 42.0, long = -72.0)
        
        // When: Calculate camera position
        val result = calculateMapCameraPosition(teeLocation, flagLocation)
        
        // Then: Center should be exact midpoint
        val expectedCenterLat = (40.0 + 42.0) / 2
        val expectedCenterLng = (-74.0 + (-72.0)) / 2
        
        assertEquals(expectedCenterLat, result.centerLat, 0.001)
        assertEquals(expectedCenterLng, result.centerLng, 0.001)
    }

    // Test 2: testDefaultZoom
    @Test
    fun testDefaultZoom() {
        // Given: Any two locations
        val start = Location(lat = 40.0, long = -74.0)
        val end = Location(lat = 40.1, long = -74.1)
        
        // When: Calculate camera position without specifying zoom
        val result = calculateMapCameraPosition(start, end)
        
        // Then: Should use default zoom level
        assertEquals(CalculateMapCameraPosition.DEFAULT_ZOOM_LEVEL, result.zoom)
    }

    // Test 3: testCustomZoom
    @Test
    fun testCustomZoom() {
        // Given: Any two locations and custom zoom level
        val start = Location(lat = 40.0, long = -74.0)
        val end = Location(lat = 40.1, long = -74.1)
        val customZoom = 18.5f
        
        // When: Calculate camera position with custom zoom
        val result = calculateMapCameraPosition(start, end, customZoom)
        
        // Then: Should use the custom zoom level
        assertEquals(customZoom, result.zoom)
    }

    // Test 4: testBearingDelegation
    @Test
    fun testBearingDelegation() {
        // Given: Locations for directional movements
        val start = Location(lat = 40.0, long = -74.0)
        val northEnd = Location(lat = 41.0, long = -74.0)    // North
        val eastEnd = Location(lat = 40.0, long = -73.0)     // East
        
        // When: Calculate camera positions
        val northResult = calculateMapCameraPosition(start, northEnd)
        val eastResult = calculateMapCameraPosition(start, eastEnd)
        
        // Then: Bearing should be delegated to CalculateBearings
        // We're testing delegation, not exact bearing values
        assertEquals(0.0f, northResult.bearing, 1.0f)     // Should be close to North (0°)
        assertEquals(90.0f, eastResult.bearing, 1.0f)     // Should be close to East (90°)
    }

    // Test 5: testBearingConversion
    @Test
    fun testBearingConversion() {
        // Given: Locations that will produce a specific bearing
        val start = Location(lat = 40.0, long = -74.0)
        val end = Location(lat = 41.0, long = -74.0)  // Pure north movement
        
        // When: Calculate camera position
        val result = calculateMapCameraPosition(start, end)
        
        // Calculate expected bearing directly for comparison
        val directBearing = calculateBearings(start, end)
        val expectedFloatBearing = directBearing.toFloat()
        
        assertEquals(expectedFloatBearing, result.bearing, 0.001f,
                    "Bearing conversion from Double to Float should be accurate")
        
        // Verify the conversion maintains precision
        val precisionDifference = abs(directBearing - result.bearing.toDouble())
        assertTrue(precisionDifference < 0.01,
                  "Double to Float conversion should maintain reasonable precision")
    }

    // Test 6: testBearingPassthrough
    @Test
    fun testBearingPassthrough() {
        // Given: Multiple directional movements to test bearing passthrough
        val testCases = listOf(
            // Different movements to verify bearing is correctly passed through
            Pair(Location(40.0, -74.0), Location(41.0, -74.0)),      // North
            Pair(Location(40.0, -74.0), Location(40.0, -73.0)),      // East  
            Pair(Location(41.0, -74.0), Location(40.0, -74.0)),      // South
            Pair(Location(40.0, -73.0), Location(40.0, -74.0)),      // West
            Pair(Location(40.0, -74.0), Location(40.5, -73.5))       // Northeast
        )
        
        testCases.forEach { (start, end) ->
            // When: Calculate camera position and direct bearing separately
            val cameraResult = calculateMapCameraPosition(start, end)
            val directBearing = calculateBearings(start, end)
            
            // Then: Camera bearing should match direct bearing calculation
            assertEquals(
                directBearing.toFloat(), cameraResult.bearing, 0.001f,
                "Camera position bearing should exactly match CalculateBearings result " +
                "for movement from $start to $end"
            )
        }
    }

    // Test 7: testTeeToPin
    @Test
    fun testTeeToPin() {
        // Given: Typical golf hole - tee to pin (par 4, ~350 yards)
        val teeLocation = Location(lat = 40.123000, long = -74.987000)    // Tee box
        val pinLocation = Location(lat = 40.126000, long = -74.983000)    // Flag location
        
        // When: Calculate camera position for hole overview
        val result = calculateMapCameraPosition(teeLocation, pinLocation)
        
        // Then: Should center between tee and pin
        val expectedCenterLat = (40.123000 + 40.126000) / 2
        val expectedCenterLng = (-74.987000 + (-74.983000)) / 2
        
        assertEquals(expectedCenterLat, result.centerLat, 0.000001)
        assertEquals(expectedCenterLng, result.centerLng, 0.000001)
        assertEquals(CalculateMapCameraPosition.DEFAULT_ZOOM_LEVEL, result.zoom)
        
        // Bearing should be realistic (between 0-360)
        assertTrue(result.bearing >= 0.0f && result.bearing <= 360.0f, "Bearing should be valid compass direction")
    }

    // Test 8: testChipShot
    @Test
    fun testChipShot() {
        // Given: Chip shot scenario (~30 yards)
        val ballLocation = Location(lat = 40.123456, long = -74.987654)
        val pinLocation = Location(lat = 40.123726, long = -74.987354)    // ~30 yards northeast
        
        // When: Calculate camera position for chip shot
        val result = calculateMapCameraPosition(ballLocation, pinLocation, 19.0f) // Higher zoom for short shot
        
        // Then: Should handle very precise coordinates
        val expectedCenterLat = (40.123456 + 40.123726) / 2
        val expectedCenterLng = (-74.987654 + (-74.987354)) / 2
        
        assertEquals(expectedCenterLat, result.centerLat, 0.000001)
        assertEquals(expectedCenterLng, result.centerLng, 0.000001)
        assertEquals(19.0f, result.zoom) // Custom zoom for close-up view
        
        // Should calculate bearing for short distance accurately
        assertTrue(result.bearing >= 0.0f && result.bearing <= 360.0f)
    }

    // Test 9: testLongHole
    @Test
    fun testLongHole() {
        // Given: Long par 5 hole (~550 yards)
        val teeLocation = Location(lat = 40.120000, long = -74.990000)
        val pinLocation = Location(lat = 40.135000, long = -74.975000)    // Long distance
        
        // When: Calculate camera position for long hole
        val result = calculateMapCameraPosition(teeLocation, pinLocation, 14.0f) // Lower zoom for overview
        
        // Then: Should handle large distance between points
        val expectedCenterLat = (40.120000 + 40.135000) / 2
        val expectedCenterLng = (-74.990000 + (-74.975000)) / 2
        
        assertEquals(expectedCenterLat, result.centerLat, 0.000001)
        assertEquals(expectedCenterLng, result.centerLng, 0.000001)
        assertEquals(14.0f, result.zoom) // Custom zoom for wide view
        
        // Bearing should be calculated for long distance
        assertTrue(result.bearing >= 0.0f && result.bearing <= 360.0f)
    }

    // Test 10: testSameLocation  
    @Test
    fun testSameLocation() {
        // Given: Same location (e.g., showing current position on green)
        val location = Location(lat = 40.123456, long = -74.987654)
        
        // When: Calculate camera position for same start/end
        val result = calculateMapCameraPosition(location, location)
        
        // Then: Center should be the exact location
        assertEquals(location.lat, result.centerLat, 0.000001)
        assertEquals(location.long, result.centerLng, 0.000001)
        assertEquals(CalculateMapCameraPosition.DEFAULT_ZOOM_LEVEL, result.zoom)
        
        // Bearing should be calculated (even if 0 due to no movement)
        assertTrue(result.bearing >= 0.0f && result.bearing <= 360.0f)
    }
}