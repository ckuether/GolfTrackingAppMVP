package com.example.round_of_golf_domain.di

import com.example.round_of_golf_domain.data.repository.RoundOfGolfEventLocalRepository
import com.example.round_of_golf_domain.data.repository.RoundOfGolfEventLocalRepositoryImpl
import com.example.round_of_golf_domain.domain.usecase.TrackSingleRoundEventUseCase
import org.koin.dsl.module
val roundOfGolfDomainModule = module {
    // Use Cases - Single Responsibility Principle
    single { TrackSingleRoundEventUseCase(get()) }

    // Repositories
    single<RoundOfGolfEventLocalRepository> {
        RoundOfGolfEventLocalRepositoryImpl(get())
    }
}