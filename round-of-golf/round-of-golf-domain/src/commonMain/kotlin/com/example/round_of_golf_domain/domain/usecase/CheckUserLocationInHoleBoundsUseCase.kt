package com.example.round_of_golf_domain.domain.usecase

import com.example.shared.data.model.Location
import com.example.shared.data.model.Hole
import com.example.shared.data.model.distanceToInYards
import com.example.shared.data.model.midPoint

class CheckUserLocationInHoleBoundsUseCase {
    
    operator fun invoke(
        userLocation: Location,
        hole: Hole,
        bufferYards: Int = 150
    ): Boolean {
        val teeLocation = hole.teeLocation
        val flagLocation = hole.flagLocation
        
        val distanceToTee = userLocation.distanceToInYards(teeLocation)
        val distanceToFlag = userLocation.distanceToInYards(flagLocation)
        val holeLength = teeLocation.distanceToInYards(flagLocation)
        
        // Check if user is within reasonable distance of either tee or flag
        val withinTeeRange = distanceToTee <= bufferYards
        val withinFlagRange = distanceToFlag <= bufferYards
        
        // Check if user is somewhere along the hole corridor (not too far from the direct line)
        val midPoint = teeLocation.midPoint(flagLocation)
        val distanceToHoleLine = userLocation.distanceToInYards(midPoint)
        val withinHoleCorridor = distanceToHoleLine <= (holeLength / 4).coerceAtLeast(bufferYards)
        
        return withinTeeRange || withinFlagRange || withinHoleCorridor
    }
}