package com.example.pokerunwearos.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.ExerciseType
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.itemsIndexed
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.scrollAway
import com.example.pokerunwearos.presentation.ui.utils.exerciseTypes
import com.example.pokerunwearos.presentation.ui.utils.toFormattedString
import com.example.pokerunwearos.presentation.ui.widgets.CenteredRow

@Composable
fun SettingsExerciseTypeScreen(
    hasCapabilities: (Array<ExerciseType>, Boolean) -> Boolean,
    setExerciseType: (String) -> Unit,
    navigateBack: () -> Unit = {},
) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            autoCentering = AutoCenteringParams(itemIndex = 0),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
        ) {
            item {
                ListHeader {
                    Text(text = "Select exercise")
                }
            }
            itemsIndexed(exerciseTypes) { _, exerciseType ->
                CenteredRow {
                    Button(
                        onClick = {
                            setExerciseType(exerciseType.toFormattedString())
                            navigateBack()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = hasCapabilities(arrayOf(exerciseType), false)
                    ) {
                        Text(text = exerciseType.toFormattedString())
                    }
                }
            }
        }
    }
}