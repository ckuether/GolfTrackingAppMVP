package com.example.location_domain.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.location_domain.domain.permissions.PermissionResult
import com.example.location_domain.domain.permissions.RequestLocationPermission
import com.example.shared.platform.ActivityContextProvider
import com.example.shared.platform.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidRequestLocationPermission(
    private val context: Context,
    private val logger: Logger
) : RequestLocationPermission {

    companion object {
        private const val TAG = "AndroidRequestLocationPermission"
    }

    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null

    override suspend fun invoke(): PermissionResult {
        logger.debug(TAG, "Starting location permission request")

        val activity = ActivityContextProvider.getCurrentActivity()
        if (activity == null) {
            logger.error(TAG, "No ComponentActivity available, cannot request permissions")
            return PermissionResult.Denied
        }

        logger.debug(TAG, "ComponentActivity found: ${activity.javaClass.simpleName}")

        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

        // Check if permissions are already granted
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, fineLocationPermission
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context, coarseLocationPermission
        ) == PackageManager.PERMISSION_GRANTED

        logger.debug(TAG, "Permission status - Fine: $hasFineLocation, Coarse: $hasCoarseLocation")

        if (hasFineLocation || hasCoarseLocation) {
            logger.info(TAG, "Location permission already granted")
            return PermissionResult.Granted
        }

        // Request permissions
        logger.info(TAG, "Requesting location permissions from user")

        return suspendCancellableCoroutine { continuation ->
            try {
                permissionLauncher = activity.activityResultRegistry.register(
                    "location_permission_request",
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    logger.debug(TAG, "Permission result received: $permissions")

                    val granted = permissions[fineLocationPermission] == true ||
                            permissions[coarseLocationPermission] == true

                    if (granted) {
                        logger.info(TAG, "Location permission granted by user")
                        continuation.resume(PermissionResult.Granted)
                    } else {
                        // Check if user selected "Don't ask again"
                        val shouldShowRationaleFine =
                            activity.shouldShowRequestPermissionRationale(fineLocationPermission)
                        val shouldShowRationaleCoarse =
                            activity.shouldShowRequestPermissionRationale(coarseLocationPermission)

                        logger.debug(
                            TAG,
                            "Permission denied. Show rationale - Fine: $shouldShowRationaleFine, Coarse: $shouldShowRationaleCoarse"
                        )

                        if (!shouldShowRationaleFine && !shouldShowRationaleCoarse) {
                            logger.warn(TAG, "Location permission permanently denied")
                            continuation.resume(PermissionResult.PermanentlyDenied)
                        } else {
                            logger.warn(TAG, "Location permission denied")
                            continuation.resume(PermissionResult.Denied)
                        }
                    }
                    permissionLauncher?.unregister()
                    permissionLauncher = null
                }

                continuation.invokeOnCancellation {
                    logger.debug(TAG, "Permission request cancelled")
                    permissionLauncher?.unregister()
                    permissionLauncher = null
                }

                logger.debug(TAG, "Launching permission request dialog")
                permissionLauncher?.launch(
                    arrayOf(
                        fineLocationPermission,
                        coarseLocationPermission
                    )
                )

            } catch (e: Exception) {
                logger.error(TAG, "Error requesting permissions", e)
                continuation.resume(PermissionResult.Denied)
            }
        }
    }
}