package com.example.location_domain.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.location_domain.permissions.AndroidCheckLocationPermission
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidCheckLocationPermissionTest {

    private val mockContext = mockk<Context>()
    private val androidCheckLocationPermission = AndroidCheckLocationPermission(mockContext)

    @Test
    fun testInvoke_FineLocationGranted_ReturnsTrue() = runTest {
        // Given: Fine location permission is granted, coarse is denied
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION) 
        } returns PackageManager.PERMISSION_GRANTED
        every { 
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_COARSE_LOCATION) 
        } returns PackageManager.PERMISSION_DENIED

        // When: Check location permission
        val result = androidCheckLocationPermission.invoke()

        // Then: Should return true (fine location is sufficient)
        assertTrue(result, "Should return true when fine location permission is granted")
        
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun testInvoke_CoarseLocationGranted_ReturnsTrue() = runTest {
        // Given: Coarse location permission is granted, fine is denied
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION) 
        } returns PackageManager.PERMISSION_DENIED
        every { 
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_COARSE_LOCATION) 
        } returns PackageManager.PERMISSION_GRANTED

        // When: Check location permission
        val result = androidCheckLocationPermission.invoke()

        // Then: Should return true (coarse location is sufficient)
        assertTrue(result, "Should return true when coarse location permission is granted")
        
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun testInvoke_BothPermissionsGranted_ReturnsTrue() = runTest {
        // Given: Both fine and coarse location permissions are granted
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION) 
        } returns PackageManager.PERMISSION_GRANTED
        every { 
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_COARSE_LOCATION) 
        } returns PackageManager.PERMISSION_GRANTED

        // When: Check location permission
        val result = androidCheckLocationPermission.invoke()

        // Then: Should return true (both permissions granted)
        assertTrue(result, "Should return true when both location permissions are granted")
        
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun testInvoke_NoPermissionsGranted_ReturnsFalse() = runTest {
        // Given: Neither fine nor coarse location permissions are granted
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION) 
        } returns PackageManager.PERMISSION_DENIED
        every { 
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_COARSE_LOCATION) 
        } returns PackageManager.PERMISSION_DENIED

        // When: Check location permission
        val result = androidCheckLocationPermission.invoke()

        // Then: Should return false (no permissions granted)
        assertFalse(result, "Should return false when no location permissions are granted")
        
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun testInvoke_CorrectPermissionConstants() = runTest {
        // Given: Mock to verify correct permission constants are used
        mockkStatic(ContextCompat::class)
        var fineLocationChecked = false
        var coarseLocationChecked = false
        
        every { 
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION) 
        } answers {
            fineLocationChecked = true
            PackageManager.PERMISSION_GRANTED
        }
        every { 
            ContextCompat.checkSelfPermission(mockContext, Manifest.permission.ACCESS_COARSE_LOCATION) 
        } answers {
            coarseLocationChecked = true
            PackageManager.PERMISSION_DENIED
        }

        // When: Check location permission
        androidCheckLocationPermission.invoke()

        // Then: Verify correct permissions were checked
        assertTrue(fineLocationChecked, "Should check ACCESS_FINE_LOCATION permission")
        assertTrue(coarseLocationChecked, "Should check ACCESS_COARSE_LOCATION permission")
        
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun testInvoke_UsesProvidedContext() = runTest {
        // Given: Mock to verify the provided context is used
        val specificMockContext = mockk<Context>()
        val permissionChecker = AndroidCheckLocationPermission(specificMockContext)
        
        mockkStatic(ContextCompat::class)
        every { 
            ContextCompat.checkSelfPermission(specificMockContext, any()) 
        } returns PackageManager.PERMISSION_GRANTED
        
        // Verify that calls with different context would fail
        every { 
            ContextCompat.checkSelfPermission(mockContext, any()) 
        } returns PackageManager.PERMISSION_DENIED

        // When: Check location permission
        val result = permissionChecker.invoke()

        // Then: Should use the specific context provided to constructor
        assertTrue(result, "Should use the context provided in constructor")
        
        unmockkStatic(ContextCompat::class)
    }
}