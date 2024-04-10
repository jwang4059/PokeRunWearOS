package com.example.pokerunwearos.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Stepper
import androidx.wear.compose.material.StepperDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.example.pokerunwearos.presentation.ui.utils.MeasurementMap
import com.example.pokerunwearos.presentation.ui.utils.MeasurementUnit
import com.example.pokerunwearos.presentation.ui.utils.formatDistance
import com.example.pokerunwearos.presentation.ui.widgets.CenteredColumn
import com.example.pokerunwearos.presentation.ui.widgets.Section

@Composable
fun SettingsExerciseGoalStepperScreen(
    measurementUnit: MeasurementUnit = MeasurementUnit.IMPERIAL,
    exerciseGoal: Double = 0.0,
    setExerciseGoal: (Double) -> Unit,
) {
    val measurementConversionUnit = MeasurementMap[measurementUnit]!!

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
            CenteredColumn {
                val value = (exerciseGoal / measurementConversionUnit.distanceConversion).toInt()

                Stepper(
                    value = value,
                    onValueChange = { setExerciseGoal(it * measurementConversionUnit.distanceConversion) },
                    increaseIcon = { Icon(StepperDefaults.Increase, "Increase") },
                    decreaseIcon = { Icon(StepperDefaults.Decrease, "Decrease") },
                    valueProgression = 0..100
                ) {
                    Text(
                        text = formatDistance(
                            meters = exerciseGoal,
                            measurementUnit = measurementUnit,
                            hasUnit = true,
                            isInt = true
                        ).toString()
                    )
                }
            }
        }
    }
}