package com.example.pokerunwearos.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseGoalType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.pokerunwearos.data.repository.health.ServiceState
import com.example.pokerunwearos.data.models.Workout
import com.example.pokerunwearos.presentation.service.ExerciseStateChange
import com.example.pokerunwearos.presentation.ui.utils.formatCalories
import com.example.pokerunwearos.presentation.ui.utils.formatDistanceMi
import com.example.pokerunwearos.presentation.ui.utils.formatElapsedTime
import com.example.pokerunwearos.presentation.ui.utils.formatPaceMinPerMi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.Date
import kotlin.time.toKotlinDuration

@Composable
fun TrackWorkoutScreen(
    onPauseClick: () -> Unit = {},
    onEndClick: () -> Unit = {},
    onResumeClick: () -> Unit = {},
    onStartClick: () -> Unit = {},
    serviceState: ServiceState,
    saveWorkout: (workout: Workout) -> Unit = {},
    navigateToPostWorkout: () -> Unit = {},
) {
    val chronoTickJob = remember { mutableStateOf<Job?>(null) }

    when (serviceState) {
        is ServiceState.Connected -> {
            val scope = rememberCoroutineScope()
            val getExerciseServiceState by serviceState.exerciseServiceState.collectAsStateWithLifecycle()
            val (_, exerciseMetrics, exerciseLaps, _, exerciseStateChange, exerciseConfig) = getExerciseServiceState
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
                    // Duration
                    item {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row {
                                Text(text = stringResource(id = R.string.duration))
                            }
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.large)
                                    .background(MaterialTheme.colors.primaryVariant)
                                    .padding(8.dp, 4.dp)
                            ) {
                                Text(elapsedTime.value)
                            }
                        }
                    }

                    // Distance + Steps
                    item {
                        Column {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.distance),
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.steps),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                val distanceStr =
                                    if (distance != null) formatDistanceMi(distance).toString() else formatDistanceMi(
                                        tempDistance.value
                                    ).toString()

                                if (distance != null) tempDistance.value = distance

                                val stepsStr = steps?.toString() ?: tempSteps.value.toString()

                                if (steps != null) tempSteps.value = steps

                                Row(
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.large)
                                        .background(MaterialTheme.colors.primaryVariant)
                                        .padding(8.dp, 4.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = distanceStr,
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = stepsStr,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Calories
                    item {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val caloriesStr =
                                if (calories != null) formatCalories(calories).toString() else formatCalories(
                                    tempCalories.value
                                ).toString()

                            if (calories != null) tempCalories.value = calories

                            Row {
                                Text(text = stringResource(id = R.string.calories))
                            }

                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.large)
                                    .background(MaterialTheme.colors.primaryVariant)
                                    .padding(8.dp, 4.dp)
                            ) {
                                Text(caloriesStr)
                            }
                        }
                    }

                    // Pace
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
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (exerciseStateChange.exerciseState.isEnded || exerciseStateChange.exerciseState.isEnding) {
                                Button(onClick = { onStartClick() }) {
                                    Icon(
                                        imageVector = startOrEnd,
                                        contentDescription = stringResource(
                                            id = R.string.startOrEnd
                                        )
                                    )
                                }

                            } else {
                                Button(onClick = {
                                    onEndClick()
                                    saveWorkout(
                                        Workout(
                                            exerciseType = exerciseConfig?.exerciseType.toString(),
                                            timeMillis = activeDuration.toMillis(),
                                            distance = tempDistance.value,
                                            pace = tempPace.value,
                                            steps = tempSteps.value,
                                            calories = tempCalories.value,
                                            avgHeartRate = tempAverageHeartRate.value,
                                            distanceGoal = exerciseConfig?.exerciseGoals?.filter {
                                                it.exerciseGoalType == ExerciseGoalType.ONE_TIME_GOAL && it.dataTypeCondition.dataType == DataType.DISTANCE_TOTAL && it.dataTypeCondition.comparisonType == ComparisonType.GREATER_THAN_OR_EQUAL
                                            }
                                                ?.maxByOrNull { it.dataTypeCondition.threshold.toDouble() }?.dataTypeCondition?.threshold?.toDouble(),
                                            date = Date(),
                                        )
                                    )
                                    navigateToPostWorkout()
                                }) {
                                    Icon(
                                        imageVector = startOrEnd,
                                        contentDescription = stringResource(
                                            id = R.string.startOrEnd
                                        )
                                    )
                                }
                            }
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