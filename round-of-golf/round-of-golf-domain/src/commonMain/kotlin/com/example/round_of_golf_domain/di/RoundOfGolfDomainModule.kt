package com.example.round_of_golf_domain.di

import com.example.round_of_golf_domain.data.repository.RoundOfGolfEventLocalRepository
import com.example.round_of_golf_domain.data.repository.RoundOfGolfEventLocalRepositoryImpl
import com.example.round_of_golf_domain.domain.usecase.TrackSingleRoundEventUseCase
import com.example.round_of_golf_domain.domain.usecase.TrackHoleChangedEventUseCase
import com.example.round_of_golf_domain.domain.usecase.CheckUserLocationInHoleBoundsUseCase
import com.example.round_of_golf_domain.domain.usecase.GetTrackedShotsForHoleUseCase
import com.example.round_of_golf_domain.domain.usecase.GetShotDistanceUseCase
import org.koin.dsl.module
val roundOfGolfDomainModule = module {
    // Use Cases - Single Responsibility Principle
    single { TrackSingleRoundEventUseCase(get()) }
    single { TrackHoleChangedEventUseCase(get()) }
    single { CheckUserLocationInHoleBoundsUseCase() }
    single { GetTrackedShotsForHoleUseCase(get()) }
    single { GetShotDistanceUseCase() }

    // Repositories
    single<RoundOfGolfEventLocalRepository> {
        RoundOfGolfEventLocalRepositoryImpl(get())
    }
}