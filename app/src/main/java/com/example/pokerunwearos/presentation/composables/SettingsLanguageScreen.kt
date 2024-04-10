package com.example.pokerunwearos.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.example.pokerunwearos.presentation.ui.widgets.CenteredRow

@Composable
fun SettingsLanguageScreen(
    setLanguage: (String) -> Unit,
    navigateBack: () -> Unit = {},
) {
    val listState = rememberScalingLazyListState()
    val languageList = arrayOf("English", "Japanese")

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
                    Text(text = "Select language")
                }
            }
            itemsIndexed(languageList) { _, language ->
                CenteredRow {
                    Button(onClick = {
                        setLanguage(language)
                        navigateBack()
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = language)
                    }
                }
            }
        }
    }
}