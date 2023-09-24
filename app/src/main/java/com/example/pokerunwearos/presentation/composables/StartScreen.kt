package com.example.pokerunwearos.presentation.composables

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.DataTypeAvailability
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyColumnDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import com.example.pokerunwearos.R
import com.example.pokerunwearos.presentation.ui.widgets.HeartRateLabel
import kotlinx.coroutines.launch

@Composable
fun StartScreen(
    permissions: Array<String>,
    hrBPM: Double,
    availability: DataTypeAvailability,
    navigateToExerciseSelection: () -> Unit = {},
) {
    val listState = rememberScalingLazyListState()

    Scaffold(timeText = {
        TimeText(
            timeSource = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()),
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
            verticalArrangement = Arrangement.spacedBy(64.dp),
            autoCentering = AutoCenteringParams(itemIndex = 0),
            flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(
                state = listState, snapOffset = 0.dp
            )
        ) {
            item {
                CharacterProfileCard(permissions, hrBPM, availability)
            }
            item {
                StartWorkoutCard(navigateToExerciseSelection)
            }

        }
    }
}

@Composable
fun CharacterProfileCard(
    permissions: Array<String>, hrBPM: Double, availability: DataTypeAvailability
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


    Row(
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeartRateLabel(
                hrBPM = hrBPM, availability = availability
            )
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
fun StartWorkoutCard(
    navigateToExerciseSelection: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
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