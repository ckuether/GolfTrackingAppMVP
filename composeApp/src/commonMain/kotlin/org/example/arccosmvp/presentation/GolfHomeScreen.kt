package org.example.arccosmvp.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.layout.ContentScale
import com.example.shared.utils.StringResources
import com.example.core_ui.components.RoundedButton
import com.example.core_ui.resources.LocalDimensionResources
import com.example.core_ui.utils.UiEvent
import com.example.core_ui.utils.UiText
import com.example.shared.navigation.Route
import org.example.arccosmvp.presentation.viewmodel.AppViewModel
import com.example.shared.utils.DrawableResources
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GolfHomeScreen(
    appViewModel: AppViewModel,
    updateUiEvent: (UiEvent) -> Unit,
) {
    val dimensions = LocalDimensionResources.current
    val allScoreCards by appViewModel.allScoreCards.collectAsStateWithLifecycle(emptyList())
    val course by appViewModel.course.collectAsStateWithLifecycle()
    var showPreviousRounds by remember { mutableStateOf(false) }

    //TODO: This BackHandler is Experimental and may break in the future
    BackHandler {
        if(showPreviousRounds){
            showPreviousRounds = false
            return@BackHandler
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(DrawableResources.GolfBg),
            contentDescription = UiText.StringResourceId(StringResources.golfCourseBackground).asString(),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .safeContentPadding()
                .padding(dimensions.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = UiText.StringResourceId(StringResources.welcomeToBrokenTee).asString(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            
            Spacer(modifier = Modifier.height(dimensions.spacingXLarge))
            
            RoundedButton(
                modifier = Modifier
                    .padding(vertical = dimensions.paddingMedium),
                text = if (course == null) UiText.StringResourceId(StringResources.loadingCourse).asString() else UiText.StringResourceId(StringResources.startRound).asString(),
                enabled = course != null,
                onClick = {
                    updateUiEvent(UiEvent.NavigateAndClearBackStack(Route.ROUND_OF_GOLF))
                }
            )


            RoundedButton(
                modifier = Modifier
                    .padding(vertical = dimensions.paddingMedium),
                text = UiText.StringResourceId(StringResources.pastRounds).asString(),
                onClick = {
                    showPreviousRounds = true
                }
            )
        }
        
        // Previous Rounds Bottom Sheet
        if (showPreviousRounds) {
            PreviousRoundsBottomSheet(
                scoreCards = allScoreCards,
                onDismiss = { showPreviousRounds = false }
            )
        }
    }
}