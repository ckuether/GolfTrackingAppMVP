package com.example.location_domain.domain.permissions

interface CheckLocationPermission {
    suspend operator fun invoke(): Boolean
}