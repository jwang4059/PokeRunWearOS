package com.example.pokerunwearos.ui

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.health.services.client.data.LocationAvailability
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
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
import com.example.pokerunwearos.data.ServiceState
import com.example.pokerunwearos.ui.component.ExerciseInProgressAlert
import kotlinx.coroutines.launch

@Composable
fun PreparingExercise(
    onUnavailable: () -> Unit = {},
    onStart: () -> Unit = {},
    prepareExercise: () -> Unit,
    serviceState: ServiceState,
    permissions: Array<String>,
    hasCapabilities: Boolean,
    isTrackingAnotherExercise: Boolean,
) {
    if (!hasCapabilities) onUnavailable()
    if (isTrackingAnotherExercise) ExerciseInProgressAlert(isTrackingExercise = true)

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
            val gpsAcquired =
                location == LocationAvailability.ACQUIRED_TETHERED || location == LocationAvailability.ACQUIRED_UNTETHERED

            LaunchedEffect(Unit) {
                launch {
                    permissionLauncher.launch(permissions)
                    prepareExercise()
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
                            Text(
                                text = updatePrepareLocationStatus(locationAvailability = location),
                            )
                            Button(
                                onClick = { onStart() },
                                enabled = gpsAcquired,
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