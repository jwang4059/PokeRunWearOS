package com.example.pokerunwearos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.health.services.client.data.DataType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
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
import com.example.pokerunwearos.R
import com.example.pokerunwearos.Screens
import com.example.pokerunwearos.data.ServiceState
import com.example.pokerunwearos.service.ExerciseStateChange
import com.example.pokerunwearos.ui.component.formatCalories
import com.example.pokerunwearos.ui.component.formatDistanceKm
import com.example.pokerunwearos.ui.component.formatDistanceMi
import com.example.pokerunwearos.ui.component.formatElapsedTime
import com.example.pokerunwearos.ui.component.formatPaceMinPerMi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.time.toKotlinDuration

@Composable
fun ExerciseScreen(
    onPauseClick: () -> Unit = {},
    onEndClick: () -> Unit = {},
    onResumeClick: () -> Unit = {},
    onStartClick: () -> Unit = {},
    serviceState: ServiceState,
    navController: NavHostController,
    ) {
    val chronoTickJob = remember { mutableStateOf<Job?>(null) }

    when (serviceState) {
        is ServiceState.Connected -> {
            val scope = rememberCoroutineScope()
            val getExerciseServiceState by serviceState.exerciseServiceState.collectAsStateWithLifecycle()
            val (_, exerciseMetrics, exerciseLaps, _, exerciseStateChange) = getExerciseServiceState
            var baseActiveDuration by remember { mutableStateOf(Duration.ZERO) }
            var activeDuration by remember { mutableStateOf(Duration.ZERO) }

            val tempHeartRate = remember { mutableStateOf(0.0) }
            if (exerciseMetrics?.getData(DataType.HEART_RATE_BPM)?.isNotEmpty() == true) {
                tempHeartRate.value = exerciseMetrics.getData(DataType.HEART_RATE_BPM).last().value
            } else {
                tempHeartRate.value = tempHeartRate.value
            }

            val averageHeartRate = exerciseMetrics?.getData(DataType.HEART_RATE_BPM_STATS)?.average
            val tempAverageHeartRate = remember { mutableStateOf(0.0) }

            val distance = exerciseMetrics?.getData(DataType.DISTANCE_TOTAL)?.total
            val tempDistance = remember { mutableStateOf(0.0) }

            val tempPace = remember { mutableStateOf(0.0) }
            if (exerciseMetrics?.getData(DataType.PACE)?.isNotEmpty() == true) {
                tempPace.value = exerciseMetrics.getData(DataType.PACE).last().value
            } else {
                tempPace.value = tempPace.value
            }

            val steps = exerciseMetrics?.getData(DataType.STEPS_TOTAL)?.total
            val tempSteps: MutableState<Long> = remember { mutableStateOf(0) }

            val calories = exerciseMetrics?.getData(DataType.CALORIES_TOTAL)?.total
            val tempCalories = remember { mutableStateOf(0.0) }


            val pauseOrResume = when (exerciseStateChange.exerciseState.isPaused) {
                true -> Icons.Default.PlayArrow
                false -> Icons.Default.Pause
            }

            val startOrEnd =
                when (exerciseStateChange.exerciseState.isEnded || exerciseStateChange.exerciseState.isEnding) {
                    true -> Icons.Default.PlayArrow
                    false -> Icons.Default.Stop
                }

            val elapsedTime = remember {
                derivedStateOf {
                    formatElapsedTime(
                        activeDuration.toKotlinDuration(), true
                    ).toString()
                }
            }

            LaunchedEffect(exerciseStateChange) {
                if (exerciseStateChange is ExerciseStateChange.ActiveStateChange) {
                    val timeOffset =
                        (System.currentTimeMillis() - exerciseStateChange.durationCheckPoint.time.toEpochMilli())
                    baseActiveDuration =
                        exerciseStateChange.durationCheckPoint.activeDuration.plusMillis(timeOffset)
                    chronoTickJob.value = startTick(chronoTickJob.value, scope) { tickerTime ->
                        activeDuration = baseActiveDuration.plusMillis(tickerTime)
                    }
                } else {
                    chronoTickJob.value?.cancel()
                }
            }

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
                        Column {
                            Text(text = stringResource(id = R.string.duration))
                            Text(elapsedTime.value)
                        }
                    }

                    item {
                        Column {
                            Text(text = stringResource(id = R.string.distance))
                            if (distance != null) {
                                Text(
                                    formatDistanceMi(distance).toString()
                                )
                                tempDistance.value = distance
                            } else {
                                Text(
                                    formatDistanceMi(tempDistance.value).toString()
                                )
                            }
                        }
                    }

                    item {
                        Column {
                            Text(text = stringResource(id = R.string.steps))
                            if (steps != null) {
                                Text(
                                    steps.toString()
                                )
                                tempSteps.value = steps
                            } else {
                                Text(
                                    tempSteps.value.toString()
                                )
                            }
                        }
                    }

                    item {
                        Column {
                            Text(text = stringResource(id = R.string.calories))
                            if (calories != null) {
                                Text(
                                    formatCalories(calories).toString()
                                )
                                tempCalories.value = calories
                            } else {
                                Text(
                                    formatCalories(tempCalories.value).toString()
                                )
                            }
                        }
                    }

                    item {
                        Column {
                            Text(text = stringResource(id = R.string.pace))
                            Text(
                                formatPaceMinPerMi(tempPace.value).toString()
                            )
                        }
                    }

                    item {
                        Row {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = stringResource(id = R.string.heart_rate)
                            )
                            Text(
                                tempHeartRate.value.toString()
                            )
                            if (averageHeartRate != null) {
                                tempAverageHeartRate.value = averageHeartRate
                            }
                        }
                    }

//                    item {
//                        Row {
//                            Icon(
//                                imageVector = Icons.Default._360,
//                                contentDescription = stringResource(id = R.string.laps)
//                            )
//                            Text(text = exerciseLaps.toString())
//
//                        }
//                    }

                    item {
                        if (exerciseStateChange.exerciseState.isEnding) {

                        navController.navigate(
                            Screens.SummaryScreen.route + "/${tempAverageHeartRate.value.toInt()}/${
                                formatDistanceKm(
                                    tempDistance.value
                                )
                            }/${formatCalories(tempCalories.value)}/" + formatElapsedTime(
                                activeDuration.toKotlinDuration(), true
                            ).toString()
                        ) { popUpTo(Screens.ExerciseScreen.route) { inclusive = true } }

                        Button(onClick = { onStartClick() }) {
                            Icon(
                                imageVector = startOrEnd, contentDescription = stringResource(
                                    id = R.string.startOrEnd
                                )
                            )
                        }

                        } else {
                            Button(onClick = { onEndClick() }) {
                                Icon(
                                    imageVector = startOrEnd, contentDescription = stringResource(
                                        id = R.string.startOrEnd
                                    )
                                )
                            }

                        }
                    }

                    item {
                        if (exerciseStateChange.exerciseState.isPaused) {
                            Button(onClick = {
                                onResumeClick()
                            }) {
                                Icon(
                                    imageVector = pauseOrResume,
                                    contentDescription = stringResource(id = R.string.pauseOrResume)
                                )
                            }
                        } else {
                            Button(onClick = {
                                onPauseClick()
                            }) {
                                Icon(
                                    imageVector = pauseOrResume,
                                    contentDescription = stringResource(id = R.string.pauseOrResume)
                                )
                            }

                        }
                    }
                }
            }
        }

        else -> {}
    }
}

private fun startTick(
    chronoTickJob: Job?, scope: CoroutineScope, block: (tickTime: Long) -> Unit
): Job? {
    if (chronoTickJob == null || !chronoTickJob.isActive) {
        return scope.launch {
            val tickStart = System.currentTimeMillis()
            while (isActive) {
                val tickSpan = System.currentTimeMillis() - tickStart
                block(tickSpan)
                delay(CHRONO_TICK_MS)
            }
        }
    }
    return null
}

const val CHRONO_TICK_MS = 200L