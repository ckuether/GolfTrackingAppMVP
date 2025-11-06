package org.example.arccosmvp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.example.core_ui.theme.GolfAppTheme
import com.example.core_ui.utils.UiEvent
import com.example.core_ui.utils.UiText
import com.example.shared.navigation.Route
import org.example.arccosmvp.presentation.GolfHomeScreen
import com.example.round_of_golf_presentation.presentation.RoundOfGolf
import org.example.arccosmvp.presentation.viewmodel.AppViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    GolfAppTheme {
        ArccosMVPApp()
    }
}

@Composable
fun ArccosMVPApp(){
    val navController = rememberNavController()
    val appViewModel: AppViewModel = koinViewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle UI events globally for all screens
    val uiEvent by appViewModel.uiEvent.collectAsStateWithLifecycle()
    
    LaunchedEffect(uiEvent) {
        uiEvent?.let { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    navController.navigate(event.route)
                }

                is UiEvent.NavigateAndClearBackStack -> {
                    navController.navigate(event.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                is UiEvent.NavigateUp -> {
                    navController.navigateUp()
                }

                is UiEvent.ShowSnackbar -> {
                    val message = when (val uiText = event.uiText) {
                        is UiText.DynamicString -> uiText.value
                        is UiText.StringResourceId -> {
                            "Error StringResourceId Toast Failure"
                        }
                    }
                    snackbarHostState.showSnackbar(message)
                }
                is UiEvent.ShowErrorSnackbar -> {
                    val message = when (val uiText = event.uiText) {
                        is UiText.DynamicString -> uiText.value
                        is UiText.StringResourceId -> {
                            "Error StringResourceId Toast Failure"
                        }
                    }
                    snackbarHostState.showSnackbar(message)
                }
                is UiEvent.IsLoading -> {
                    //TODO: Handle Loading
                }
            }
            // Clear the event after handling to prevent re-delivery
            appViewModel.clearUiEvent()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
        ) {
            NavHost(
                navController = navController,
                startDestination = Route.GOLF_HOME
            ) {
                composable(Route.GOLF_HOME) {
                    GolfHomeScreen(
                        appViewModel = appViewModel,
                        updateUiEvent = { uiEvent ->
                            appViewModel.updateUiEvent(uiEvent)
                        }
                    )
                }
                composable(Route.ROUND_OF_GOLF) {
                    val course by appViewModel.course.collectAsStateWithLifecycle()
                    val currentPlayer by appViewModel.currentPlayer.collectAsStateWithLifecycle()
                    if (course != null && currentPlayer != null) {
                        RoundOfGolf(
                            currentPlayer = currentPlayer!!,
                            golfCourse = course!!,
                            updateUiEvent = { uiEvent ->
                                appViewModel.updateUiEvent(uiEvent)
                            }
                        )
                    }
                }
            }
        }
    }
}

