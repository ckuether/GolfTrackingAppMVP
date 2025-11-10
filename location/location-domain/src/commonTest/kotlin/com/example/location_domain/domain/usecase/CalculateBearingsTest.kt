import com.example.location_domain.domain.usecase.CalculateBearings
import com.example.shared.data.model.Location
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalculateBearingsTest {

    private val calculateBearings = CalculateBearings()

    @Test
    fun testBasicDirections_North() {
        // Given: Two locations with same longitude, end latitude higher
        val start = Location(lat = 40.0, long = -74.0) // New York area
        val end = Location(lat = 41.0, long = -74.0)   // 1 degree north
        
        // When: Calculate bearing
        val bearing = calculateBearings(start, end)
        
        // Then: Should be approximately 0 degrees (North)
        assertEquals(0.0, bearing, 1.0)
    }

    @Test
    fun testBasicDirections_South() {
        // Given: Two locations with same longitude, end latitude lower
        val start = Location(lat = 41.0, long = -74.0)
        val end = Location(lat = 40.0, long = -74.0)   // 1 degree south
        
        // When: Calculate bearing
        val bearing = calculateBearings(start, end)
        
        // Then: Should be approximately 180 degrees (South)
        assertEquals(180.0, bearing, 1.0)
    }

    @Test
    fun testBasicDirections_East() {
        // Given: Two locations with same latitude, end longitude higher
        val start = Location(lat = 40.0, long = -74.0)
        val end = Location(lat = 40.0, long = -73.0)   // 1 degree east
        
        // When: Calculate bearing
        val bearing = calculateBearings(start, end)
        
        // Then: Should be approximately 90 degrees (East)
        assertEquals(90.0, bearing, 1.0)
    }

    @Test
    fun testBasicDirections_West() {
        // Given: Two locations with same latitude, end longitude lower
        val start = Location(lat = 40.0, long = -73.0)
        val end = Location(lat = 40.0, long = -74.0)   // 1 degree west
        
        // When: Calculate bearing
        val bearing = calculateBearings(start, end)
        
        // Then: Should be approximately 270 degrees (West)
        assertEquals(270.0, bearing, 1.0)
    }

    @Test
    fun testDiagonalShots_Northeast() {
        // Given: Movement northeast (common golf shot direction)
        val start = Location(lat = 40.0, long = -74.0)
        val end = Location(lat = 40.5, long = -73.5)   // Northeast diagonal
        
        // When: Calculate bearing
        val bearing = calculateBearings(start, end)
        
        // Then: Should be in northeast quadrant (0-90 degrees)
        assertTrue(bearing > 30.0 && bearing < 60.0, "Bearing should be in northeast quadrant")
    }

    @Test
    fun testDiagonalShots_Southeast() {
        // Given: Movement southeast
        val start = Location(lat = 40.0, long = -74.0)
        val end = Location(lat = 39.5, long = -73.5)   // Southeast diagonal
        
        // When: Calculate bearing
        val bearing = calculateBearings(start, end)
        
        // Then: Should be in southeast quadrant (90-180 degrees)
        assertTrue(bearing > 120.0 && bearing < 160.0, "Bearing should be in southeast quadrant")
    }

    @Test
    fun testDiagonalShots_Southwest() {
        // Given: Movement southwest
        val start = Location(lat = 40.0, long = -74.0)
        val end = Location(lat = 39.5, long = -74.5)   // Southwest diagonal
        
        // When: Calculate bearing
        val bearing = calculateBearings(start, end)
        
        // Then: Should be in southwest quadrant (180-270 degrees)
        assertTrue(bearing > 200.0 && bearing < 240.0, "Bearing should be in southwest quadrant")
    }

    @Test
    fun testDiagonalShots_Northwest() {
        // Given: Movement northwest
        val start = Location(lat = 40.0, long = -74.0)
        val end = Location(lat = 40.5, long = -74.5)   // Northwest diagonal
        
        // When: Calculate bearing
        val bearing = calculateBearings(start, end)
        
        // Then: Should be in northwest quadrant (270-360 degrees)
        assertTrue(bearing > 300.0 && bearing < 340.0, "Bearing should be in northwest quadrant")
    }

    @Test
    fun testSameLocation() {
        // Given: Same starting and ending location (player hasn't moved)
        val location = Location(lat = 40.7128, long = -74.0060) // NYC coordinates
        
        // When: Calculate bearing between identical locations
        val bearing = calculateBearings(location, location)
        
        // Then: Should return 0.0 degrees (North) as default when no movement
        assertEquals(0.0, bearing, 0.001)
    }

    @Test
    fun testSameLocation_WithGPSPrecisionVariance() {
        // Given: Very slight differences due to GPS precision (common in golf)
        val start = Location(lat = 40.712800, long = -74.006000)
        val end = Location(lat = 40.712801, long = -74.006001)   // 0.000001° difference
        
        // When: Calculate bearing
        val bearing = calculateBearings(start, end)
        
        // Then: Should handle micro-movements gracefully (any valid bearing is fine)
        assertTrue(bearing >= 0.0 && bearing <= 360.0, "Bearing should be valid (0-360°)")
    }
}