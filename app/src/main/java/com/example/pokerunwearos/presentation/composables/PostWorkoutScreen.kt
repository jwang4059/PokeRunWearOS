package com.example.pokerunwearos.presentation.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pokerunwearos.R
import com.example.pokerunwearos.data.models.Pokemon
import com.example.pokerunwearos.data.models.Workout
import com.example.pokerunwearos.presentation.ui.utils.ElapsedTime
import com.example.pokerunwearos.presentation.ui.utils.MeasurementMap
import com.example.pokerunwearos.presentation.ui.utils.MeasurementUnit
import com.example.pokerunwearos.presentation.ui.utils.formatCalories
import com.example.pokerunwearos.presentation.ui.utils.formatDistance
import com.example.pokerunwearos.presentation.ui.utils.formatElapsedTime
import com.example.pokerunwearos.presentation.ui.utils.formatPace
import com.example.pokerunwearos.presentation.ui.utils.formatSpeed
import com.example.pokerunwearos.presentation.ui.utils.toFormattedString
import com.example.pokerunwearos.presentation.ui.widgets.CenteredColumn
import com.example.pokerunwearos.presentation.ui.widgets.CenteredRow
import com.example.pokerunwearos.presentation.ui.widgets.Section
import com.example.pokerunwearos.presentation.ui.widgets.VerticalDivider
import com.example.pokerunwearos.presentation.viewmodels.PokemonUiState
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostWorkoutScreen(
    modifier: Modifier = Modifier,
    workout: Workout?,
    measurementUnit: MeasurementUnit = MeasurementUnit.IMPERIAL,
    fetchPokemon: suspend () -> Pokemon,
    onRestartClick: () -> Unit = {}
) {
    var pokemonUiState: PokemonUiState by remember { mutableStateOf(PokemonUiState.Loading) }

    LaunchedEffect(Unit) {
        pokemonUiState = PokemonUiState.Loading
        pokemonUiState = try {
            PokemonUiState.Success(fetchPokemon())
        } catch (e: IOException) {
            PokemonUiState.Error
        } catch (e: HttpException) {
            PokemonUiState.Error
        }
    }

    val measurementConversionUnit = MeasurementMap[measurementUnit]!!

    val coroutineScope = rememberCoroutineScope()
    val state = rememberLazyListState()
    val snappingLayout = remember(state) { SnapLayoutInfoProvider(state) }
    val flingBehavior = rememberSnapFlingBehavior(snappingLayout)

    Scaffold(timeText = {
        TimeText(
            timeSource = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()),
            modifier = Modifier.scrollAway(state)
        )
    }, vignette = {
        Vignette(vignettePosition = VignettePosition.TopAndBottom)
    }, positionIndicator = {
        PositionIndicator(lazyListState = state)
    }, modifier = modifier) {
        val focusRequester = remember { FocusRequester() }

        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .onRotaryScrollEvent {
                coroutineScope.launch {
                    state.scrollBy(it.verticalScrollPixels)
                }
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
            state = state,
            flingBehavior = flingBehavior) {
            item {
                Section(modifier = Modifier.fillParentMaxSize()) {
                    CenteredColumn {
                        CenteredRow {
                            if (workout != null) {
                                workout.exerciseType?.toFormattedString()?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colors.secondary
                                    )
                                }
                            }
                        }
                        CenteredRow {
                            when (pokemonUiState) {
                                is PokemonUiState.Success -> AsyncImage(
                                    model = ImageRequest.Builder(context = LocalContext.current)
                                        .data((pokemonUiState as PokemonUiState.Success).pokemon.sprites.frontDefault)
                                        .crossfade(true).build(),
                                    contentDescription = "pokemon",
                                    contentScale = ContentScale.FillBounds
                                )

                                else -> {}
                            }
                        }
                        if (workout != null) {
                            val durationStr = formatElapsedTime(
                                time = ElapsedTime.ElapsedTimeLong(workout.timeMillis),
                                includeSeconds = true,
                                includeHundredth = true
                            ).toString()

                            val distanceStr = formatDistance(
                                meters = workout.distance,
                                measurementUnit = measurementUnit,
                                hasUnit = false
                            ).toString()

                            val stepsStr = workout.steps.toString()

                            val caloriesStr = formatCalories(
                                calories = workout.calories, hasUnit = false
                            ).toString()

                            CenteredRow {
                                CenteredColumn {
                                    CenteredRow(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = durationStr,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    CenteredRow(
                                        modifier = Modifier
                                            .height(IntrinsicSize.Min)
                                            .fillMaxWidth()
                                            .padding(12.dp, 4.dp)
                                    ) {
                                        StatText(
                                            value = distanceStr,
                                            unit = measurementConversionUnit.distanceUnit,
                                            modifier = Modifier.weight(1f)
                                        )
                                        VerticalDivider(height = 0.4f, color = Color.LightGray)
                                        StatText(
                                            value = stepsStr,
                                            unit = measurementConversionUnit.stepsUnit,
                                            modifier = Modifier.weight(1f)
                                        )
                                        VerticalDivider(height = 0.4f, color = Color.LightGray)
                                        StatText(
                                            value = caloriesStr,
                                            unit = measurementConversionUnit.energyUnit,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Section(modifier = Modifier.fillParentMaxSize()) {
                    CenteredColumn {
                        Row {
                            Text(text = "Averages")
                        }
                        if (workout != null) {
                            val avgSpeedStr = formatSpeed(
                                metersPerSec = workout.avgSpeed,
                                measurementUnit = measurementUnit,
                                hasUnit = false
                            ).toString()
                            val avgPaceStr = formatPace(
                                msPerKm = workout.avgPace,
                                measurementUnit = measurementUnit,
                                available = false
                            ).toString()
                            val avgHeartRateStr = workout.avgHeartRate.toInt().toString()

                            CenteredRow {
                                CenteredColumn {
                                    AvgStatText(
                                        label = stringResource(id = R.string.speed),
                                        value = avgSpeedStr,
                                        unit = " ${measurementConversionUnit.speedUnit}"
                                    )
                                    AvgStatText(
                                        label = stringResource(id = R.string.pace),
                                        value = avgPaceStr,
                                        unit = " ${measurementConversionUnit.paceUnit}"
                                    )
                                    AvgStatText(
                                        label = stringResource(id = R.string.heart_rate),
                                        value = avgHeartRateStr,
                                        unit = " ${measurementConversionUnit.hrUnit}"
                                    )
                                }
                            }
                        }
                    }
                }
            }


            item {
                Section(modifier = Modifier.fillParentMaxSize()) {
                    Button(
                        onClick = {
                            onRestartClick()
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.backToMainMenu))
                    }
                }
            }
        }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}

@Composable
fun StatText(modifier: Modifier = Modifier, value: String, unit: String) {
    CenteredColumn(modifier = modifier) {
        Text(text = value, fontWeight = FontWeight.Bold)
        Text(text = unit, color = Color.Gray)
    }
}

@Composable
fun AvgStatText(modifier: Modifier = Modifier, label: String, value: String, unit: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
        modifier = modifier.fillMaxWidth(0.9F)
    ) {
        Column {
            Text(text = label)
        }
        Column {
            Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
                Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = unit)
            }
        }
    }
}


//@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
//@Composable
//fun SummaryScreenPreview() {
//    PostWorkoutScreen(averageHeartRate = "75.0",
//        totalDistance = "2 km",
//        totalCalories = "100",
//        elapsedTime = "17m01",
//        onRestartClick = {})
//}