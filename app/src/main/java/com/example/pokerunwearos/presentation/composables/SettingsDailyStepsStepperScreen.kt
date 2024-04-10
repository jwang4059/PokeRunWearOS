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
import com.example.pokerunwearos.presentation.ui.widgets.CenteredColumn
import com.example.pokerunwearos.presentation.ui.widgets.Section

@Composable
fun SettingsDailyStepsStepperScreen(
    dailyStepsGoal: Int = 0,
    setDailyStepsGoal: (Int) -> Unit,
) {
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
                Stepper(
                    value = dailyStepsGoal,
                    onValueChange = { setDailyStepsGoal(it) },
                    increaseIcon = { Icon(StepperDefaults.Increase, "Increase") },
                    decreaseIcon = { Icon(StepperDefaults.Decrease, "Decrease") },
                    valueProgression = 0..100_000 step 1_000
                ) {
                    Text(
                        text = "$dailyStepsGoal steps"
                    )
                }
            }
        }
    }
}