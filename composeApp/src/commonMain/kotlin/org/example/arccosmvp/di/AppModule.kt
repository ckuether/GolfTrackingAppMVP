package org.example.arccosmvp.di

import com.example.shared.data.model.Course
import com.example.shared.data.repository.GolfCourseRepository
import com.example.shared.data.repository.UserRepository
import com.example.shared.data.repository.UserRepositoryImpl
import com.example.shared.usecase.LoadGolfCourse
import com.example.shared.usecase.LoadCurrentUser
import com.example.round_of_golf_domain.domain.usecase.SaveScoreCard
import com.example.shared.usecase.GetAllScoreCards
import com.example.round_of_golf_presentation.RoundOfGolfViewModel
import com.example.shared.data.model.Player
import org.example.arccosmvp.presentation.viewmodel.AppViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {

    single<GolfCourseRepository> { GolfCourseRepository(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    
    // UseCases
    factoryOf(::LoadGolfCourse)
    factoryOf(::LoadCurrentUser)
    factoryOf(::SaveScoreCard)
    factoryOf(::GetAllScoreCards)
    
    factory { (course: Course, player: Player) ->
        RoundOfGolfViewModel(
            course = course,
            currentPlayer = player,
            locationTrackingService = get(),
            trackEventUseCase = get(),
            checkLocationPermission = get(),
            requestLocationPermission = get(),
            saveScoreCard = get(),
            logger = get()
        )
    }
    viewModelOf(::AppViewModel)
}