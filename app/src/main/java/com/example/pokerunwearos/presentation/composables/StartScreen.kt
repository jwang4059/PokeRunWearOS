package com.example.pokerunwearos.presentation.composables

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.DataTypeAvailability
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.example.pokerunwearos.R
import com.example.pokerunwearos.data.repository.health.MeasureMessage
import com.example.pokerunwearos.presentation.ui.widgets.HeartRateLabel
import com.example.pokerunwearos.presentation.ui.widgets.Section
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StartScreen(
    modifier: Modifier = Modifier,
    permissions: Array<String>,
    stepsDaily: Long?,
    hasHeartRateCapability: suspend () -> Boolean,
    heartRateMeasureFlow: () -> Flow<MeasureMessage>,
    navigateToExerciseSelection: () -> Unit = {},
) {
    var hasHeartRate by remember { mutableStateOf(false) }
    var hrBPM by remember { mutableStateOf(0.0) }
    var availability by remember { mutableStateOf(DataTypeAvailability.UNKNOWN) }

    val state = rememberLazyListState()
    val snappingLayout = remember(state) { SnapLayoutInfoProvider(state) }
    val flingBehavior = rememberSnapFlingBehavior(snappingLayout)

    LaunchedEffect(Unit) {
        launch {
            hasHeartRate = hasHeartRateCapability()
        }

        launch {
            heartRateMeasureFlow().collect { measureMessage ->
                when (measureMessage) {
                    is MeasureMessage.MeasureAvailability -> {
                        availability = measureMessage.availability
                    }

                    is MeasureMessage.MeasureData -> {
                        hrBPM = measureMessage.data.last().value
                    }
                }
            }
        }
    }

    Scaffold(timeText = {
        TimeText(
            timeSource = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()),
        )
    }, vignette = {
        Vignette(vignettePosition = VignettePosition.TopAndBottom)
    }, positionIndicator = {
        PositionIndicator(lazyListState = state)
    }, modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            state = state,
            flingBehavior = flingBehavior
        ) {
            item {
                CharacterProfileSection(
                    permissions = permissions,
                    hrBPM = hrBPM,
                    availability = availability,
                    stepsDaily = stepsDaily,
                    modifier = Modifier.fillParentMaxSize()
                )
            }
            item {
                StartWorkoutSection(
                    navigateToExerciseSelection = navigateToExerciseSelection,
                    modifier = Modifier.fillParentMaxSize()
                )
            }

        }
    }
}

@Composable
fun CharacterProfileSection(
    modifier: Modifier = Modifier,
    permissions: Array<String>,
    hrBPM: Double,
    availability: DataTypeAvailability,
    stepsDaily: Long?
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            Log.d("Permissions", "All required permissions granted")
        } else {
            Log.d("Permissions", "Missing permissions")
        }
    }

    LaunchedEffect(Unit) {
        launch {
            permissionLauncher.launch(permissions)
        }
    }


    Section(modifier = modifier) {
        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                HeartRateLabel(
                    hrBPM = hrBPM, availability = availability
                )
            }
            Row {
                Text(text = stepsDaily?.toString() ?: "N/A")
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.male),
                contentDescription = "Profile Image",
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
fun StartWorkoutSection(
    modifier: Modifier = Modifier,
    navigateToExerciseSelection: () -> Unit = {},
) {
    Section(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = "Start Workout",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { navigateToExerciseSelection() },
                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(id = R.string.start)
                    )
                }
            }
        }
    }
}


//@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    StartingUp(hasCapabilities = false, isTrackingAnotherExercise = false)
//}