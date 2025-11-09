package com.example.round_of_golf_domain.di

import com.example.round_of_golf_domain.data.repository.RoundOfGolfEventLocalRepository
import com.example.round_of_golf_domain.data.repository.RoundOfGolfEventLocalRepositoryImpl
import com.example.round_of_golf_domain.domain.usecase.TrackSingleRoundEvent
import com.example.round_of_golf_domain.domain.usecase.TrackHoleChangedEvent
import com.example.round_of_golf_domain.domain.usecase.CheckUserLocationInHoleBounds
import com.example.round_of_golf_domain.domain.usecase.GetTrackedShotsForHole
import com.example.round_of_golf_domain.domain.usecase.GetShotDistance
import com.example.round_of_golf_domain.domain.usecase.UpdateHoleStatsFromTrackedShots
import org.koin.dsl.module
val roundOfGolfDomainModule = module {
    // Use Cases - Single Responsibility Principle
    single { TrackSingleRoundEvent(get()) }
    single { TrackHoleChangedEvent(get()) }
    single { CheckUserLocationInHoleBounds() }
    single { GetTrackedShotsForHole(get()) }
    single { GetShotDistance() }
    single { UpdateHoleStatsFromTrackedShots(get(), get()) }

    // Repositories
    single<RoundOfGolfEventLocalRepository> {
        RoundOfGolfEventLocalRepositoryImpl(get())
    }
}