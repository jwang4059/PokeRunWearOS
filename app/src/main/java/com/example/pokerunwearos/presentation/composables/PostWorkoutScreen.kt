package com.example.pokerunwearos.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import androidx.wear.compose.material.rememberScalingLazyListState
import androidx.wear.compose.material.scrollAway
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pokerunwearos.R
import com.example.pokerunwearos.data.models.Pokemon
import com.example.pokerunwearos.data.models.Workout
import com.example.pokerunwearos.presentation.ui.utils.ElapsedTime
import com.example.pokerunwearos.presentation.ui.utils.MeasurementUnit
import com.example.pokerunwearos.presentation.ui.utils.formatCalories
import com.example.pokerunwearos.presentation.ui.utils.formatDistance
import com.example.pokerunwearos.presentation.ui.utils.formatElapsedTime
import com.example.pokerunwearos.presentation.ui.widgets.SummaryFormat
import com.example.pokerunwearos.presentation.viewmodels.PokemonUiState
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@Composable
fun PostWorkoutScreen(
    workout: Workout?, fetchPokemon: suspend () -> Pokemon, onRestartClick: () -> Unit = {}
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

    val listState = rememberScalingLazyListState()
    val coroutineScope = rememberCoroutineScope()

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
        val focusRequester = remember { FocusRequester() }

        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .onRotaryScrollEvent {
                    coroutineScope.launch {
                        listState.scrollBy(it.verticalScrollPixels)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable(),
            autoCentering = AutoCenteringParams(itemIndex = 0),
            state = listState,
        ) {
            item { ListHeader { Text(stringResource(id = R.string.workout_complete)) } }
            item {
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
            item {
                if (workout != null) {
                    SummaryFormat(
                        value = formatElapsedTime(
                            time = ElapsedTime.ElapsedTimeLong(workout.timeMillis),
                            includeSeconds = true,
                            includeHundredth = true
                        ).toString(),
                        metric = stringResource(id = R.string.duration),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                if (workout != null) {
                    SummaryFormat(
                        value = workout.avgHeartRate.toInt().toString(),
                        metric = stringResource(id = R.string.avgHR),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                if (workout != null) {
                    SummaryFormat(
                        value = formatDistance(
                            workout.distance, MeasurementUnit.IMPERIAL
                        ).toString(),
                        metric = stringResource(id = R.string.distance),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                if (workout != null) {
                    SummaryFormat(
                        value = formatCalories(workout.calories).toString(),
                        metric = stringResource(id = R.string.calories),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    Button(
                        onClick = {
                            onRestartClick()
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = R.string.restart))
                    }
                }
            }
        }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
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