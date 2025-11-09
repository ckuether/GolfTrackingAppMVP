package com.example.location_domain.domain.permissions

interface RequestLocationPermission {
    suspend operator fun invoke(): PermissionResult
}

sealed class PermissionResult {
    object Granted : PermissionResult()
    object Denied : PermissionResult()
    object PermanentlyDenied : PermissionResult()
}