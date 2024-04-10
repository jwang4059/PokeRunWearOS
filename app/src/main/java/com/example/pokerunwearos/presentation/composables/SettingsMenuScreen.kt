package com.example.pokerunwearos.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.ExerciseTypeCapabilities
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.scrollAway
import com.example.pokerunwearos.presentation.ui.utils.MeasurementUnit
import com.example.pokerunwearos.presentation.ui.utils.formatDistance

@Composable
fun SettingsMenuScreen(
    capabilities: ExerciseTypeCapabilities? = null,
    currentExerciseType: String? = null,
    currentExerciseGoal: Double? = null,
    dailyStepsGoal: Int? = null,
    gender: String? = null,
    language: String? = null,
    skipPrompt: Boolean = false,
    autoPause: Boolean = false,
    useMetric: Boolean = false,
    measurementUnit: MeasurementUnit = MeasurementUnit.IMPERIAL,
    setSkipPrompt: (Boolean) -> Unit = {},
    setAutoPause: (Boolean) -> Unit = {},
    setUseMetric: (Boolean) -> Unit = {},
    isExerciseInProgress: suspend () -> Boolean = { false },
    navigateToSettingsExerciseType: () -> Unit = {},
    navigateToSettingsExerciseGoal: () -> Unit = {},
    navigateToSettingsDailyStepsGoal: () -> Unit = {},
    navigateToSettingsGender: () -> Unit = {},
    navigateToSettingsLanguage: () -> Unit = {},
) {
    var active by remember { mutableStateOf(false) }
    val listState = rememberScalingLazyListState()

    LaunchedEffect(Unit) {
        active = isExerciseInProgress()
    }

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
            if (!active || (capabilities != null && capabilities.supportsAutoPauseAndResume)) {
                item {
                    ListHeader {
                        Text(text = "Workout")
                    }
                }
            }

            if (!active) {
                item {
                    SettingsMenuToggleChip(
                        checked = skipPrompt,
                        setChecked = setSkipPrompt,
                        label = {
                            Text("Skip prompt", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        })
                }

                item {
                    SettingsMenuChip(onClick = { navigateToSettingsExerciseType() }, label = {
                        Text(
                            text = "Exercise Type", maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }, secondaryLabel = {
                        if (currentExerciseType != null) {
                            Text(text = currentExerciseType)
                        }
                    })
                }

                item {
                    SettingsMenuChip(onClick = { navigateToSettingsExerciseGoal() }, label = {
                        Text(
                            text = "Exercise Goal", maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }, secondaryLabel = {
                        if (currentExerciseGoal != null) {
                            Text(
                                text = formatDistance(
                                    meters = currentExerciseGoal.toDouble(),
                                    measurementUnit = measurementUnit,
                                    hasUnit = true,
                                    isInt = true
                                ).toString()
                            )
                        }
                    })
                }

                item {
                    SettingsMenuChip(onClick = { navigateToSettingsDailyStepsGoal() }, label = {
                        Text(
                            text = "Daily Steps Goal",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }, secondaryLabel = {
                        if (dailyStepsGoal != null) {
                            Text(
                                text = "$dailyStepsGoal steps"
                            )
                        }
                    })
                }
            }

            if (capabilities != null && capabilities.supportsAutoPauseAndResume) {
                item {
                    SettingsMenuToggleChip(checked = autoPause, setChecked = setAutoPause, label = {
                        Text("Auto Pause", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    })
                }
            }

            item {
                ListHeader {
                    Text(text = "Profile")
                }
            }

            item {
                SettingsMenuChip(onClick = { navigateToSettingsGender() }, label = {
                    Text(
                        text = "Gender", maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                }, secondaryLabel = {
                    if (gender != null) {
                        Text(text = gender)
                    }
                })
            }

            item {
                SettingsMenuChip(onClick = { navigateToSettingsLanguage() }, label = {
                    Text(
                        text = "Language", maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                }, secondaryLabel = {
                    if (language != null) {
                        Text(text = language)
                    }
                })
            }

            item {
                SettingsMenuToggleChip(checked = useMetric, setChecked = setUseMetric, label = {
                    Text("Use Metrics", maxLines = 1, overflow = TextOverflow.Ellipsis)
                })
            }
        }
    }
}

@Composable
fun SettingsMenuChip(
    modifier: Modifier = Modifier,
    label: @Composable (RowScope.() -> Unit),
    onClick: () -> Unit = {},
    secondaryLabel: @Composable (RowScope.() -> Unit)? = null,
    icon: @Composable (BoxScope.() -> Unit)? = null,
    enabled: Boolean = true
) {
    Chip(
        onClick = onClick,
        enabled = enabled,
        label = label,
        secondaryLabel = secondaryLabel,
        icon = icon,
        colors = ChipDefaults.chipColors(backgroundColor = Color.DarkGray),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun SettingsMenuToggleChip(
    modifier: Modifier = Modifier,
    label: @Composable (RowScope.() -> Unit),
    checked: Boolean = false,
    setChecked: (Boolean) -> Unit = {},
    secondaryLabel: @Composable (RowScope.() -> Unit)? = null,
    appIcon: @Composable (BoxScope.() -> Unit)? = null,
    enabled: Boolean = true
) {
    ToggleChip(
        label = label,
        secondaryLabel = secondaryLabel,
        checked = checked,
        colors = ToggleChipDefaults.toggleChipColors(
            uncheckedToggleControlColor = ToggleChipDefaults.SwitchUncheckedIconColor
        ),
        toggleControl = {
            Switch(checked = checked, enabled = true, modifier = Modifier.semantics {
                this.contentDescription = if (checked) "On" else "Off"
            })
        },
        onCheckedChange = { setChecked(it) },
        appIcon = appIcon,
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
    )
}