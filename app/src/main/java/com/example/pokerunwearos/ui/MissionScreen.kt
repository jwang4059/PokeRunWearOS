package com.example.pokerunwearos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.ExerciseGoal
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.scrollAway

@Composable
fun MissionScreen(
    currentExerciseGoal: ExerciseGoal<Double>?,
    setExerciseGoal: (Double) -> Unit,
    navigateToPrepareScreen: () -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    val currentThreshold = currentExerciseGoal?.dataTypeCondition?.threshold

    val colorStops = arrayOf(
        0.0f to Color(0xFF2196F3), 1f to Color(0xFFE3F2FD)
    )

    Scaffold(
        timeText = {
            TimeText(
                timeSource = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()),
                modifier = Modifier.scrollAway(listState)
            )
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        },
        modifier = Modifier.background(
            Brush.verticalGradient(colorStops = colorStops)
        ),
    ) {
        Column {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.weight(2f)
            ) {
                item {
                    Button(
                        onClick = { setExerciseGoal(5_000.0); navigateToPrepareScreen() },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (currentThreshold == null || currentThreshold == 5_000.0) Color(
                                0xFFE57373
                            ) else Color.Gray
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Text(text = "5000km")
                    }
                }


                item {
                    Button(
                        onClick = { setExerciseGoal(10_000.0); navigateToPrepareScreen() },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (currentThreshold == null || currentThreshold == 10_000.0) Color(
                                0xFF4CAF50
                            ) else Color.Gray
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Text(text = "10000km")
                    }
                }


                item {
                    Button(
                        onClick = { setExerciseGoal(15_000.0); navigateToPrepareScreen() },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (currentThreshold == null || currentThreshold == 15_000.0) Color(
                                0xFFFFEB3B
                            ) else Color.Gray
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Text(text = "15000km")
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = { onBack() }, modifier = Modifier.padding(16.dp, 4.dp)
                ) {
                    Text(text = "Back")
                }
            }
        }
    }
}