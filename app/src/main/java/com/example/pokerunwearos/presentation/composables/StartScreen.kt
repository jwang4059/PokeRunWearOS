package com.example.pokerunwearos.presentation.composables

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.example.pokerunwearos.presentation.ui.widgets.CenteredColumn
import com.example.pokerunwearos.presentation.ui.widgets.HeartRateLabel
import com.example.pokerunwearos.presentation.ui.widgets.Section
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StartScreen(
    modifier: Modifier = Modifier,
    permissions: Array<String>,
    skipPrompt: Boolean?,
    gender: String?,
    stepsDaily: Long?,
    hrBPM: Double,
    hrAvailability: DataTypeAvailability,
    setHrEnabled: (Boolean) -> Unit = {},
    navigateToExerciseSelection: () -> Unit = {},
    navigateToSummary: () -> Unit = {},
    navigateToSettings: () -> Unit = {}
) {
    val state = rememberLazyListState()
    val snappingLayout = remember(state) { SnapLayoutInfoProvider(state) }
    val flingBehavior = rememberSnapFlingBehavior(snappingLayout)

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
            setHrEnabled(true)
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
                    gender = gender,
                    hrBPM = hrBPM,
                    availability = hrAvailability,
                    stepsDaily = stepsDaily,
                    modifier = Modifier.fillParentMaxSize()
                )
            }
            item {
                StartWorkoutSection(
                    skipPrompt = skipPrompt,
                    setHrEnabled = setHrEnabled,
                    navigateToExerciseSelection = navigateToExerciseSelection,
                    navigateToSummary = navigateToSummary,
                    modifier = Modifier.fillParentMaxSize()
                )
            }
            item {
                SettingsSection(
                    navigateToSettings = navigateToSettings,
                    modifier = Modifier.fillParentMaxSize()
                )
            }
        }
    }
}

@Composable
fun CharacterProfileSection(
    modifier: Modifier = Modifier,
    gender: String?,
    hrBPM: Double,
    availability: DataTypeAvailability,
    stepsDaily: Long?
) {
    Section(modifier = modifier) {
        CenteredColumn(
            modifier = Modifier.weight(2f),
        ) {
            HeartRateLabel(
                hrBPM = hrBPM, availability = availability
            )
            Text(text = stepsDaily?.toString() ?: "N/A")
        }
        CenteredColumn(
            modifier = Modifier.weight(1f),
        ) {
            if (gender != null) {
                val painterResourceId = if (gender == "Male") R.drawable.male else R.drawable.female

                Image(
                    painter = painterResource(painterResourceId),
                    contentDescription = "Profile Image",
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}

@Composable
fun StartWorkoutSection(
    modifier: Modifier = Modifier,
    skipPrompt: Boolean?,
    setHrEnabled: (Boolean) -> Unit,
    navigateToExerciseSelection: () -> Unit = {},
    navigateToSummary: () -> Unit = {},
) {
    Section(modifier = modifier) {
        CenteredColumn {
            Text(
                textAlign = TextAlign.Center,
                text = "Start Workout",
            )

            Button(
                onClick = {
                    setHrEnabled(false)
                    if (skipPrompt == true) {
                        navigateToSummary()
                    } else {
                        navigateToExerciseSelection()
                    }
                }, modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(id = R.string.start)
                )
            }

        }
    }
}

@Composable
fun SettingsSection(modifier: Modifier = Modifier, navigateToSettings: () -> Unit = {}) {
    Section(modifier = modifier) {
        CenteredColumn {
            Button(
                onClick = {
                    navigateToSettings()
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.settings))
            }
        }
    }
}


//@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    StartingUp(hasCapabilities = false, isTrackingAnotherExercise = false)
//}