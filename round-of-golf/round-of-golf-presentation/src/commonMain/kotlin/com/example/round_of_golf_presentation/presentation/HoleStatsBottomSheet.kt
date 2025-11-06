package com.example.round_of_golf_presentation.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.core_ui.components.DraggableBottomSheetWrapper
import com.example.core_ui.resources.LocalDimensionResources
import com.example.round_of_golf_domain.data.model.ShotTracked
import com.example.round_of_golf_domain.domain.usecase.GetTrackedShotsForHoleUseCase
import com.example.round_of_golf_domain.domain.usecase.GetShotDistanceUseCase
import com.example.shared.data.model.Hole
import com.example.shared.utils.StringResources
import com.example.core_ui.utils.UiText
import org.koin.compose.koinInject

@Composable
fun HoleStatsBottomSheet(
    currentHole: Hole?,
    currentHoleNumber: Int,
    totalHoles: Int,
    roundId: Long,
    existingScore: Int? = null,
    existingPutts: Int? = null,
    onDismiss: () -> Unit,
    onFinishHole: (score: Int, putts: Int?) -> Unit,
    prevHoleClicked: () -> Unit,
    nextHoleClicked: () -> Unit
) {
    DraggableBottomSheetWrapper(
        onDismiss = onDismiss,
        fillMaxHeight = null,
        dragOnlyFromHandle = true
    ) {
        HoleStats(
            currentHole = currentHole,
            currentHoleNumber = currentHoleNumber,
            totalHoles = totalHoles,
            roundId = roundId,
            existingScore = existingScore,
            existingPutts = existingPutts,
            onDismiss = onDismiss,
            onFinishHole = onFinishHole,
            prevHoleClicked = prevHoleClicked,
            nextHoleClicked = nextHoleClicked
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoleStats(
    currentHole: Hole?,
    currentHoleNumber: Int,
    totalHoles: Int,
    roundId: Long,
    existingScore: Int? = null,
    existingPutts: Int? = null,
    onDismiss: () -> Unit,
    onFinishHole: (score: Int, putts: Int?) -> Unit,
    prevHoleClicked: () -> Unit,
    nextHoleClicked: () -> Unit
) {
    val dimensions = LocalDimensionResources.current
    var selectedScore by remember(currentHoleNumber) { mutableStateOf(existingScore) }
    var selectedPutts by remember(currentHoleNumber) { mutableStateOf(existingPutts) }
    
    val getTrackedShotsUseCase: GetTrackedShotsForHoleUseCase = koinInject()
    val trackedShots by getTrackedShotsUseCase(roundId, currentHoleNumber).collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensions.paddingXXLarge)
    ) {
        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensions.buttonHeight + dimensions.paddingXXLarge + dimensions.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(dimensions.spacingMedium)
        ) {
            item {
                // Score section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = UiText.StringResourceId(StringResources.holeScoreTemplate, arrayOf(currentHoleNumber)).asString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = UiText.StringResourceId(StringResources.others).asString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                // Score selection grid
                val par = currentHole?.par ?: 4
                val scores = (1..9).toList()

                Column {
                    // First row: 1, 2, 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        scores.take(3).forEach { score ->
                            when {
                                score == par -> ParButton(
                                    isSelected = selectedScore == score,
                                    onClick = { selectedScore = score },
                                    par = par
                                )
                                else -> ScoreButton(
                                    score = score,
                                    isSelected = selectedScore == score,
                                    onClick = { selectedScore = score },
                                    par = par
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                    // Second row: 4, 5, 6
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        scores.drop(3).take(3).forEach { score ->
                            when {
                                score == par -> ParButton(
                                    isSelected = selectedScore == score,
                                    onClick = { selectedScore = score },
                                    par = par
                                )
                                else -> ScoreButton(
                                    score = score,
                                    isSelected = selectedScore == score,
                                    onClick = { selectedScore = score },
                                    par = par
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                    // Third row: 7, 8, 9
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        scores.drop(6).take(3).forEach { score ->
                            ScoreButton(
                                score = score,
                                isSelected = selectedScore == score,
                                onClick = { selectedScore = score },
                                par = par
                            )
                        }
                    }
                }
            }

            item {
                // Putts section
                Column {
                    Text(
                        text = UiText.StringResourceId(StringResources.putts).asString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(dimensions.spacingMedium))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (0..4).forEach { putts ->
                            PuttsButton(
                                putts = if (putts == 4) "≥4" else putts.toString(),
                                isSelected = selectedPutts == putts,
                                onClick = { selectedPutts = putts }
                            )
                        }
                    }
                }
            }

            // Tracked Shots section
            if (trackedShots.isNotEmpty()) {
                item {
                    Text(
                        text = "Tracked Shots",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                items(trackedShots) { shot ->
                    TrackedShotItem(
                        shot = shot,
                        dimensions = dimensions
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(dimensions.spacingMedium))
                }
            }
        }

        // Navigation and finish button container - anchored to bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensions.paddingXXLarge)
                .align(Alignment.BottomCenter)
        ) {
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Left arrow
            IconButton(
                onClick = {
                    prevHoleClicked()
                },
                enabled = currentHoleNumber > 1
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = UiText.StringResourceId(StringResources.previousHole).asString(),
                    tint = if (currentHoleNumber > 1) Color.Black else Color.Gray
                )
            }

            // Finish Hole button
            Button(
                onClick = {
                    selectedScore?.let { score ->
                        onFinishHole(score, selectedPutts)
                    }
                },
                enabled = selectedScore != null,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = dimensions.paddingLarge)
                    .height(dimensions.buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(dimensions.buttonCornerRadius)
            ) {
                Text(
                    text = if (currentHoleNumber == totalHoles) UiText.StringResourceId(StringResources.finishRound).asString() else UiText.StringResourceId(StringResources.finishHoleTemplate, arrayOf(currentHoleNumber)).asString(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Right arrow
            IconButton(
                onClick = {
                    nextHoleClicked()
                },
                enabled = currentHoleNumber < totalHoles
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = UiText.StringResourceId(StringResources.nextHole).asString(),
                    tint = if (currentHoleNumber < totalHoles) Color.Black else Color.Gray
                )
            }
            }
        }
    }
}

@Composable
private fun TrackedShotItem(
    shot: ShotTracked,
    dimensions: com.example.core_ui.resources.DimensionResources
) {
    val getShotDistanceUseCase: GetShotDistanceUseCase = koinInject()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensions.elevationSmall)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensions.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = shot.club.clubName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = getShotDistanceUseCase(shot),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
