package com.example.pokerunwearos.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.ExerciseType
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.scrollAway
import com.example.pokerunwearos.presentation.ui.utils.RUNNING
import com.example.pokerunwearos.presentation.ui.utils.TREADMILL
import com.example.pokerunwearos.presentation.ui.utils.WALKING
import com.example.pokerunwearos.presentation.ui.utils.exerciseTypes
import com.example.pokerunwearos.presentation.ui.widgets.CenteredRow

@Composable
fun ExerciseSelectionScreen(
    hasCapabilities: (Array<ExerciseType>, Boolean) -> Boolean,
    setExercise: (String) -> Unit,
    navigateToUnavailable: () -> Unit = {},
    navigateToNextScreen: () -> Unit = {},
    navigateBack: () -> Unit = {},
) {
    if (!hasCapabilities(exerciseTypes, false)) navigateToUnavailable()

    val listState = rememberScalingLazyListState()

    Scaffold(timeText = {
        TimeText(
            timeSource = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()),
            modifier = Modifier.scrollAway(listState)
        )
    }, vignette = {
        Vignette(vignettePosition = VignettePosition.TopAndBottom)
    }, positionIndicator = {
        PositionIndicator(
            scalingLazyListState = listState
        )
    }) {
        ScalingLazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            autoCentering = AutoCenteringParams(itemIndex = 0),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
        ) {
            item {
                ListHeader {
                    Text(text = "Select exercise")
                }
            }
            item {
                ExerciseSelectionButton(
                    onClick = {
                        setExercise(RUNNING)
                        navigateToNextScreen()
                    },
                    enabled = hasCapabilities(arrayOf(ExerciseType.RUNNING), false),
                    text = RUNNING
                )
            }
            item {
                ExerciseSelectionButton(
                    onClick = {
                        setExercise(TREADMILL)
                        navigateToNextScreen()
                    },
                    enabled = hasCapabilities(arrayOf(ExerciseType.RUNNING_TREADMILL), false),
                    text = TREADMILL
                )
            }
            item {
                ExerciseSelectionButton(
                    onClick = {
                        setExercise(WALKING)
                        navigateToNextScreen()
                    },
                    enabled = hasCapabilities(arrayOf(ExerciseType.WALKING), false),
                    text = WALKING
                )
            }
            item {
                ExerciseSelectionButton(onClick = {
                    navigateBack()
                }, text = "Back")
            }
        }
    }
}

@Composable
fun ExerciseSelectionButton(
    onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, text: String
) {
    CenteredRow {
        Button(onClick = onClick, modifier = modifier.fillMaxWidth(), enabled = enabled) {
            Text(text = text)
        }
    }
}