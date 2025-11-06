package com.example.round_of_golf_domain.domain.usecase

import com.example.round_of_golf_domain.data.model.ShotTracked
import com.example.shared.data.model.GolfClubType
import com.example.shared.data.model.distanceToInYards

class GetShotDistanceUseCase {
    
    operator fun invoke(shot: ShotTracked): String {
        val distanceInYards = shot.startLocation.distanceToInYards(shot.endLocation)
        
        return if (shot.club == GolfClubType.Putter) {
            "${distanceInYards * 3} feet"
        } else {
            "$distanceInYards yards"
        }
    }
}