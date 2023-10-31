package com.example.pokerunwearos.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseGoalType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.example.pokerunwearos.presentation.composables.CountdownScreen
import com.example.pokerunwearos.presentation.composables.ExerciseSelectionScreen
import com.example.pokerunwearos.presentation.composables.MissionSelectionScreen
import com.example.pokerunwearos.presentation.composables.PostWorkoutScreen
import com.example.pokerunwearos.presentation.composables.PreWorkoutScreen
import com.example.pokerunwearos.presentation.composables.StartScreen
import com.example.pokerunwearos.presentation.composables.TrackWorkoutScreen
import com.example.pokerunwearos.presentation.ui.utils.toExerciseType
import com.example.pokerunwearos.presentation.viewmodels.PokeRunViewModel
import com.example.pokerunwearos.presentation.viewmodels.PostWorkoutViewModel

@Composable
fun PokeRunNavHost(
    viewModel: PokeRunViewModel,
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
) {
    SwipeDismissableNavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        composable(PokeRunDestinations.StartScreen.route) {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val hrUiState by viewModel.hrUiState.collectAsStateWithLifecycle()

            StartScreen(permissions = viewModel.permissions,
                stepsDaily = uiState.stepsDaily,
                hrBPM = hrUiState.hrBPM,
                hrAvailability = hrUiState.hrAvailability,
                setHrEnabled = viewModel::setHrEnabled,
                navigateToExerciseSelection = { navController.navigate(PokeRunDestinations.ExerciseSelectionScreen.route) })
        }

        composable(PokeRunDestinations.ExerciseSelectionScreen.route) {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            ExerciseSelectionScreen(hasCapabilities = viewModel::hasExerciseCapabilities,
                setExercise = viewModel::setExercise,
                navigateToUnavailable = {
                    navController.navigate(PokeRunDestinations.ExerciseNotAvailable.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = false
                        }
                    }
                },
                navigateToNextScreen = {
                    if (viewModel.supportsGoalType(
                            uiState.exerciseCapabilities?.get(
                                uiState.currentExerciseType?.toExerciseType()
                            ), ExerciseGoalType.ONE_TIME_GOAL, DataType.DISTANCE_TOTAL
                        )
                    ) {
                        navController.navigate(PokeRunDestinations.MissionSelectionScreen.route)
                    } else {
                        navController.navigate(
                            PokeRunDestinations.PreWorkoutScreen.route
                        )
                    }

                },
                navigateBack = { navController.popBackStack() })
        }

        composable(PokeRunDestinations.MissionSelectionScreen.route) {
            MissionSelectionScreen(
                setExerciseGoal = viewModel::setExerciseGoal,
                navigateToSummary = {
                    navController.navigate(PokeRunDestinations.PreWorkoutScreen.route)
                },
                navigateBack = { navController.popBackStack() },
            )
        }

//        composable(PokeRunDestinations.MissionScreen.route) {
//            val appUiState by viewModel.appUiState.collectAsStateWithLifecycle()
//
//            MissionScreen(onBack = { navController.popBackStack() },
//                currentExerciseGoal = appUiState.currentExerciseGoal,
//                setExerciseGoal = viewModel::setExerciseGoal,
//                navigateToPrepareScreen = {
//                    navController.navigate(PokeRunDestinations.PreWorkoutScreen.route) {
//                        popUpTo(navController.graph.id) {
//                            inclusive = true
//                        }
//                    }
//                })
//        }

        composable(PokeRunDestinations.PreWorkoutScreen.route) {
            val serviceState by viewModel.exerciseServiceState
            val permissions = viewModel.permissions
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            PreWorkoutScreen(
                serviceState = serviceState,
                permissions = permissions,
                trackedStatus = uiState.trackedStatus,
                exerciseType = uiState.currentExerciseType,
                exerciseGoal = uiState.currentExerciseGoal,
                prepareExercise = viewModel::prepareExercise,
                navigateToCountdown = {
                    navController.navigate(PokeRunDestinations.CountdownScreen.route)
                },
            )
        }

        composable(PokeRunDestinations.CountdownScreen.route) {
            CountdownScreen(startExercise = viewModel::startExercise, navigateToTrackWorkout = {
                navController.navigate(PokeRunDestinations.TrackWorkoutScreen.route) {
                    popUpTo(navController.graph.id) {
                        inclusive = false
                    }
                }
            })
        }

        composable(PokeRunDestinations.TrackWorkoutScreen.route) {
            val serviceState by viewModel.exerciseServiceState

            TrackWorkoutScreen(
                serviceState = serviceState,
                onResumeClick = viewModel::resumeExercise,
                onPauseClick = viewModel::pauseExercise,
                onEndClick = viewModel::endExercise,
                saveWorkout = viewModel::saveWorkout,
                navigateToExerciseSelection = { navController.navigate(PokeRunDestinations.ExerciseSelectionScreen.route) },
                navigateToPostWorkout = {
                    navController.navigate(PokeRunDestinations.PostWorkoutScreen.route) {
                        popUpTo(PokeRunDestinations.TrackWorkoutScreen.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable(PokeRunDestinations.PostWorkoutScreen.route) {
            val postWorkoutViewModel = hiltViewModel<PostWorkoutViewModel>()
            val uiState by postWorkoutViewModel.uiState.collectAsStateWithLifecycle()

            PostWorkoutScreen(workout = uiState,
                fetchPokemon = postWorkoutViewModel::fetchData,
                onRestartClick = {
                    navController.navigate(PokeRunDestinations.StartScreen.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                })
        }
    }
}