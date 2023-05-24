package com.example.pokerunwearos.ui

import android.content.ContentValues
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.scrollAway
import com.example.pokerunwearos.R
import com.example.pokerunwearos.data.ServiceState
import kotlinx.coroutines.launch

@Composable
fun StartingUp(
    serviceState: ServiceState,
    permissions: Array<String>,
    hasCapabilities: Boolean,
    isTrackingAnotherExercise: Boolean,
) {
    if (hasCapabilities) {
        Log.d("STARTING", "hasCapabilities = true")
    } else {
        Log.d("STARTING", "hasCapabilities = false")
    }

    if (isTrackingAnotherExercise) {
        Log.d("STARTING", "isTrackingAnotherExercise = true")
    } else {
        Log.d("STARTING", "isTrackingAnotherExercise = false")
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.d("Permissions", "Permissions section")
        if (result.all { it.value }) {
            Log.d("Permissions", "All required permissions granted")
        } else {
            Log.d("Permissions", "Missing permissions")
        }
    }

    if (serviceState is ServiceState.Connected) {
        LaunchedEffect(Unit) {
            launch {
                permissionLauncher.launch(permissions)
            }
        }
    }

    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = {
            TimeText(modifier = Modifier.scrollAway(listState))
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = listState
            )
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            autoCentering = AutoCenteringParams(itemIndex = 0),
        ) {
            item { Greeting(greetingName = "Trainer") }
            item { Text(text = "hasCapabilities = $hasCapabilities") }
            item { Text(text = "isTrackingAnotherExercise = $isTrackingAnotherExercise") }
            item { Text(text = "serviceState = $serviceState") }
        }
    }
}

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

//@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    StartingUp(hasCapabilities = false, isTrackingAnotherExercise = false)
//}