package com.example.pokerunwearos.presentation.composables

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.health.services.client.data.LocationAvailability
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.example.pokerunwearos.R
import com.example.pokerunwearos.data.repository.health.ServiceState
import com.example.pokerunwearos.presentation.ui.utils.RUNNING
import com.example.pokerunwearos.presentation.ui.utils.TREADMILL
import com.example.pokerunwearos.presentation.ui.widgets.Section
import com.example.pokerunwearos.presentation.ui.widgets.SummaryFormat
import kotlinx.coroutines.launch

@Composable
fun PreWorkoutScreen(
    prepareExercise: () -> Unit,
    serviceState: ServiceState,
    permissions: Array<String>,
    exerciseType: String?,
    exerciseGoal: Double?,
    navigateToCountdown: () -> Unit = {},
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

    when (serviceState) {
        is ServiceState.Connected -> {
            val location by serviceState.locationAvailabilityState.collectAsStateWithLifecycle()
//            val gpsAcquired =
//                location == LocationAvailability.ACQUIRED_TETHERED || location == LocationAvailability.ACQUIRED_UNTETHERED

            LaunchedEffect(Unit) {
                launch {
                    permissionLauncher.launch(permissions)
                    prepareExercise()
                }
            }

            Scaffold(timeText = {
                TimeText(
                    timeSource = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()),
                )
            }, vignette = {
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            }) {
                Section(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                ) {
                    Column (
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row {
                            Text(
                                text = "Workout Summary",
                            )
                        }
                        Row {
                            SummaryFormat(
                                value = exerciseType ?: RUNNING,
                                metric = "Exercise Type",
                            )
                        }
                        if (exerciseGoal != null && exerciseGoal != 0.0) {
                            Row {
                                SummaryFormat(
                                    value = "%s meters".format(exerciseGoal),
                                    metric = "Exercise Goal",
                                )
                            }
                        }
                        Row {
                            Button(
                                onClick = { navigateToCountdown() },
                                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primaryVariant),
                                modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = stringResource(id = R.string.start),
                                )
                            }
                        }
                        if (exerciseType != TREADMILL) {
                            Row {
                                Text(
                                    text = updatePrepareLocationStatus(locationAvailability = location),
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

@Composable
private fun updatePrepareLocationStatus(locationAvailability: LocationAvailability): String {
    val gpsText = when (locationAvailability) {
        LocationAvailability.ACQUIRED_TETHERED, LocationAvailability.ACQUIRED_UNTETHERED -> R.string.GPS_acquired
        LocationAvailability.NO_GNSS -> R.string.GPS_disabled // TODO Consider redirecting user to change device settings in this case
        LocationAvailability.ACQUIRING -> R.string.GPS_acquiring
        LocationAvailability.UNKNOWN -> R.string.GPS_initializing
        else -> R.string.GPS_unavailable
    }

    return stringResource(id = gpsText)
}