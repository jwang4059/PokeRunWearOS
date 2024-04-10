package com.example.pokerunwearos.presentation.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseGoalType
import androidx.health.services.client.data.LocationAvailability
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.example.pokerunwearos.R
import com.example.pokerunwearos.data.models.Workout
import com.example.pokerunwearos.data.repository.health.ServiceState
import com.example.pokerunwearos.presentation.service.ExerciseStateChange
import com.example.pokerunwearos.presentation.ui.utils.ElapsedTime
import com.example.pokerunwearos.presentation.ui.utils.MeasurementUnit
import com.example.pokerunwearos.presentation.ui.utils.formatCalories
import com.example.pokerunwearos.presentation.ui.utils.formatDistance
import com.example.pokerunwearos.presentation.ui.utils.formatElapsedTime
import com.example.pokerunwearos.presentation.ui.utils.formatPace
import com.example.pokerunwearos.presentation.ui.utils.formatSpeed
import com.example.pokerunwearos.presentation.ui.utils.toFormattedString
import com.example.pokerunwearos.presentation.ui.widgets.CenteredColumn
import com.example.pokerunwearos.presentation.ui.widgets.CenteredRow
import com.example.pokerunwearos.presentation.ui.widgets.LocationIcon
import com.example.pokerunwearos.presentation.ui.widgets.Section
import com.example.pokerunwearos.presentation.ui.widgets.VerticalDivider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.Date
import kotlin.math.roundToInt
import kotlin.time.toKotlinDuration

@OptIn(ExperimentalFoundationApi::class, ExperimentalWearMaterialApi::class)
@Composable
fun TrackWorkoutScreen(
    serviceState: ServiceState,
    measurementUnit: MeasurementUnit = MeasurementUnit.IMPERIAL,
    onResumeClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onEndClick: () -> Unit = {},
    saveWorkout: (workout: Workout) -> Unit = {},
    navigateToSettings: () -> Unit = {},
    navigateToExerciseSelection: () -> Unit = {},
    navigateToPostWorkout: () -> Unit = {},
    navigateToMain: () -> Unit = {},
) {
    val chronoTickJob = remember { mutableStateOf<Job?>(null) }

    when (serviceState) {
        is ServiceState.Connected -> {
            val scope = rememberCoroutineScope()
            val getExerciseServiceState by serviceState.exerciseServiceState.collectAsStateWithLifecycle()
            val location by serviceState.locationAvailabilityState.collectAsStateWithLifecycle()
            val (_, exerciseMetrics, exerciseLaps, _, exerciseStateChange, exerciseConfig) = getExerciseServiceState
            var baseActiveDuration by remember { mutableStateOf(Duration.ZERO) }
            var activeDuration by remember { mutableStateOf(Duration.ZERO) }

            // Heart Rate
            val tempHeartRate = remember { mutableStateOf(0.0) }
            if (exerciseMetrics?.getData(DataType.HEART_RATE_BPM)?.isNotEmpty() == true) {
                tempHeartRate.value = exerciseMetrics.getData(DataType.HEART_RATE_BPM).last().value
            } else {
                tempHeartRate.value = tempHeartRate.value
            }

            // Average Heart Rate
            val averageHeartRate = exerciseMetrics?.getData(DataType.HEART_RATE_BPM_STATS)?.average
            val tempAverageHeartRate = remember { mutableStateOf(0.0) }

            // Distance
            val distance = exerciseMetrics?.getData(DataType.DISTANCE_TOTAL)?.total
            val tempDistance = remember { mutableStateOf(0.0) }

            // Pace
            val tempPace = remember { mutableStateOf(0.0) }
            if (exerciseMetrics?.getData(DataType.PACE)?.isNotEmpty() == true) {
                tempPace.value = exerciseMetrics.getData(DataType.PACE).last().value
            } else {
                tempPace.value = tempPace.value
            }

            // Average Pace
            val averagePace = exerciseMetrics?.getData(DataType.PACE_STATS)?.average
            val tempAveragePace = remember { mutableStateOf(0.0) }

            // Calories
            val calories = exerciseMetrics?.getData(DataType.CALORIES_TOTAL)?.total
            val tempCalories = remember { mutableStateOf(0.0) }

            // Speed
            val tempSpeed = remember { mutableStateOf(0.0) }
            if (exerciseMetrics?.getData(DataType.SPEED)?.isNotEmpty() == true) {
                tempSpeed.value = exerciseMetrics.getData(DataType.SPEED).last().value
            } else {
                tempSpeed.value = tempSpeed.value
            }

            // Average Speed
            val averageSpeed = exerciseMetrics?.getData(DataType.SPEED_STATS)?.average
            val tempAverageSpeed = remember { mutableStateOf(0.0) }

            // Steps
            val steps = exerciseMetrics?.getData(DataType.STEPS_TOTAL)?.total
            val tempSteps: MutableState<Long> = remember { mutableStateOf(0) }

            val elapsedTime = remember {
                derivedStateOf {
                    formatElapsedTime(
                        time = ElapsedTime.ElapsedTimeDuration(activeDuration.toKotlinDuration()),
                        includeSeconds = true,
                    ).toString()
                }
            }

            fun endWorkout() {
                saveWorkout(
                    Workout(
                        exerciseType = exerciseConfig?.exerciseType,
                        timeMillis = activeDuration.toMillis(),
                        distance = tempDistance.value,
                        steps = tempSteps.value,
                        calories = tempCalories.value,
                        avgSpeed = tempAverageSpeed.value,
                        avgPace = tempAveragePace.value,
                        avgHeartRate = tempAverageHeartRate.value,
                        distanceGoal = exerciseConfig?.exerciseGoals?.filter {
                            it.exerciseGoalType == ExerciseGoalType.ONE_TIME_GOAL && it.dataTypeCondition.dataType == DataType.DISTANCE_TOTAL && it.dataTypeCondition.comparisonType == ComparisonType.GREATER_THAN_OR_EQUAL
                        }
                            ?.maxByOrNull { it.dataTypeCondition.threshold.toDouble() }?.dataTypeCondition?.threshold?.toDouble(),
                        date = Date(),
                    )
                )
                navigateToPostWorkout()
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
                if (exerciseStateChange.exerciseState.isEnding || exerciseStateChange.exerciseState.isEnded) endWorkout()
            }

            val state = rememberLazyListState()
            val snappingLayout = remember(state) { SnapLayoutInfoProvider(state) }
            val flingBehavior = rememberSnapFlingBehavior(snappingLayout)

            val width = LocalConfiguration.current.screenWidthDp.dp

            val swipeableState = rememberSwipeableState(0)
            val sizePx = with(LocalDensity.current) { width.toPx() }
            val anchors = mapOf(-sizePx to 0, 0f to 1)

            Scaffold(vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            }, positionIndicator = {
                PositionIndicator(lazyListState = state)
            }) {
                Box(
                    modifier = Modifier.swipeable(
                        state = swipeableState,
                        anchors = anchors,
                        thresholds = { _, _ -> FractionalThreshold(0.5f) },
                        orientation = Orientation.Horizontal
                    )
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background),
                        state = state,
                        flingBehavior = flingBehavior
                    ) {
                        item {
                            WorkoutSection(
                                exerciseConfig = exerciseConfig,
                                heartRate = tempHeartRate.value,
                                location = location,
                                modifier = Modifier.fillParentMaxSize()
                            ) {
                                // Distance
                                val distanceStr = if (distance != null) formatDistance(
                                    distance, measurementUnit
                                ).toString() else formatDistance(
                                    tempDistance.value, measurementUnit
                                ).toString()

                                if (distance != null) tempDistance.value = distance

                                // Pace
                                val paceStr =
                                    formatPace(tempPace.value, measurementUnit).toString()

                                Column {
                                    WorkoutSectionRow(
                                        name1 = stringResource(id = R.string.duration),
                                        value1 = elapsedTime.value,
                                    )

                                    WorkoutSectionRow(
                                        name1 = stringResource(id = R.string.distance),
                                        value1 = distanceStr,
                                        name2 = stringResource(id = R.string.pace),
                                        value2 = paceStr
                                    )
                                }
                            }
                        }

                        // Calories , Speed
                        // Steps , Avg Pace
                        item {
                            WorkoutSection(
                                exerciseConfig = exerciseConfig,
                                heartRate = tempHeartRate.value,
                                location = location,
                                modifier = Modifier.fillParentMaxSize()
                            ) {
                                // Calories
                                val caloriesStr =
                                    if (calories != null) formatCalories(calories).toString() else formatCalories(
                                        tempCalories.value
                                    ).toString()

                                if (calories != null) tempCalories.value = calories

                                // Speed
                                val speedStr = formatSpeed(
                                    tempSpeed.value, measurementUnit
                                ).toString()

                                // Steps
                                val stepsStr = steps?.toString() ?: tempSteps.value.toString()

                                if (steps != null) tempSteps.value = steps

                                // Average Pace
                                val avgPaceStr = if (averagePace != null) formatPace(
                                    averagePace, measurementUnit
                                ).toString() else formatPace(
                                    tempAveragePace.value, measurementUnit
                                ).toString()

                                if (averagePace != null) tempAveragePace.value = averagePace

                                Column {
                                    WorkoutSectionRow(
                                        name1 = stringResource(id = R.string.calories),
                                        value1 = caloriesStr,
                                        name2 = stringResource(id = R.string.speed),
                                        value2 = speedStr
                                    )
                                    WorkoutSectionRow(
                                        name1 = stringResource(id = R.string.steps),
                                        value1 = stepsStr,
                                        name2 = stringResource(
                                            id = R.string.avgPace
                                        ),
                                        value2 = avgPaceStr,
                                    )
                                }
                            }
                        }
                    }

                    WorkoutMenu(exerciseConfig = exerciseConfig,
                        exerciseStateChange = exerciseStateChange,
                        elapsedTime = elapsedTime.value,
                        onResumeClick = { onResumeClick() },
                        onPauseClick = { onPauseClick() },
                        onEndClick = { onEndClick() },
                        navigateToSettings = { navigateToSettings() },
                        navigateToExerciseSelection = { navigateToExerciseSelection() },
                        navigateToMain = { navigateToMain() },
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    swipeableState.offset.value.roundToInt(), 0
                                )
                            }
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colors.background))
                }

                if (averageSpeed != null) {
                    tempAverageSpeed.value = averageSpeed
                }

                if (averageHeartRate != null) {
                    tempAverageHeartRate.value = averageHeartRate
                }
            }
        }

        else -> {}
    }
}

@Composable
fun WorkoutMenu(
    modifier: Modifier = Modifier,
    exerciseConfig: ExerciseConfig?,
    exerciseStateChange: ExerciseStateChange,
    elapsedTime: String,
    onResumeClick: () -> Unit = {},
    onPauseClick: () -> Unit = {},
    onEndClick: () -> Unit = {},
    navigateToSettings: () -> Unit = {},
    navigateToExerciseSelection: () -> Unit = {},
    navigateToMain: () -> Unit = {},
) {
    Section(modifier = modifier) {
        CenteredColumn {
            if (exerciseConfig != null) {
                Row {
                    Text(text = elapsedTime, fontSize = 14.sp)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // New Workout Button
                    WorkoutMenuButton(
                        onClick = { navigateToExerciseSelection() },
                        imageVector = Icons.Default.Add,
                        contextDescription = stringResource(id = R.string.newWorkout)
                    )


                    if (exerciseStateChange.exerciseState.isPaused) {
                        WorkoutMenuButton(
                            onClick = { onResumeClick() },
                            imageVector = Icons.Default.PlayArrow,
                            contextDescription = stringResource(id = R.string.resume)
                        )
                    } else {
                        WorkoutMenuButton(
                            onClick = { onPauseClick() },
                            imageVector = Icons.Default.Pause,
                            contextDescription = stringResource(id = R.string.pause)
                        )
                    }

                }

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Settings Button
                    WorkoutMenuButton(
                        onClick = { navigateToSettings() },
                        imageVector = Icons.Default.Settings,
                        contextDescription = stringResource(id = R.string.settings)
                    )

                    // Finish Button
                    WorkoutMenuButton(
                        onClick = { onEndClick() },
                        imageVector = Icons.Default.Close,
                        contextDescription = stringResource(id = R.string.finish)
                    )

                }
            } else {
                Button(onClick = {
                    navigateToMain()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Exit Workout")
                }
            }
        }
    }

}

@Composable
fun WorkoutMenuButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contextDescription: String,
) {
    CenteredColumn(
        modifier = Modifier.padding(4.dp)
    ) {
        Button(onClick = onClick) {
            Icon(
                imageVector = imageVector,
                contentDescription = contextDescription,
            )
        }
        Text(
            text = contextDescription, fontSize = 12.sp
        )
    }
}

@Composable
fun WorkoutSectionRow(
    modifier: Modifier = Modifier,
    name1: String,
    value1: String,
    name2: String? = null,
    value2: String? = null
) {
    Row(modifier = modifier) {
        Column {
            // Name Row
            CenteredRow(modifier = Modifier.fillMaxWidth()) {
                // Name 1
                CenteredColumn(modifier = Modifier.weight(1f)) {
                    Text(text = name1, fontSize = 12.sp)
                }
                // Name 2
                if (name2 != null) {
                    CenteredColumn(modifier = Modifier.weight(1f)) {
                        Text(text = name2, fontSize = 12.sp)
                    }
                }
            }
            // Values Row
            CenteredRow(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colors.primaryVariant)
                    .padding(8.dp, 4.dp)
            ) {

                // Value1
                CenteredColumn(modifier = Modifier.weight(1f)) {
                    Text(text = value1, fontSize = 20.sp)
                }

                if (value2 != null) {
                    Column {
                        VerticalDivider(height = 0.9f)
                    }

                    // Value 2
                    CenteredColumn(modifier = Modifier.weight(1f)) {
                        Text(text = value2, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutSection(
    modifier: Modifier = Modifier,
    exerciseConfig: ExerciseConfig?,
    heartRate: Double,
    location: LocationAvailability,
    content: @Composable (RowScope.() -> Unit)
) {
    Section(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Time
            CenteredRow(modifier = Modifier.fillMaxWidth()) {
                CenteredColumn {
                    CenteredRow {
                        if (exerciseConfig != null && exerciseConfig.isGpsEnabled) {
                            LocationIcon(locationAvailability = location)
                        }
                        Text(
                            text = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()).currentTime,
                            fontSize = 14.sp
                        )
                        if (exerciseConfig != null && exerciseConfig.isGpsEnabled) {
                            Spacer(modifier = Modifier.size(16.dp))
                        }
                    }
                    CenteredRow {
                        if (exerciseConfig != null) {
                            Text(
                                text = exerciseConfig.exerciseType.toFormattedString(),
                                color = MaterialTheme.colors.secondary,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }

            // Workout Stats
            CenteredRow(
                content = content, modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // Heart Rate
            CenteredRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                val heartRateStr = if (heartRate == 0.0) "__" else heartRate.toString()

                if (heartRate != 0.0) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = stringResource(id = R.string.heart_rate),
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(4.dp)
                    )
                }

                Text(text = heartRateStr, fontSize = 14.sp)
                if (heartRate != 0.0) {
                    Spacer(modifier = Modifier.size(16.dp))
                }
            }
        }
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