package com.example.pokerunwearos.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Button
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
import com.example.pokerunwearos.presentation.ui.widgets.ExerciseInProgressAlert

@Composable
fun ExerciseSelectionScreen(
    hasCapabilities: () -> Boolean,
    isTrackingAnotherExercise: Boolean,
    setExercise: (String) -> Unit,
    navigateToUnavailable: () -> Unit = {},
    navigateToNextScreen: () -> Unit = {},
    navigateBack: () -> Unit = {},
) {
    if (!hasCapabilities()) navigateToUnavailable()
    else if (isTrackingAnotherExercise) ExerciseInProgressAlert(isTrackingExercise = true)

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
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            autoCentering = AutoCenteringParams(itemIndex = 0),
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    Button(
                        onClick = {
                            setExercise(RUNNING)
                            navigateToNextScreen()
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Running")
                    }
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    Button(
                        onClick = {
                            setExercise(TREADMILL)
                            navigateToNextScreen()
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Running on Treadmill")
                    }
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    Button(
                        onClick = {
                            setExercise(WALKING)
                            navigateToNextScreen()
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Walking")
                    }
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    Button(
                        onClick = {
                            navigateBack()
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Back")
                    }
                }
            }
        }
    }
}