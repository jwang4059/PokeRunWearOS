package com.example.pokerunwearos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseGoalType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.example.pokerunwearos.Screens


@Composable
fun PokeRunWearApp(
    viewModel: PokeRunViewModel, navController: NavHostController, startDestination: String
) {


    SwipeDismissableNavHost(
        navController = navController, startDestination = startDestination
    ) {
        composable(Screens.StartWorkoutScreen.route) {
            val exerciseUiState by viewModel.exerciseUiState.collectAsStateWithLifecycle()
            StartWorkoutScreen(hasCapabilities = {
                viewModel.hasExerciseCapabilities(
                    exerciseUiState.exerciseCapabilities
                )
            }, onUnavailable = {
                navController.navigate(Screens.ExerciseNotAvailable.route) {
                    popUpTo(navController.graph.id) {
                        inclusive = false
                    }
                }
            }, navigateToExerciseSelection = {
                navController.navigate(Screens.ExerciseSelectionScreen.route)
            })
        }

        composable(Screens.ExerciseSelectionScreen.route) {
            val exerciseUiState by viewModel.exerciseUiState.collectAsStateWithLifecycle()
            val appUiState by viewModel.appUiState.collectAsStateWithLifecycle()

            ExerciseSelectionScreen(
                isTrackingAnotherExercise = exerciseUiState.isTrackingAnotherExercise,
                setExercise = { exerciseType ->
                    viewModel.setExercise(exerciseType)
                },
                navigateToNextScreen = {
                    if (viewModel.supportsGoalType(
                            exerciseUiState.exerciseCapabilities?.get(
                                appUiState.currentExerciseType
                            ), ExerciseGoalType.ONE_TIME_GOAL, DataType.DISTANCE_TOTAL
                        )
                    ) {
                        navController.navigate(Screens.MissionSelectionScreen.route)
                    } else {
                        navController.navigate(
                            Screens.PreWorkoutScreen.route
                        )
                    }

                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screens.MissionSelectionScreen.route) {
            MissionSelectionScreen(
                setExerciseGoal = { distance ->
                    viewModel.setExerciseGoal(distance)
                },
                navigateToSummary = {
                    navController.navigate(Screens.PreWorkoutScreen.route)
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screens.MissionScreen.route) {
            val appUiState by viewModel.appUiState.collectAsStateWithLifecycle()

            MissionScreen(onBack = { navController.popBackStack() },
                currentExerciseGoal = appUiState.currentExerciseGoal,
                setExerciseGoal = { distance ->
                    viewModel.setExerciseGoal(distance)
                },
                navigateToPrepareScreen = {
                    navController.navigate(Screens.PreWorkoutScreen.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                })
        }

        composable(Screens.PreWorkoutScreen.route) {
            val serviceState by viewModel.exerciseServiceState
            val permissions = viewModel.permissions
            val appUiState by viewModel.appUiState.collectAsStateWithLifecycle()

            PreWorkoutScreen(
                onStart = {
                    navController.navigate(Screens.TrackWorkoutScreen.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = false
                        }
                    }
                    viewModel.startExercise()
                },
                prepareExercise = { viewModel.prepareExercise() },
                serviceState = serviceState,
                permissions = permissions,
                exerciseType = appUiState.currentExerciseType,
                exerciseGoal = appUiState.currentExerciseGoal,
            )
        }

        composable(Screens.TrackWorkoutScreen.route) {
            val serviceState by viewModel.exerciseServiceState
            TrackWorkoutScreen(
                onPauseClick = { viewModel.pauseExercise() },
                onEndClick = { viewModel.endExercise() },
                onResumeClick = { viewModel.resumeExercise() },
                onStartClick = { viewModel.startExercise() },
                serviceState = serviceState,
                navController = navController,
            )
        }

        composable(
            Screens.PostWorkoutScreen.route + "/{averageHeartRate}/{totalDistance}/{totalCalories}/{elapsedTime}",
            arguments = listOf(navArgument("averageHeartRate") { type = NavType.StringType },
                navArgument("totalDistance") { type = NavType.StringType },
                navArgument("totalCalories") { type = NavType.StringType },
                navArgument("elapsedTime") { type = NavType.StringType })
        ) {
            PostWorkoutScreen(
                viewModel = viewModel,
                averageHeartRate = it.arguments?.getString("averageHeartRate")!!,
                totalDistance = it.arguments?.getString("totalDistance")!!,
                totalCalories = it.arguments?.getString("totalCalories")!!,
                elapsedTime = it.arguments?.getString("elapsedTime")!!,
                onRestartClick = {
                    navController.navigate(Screens.StartWorkoutScreen.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                })
        }
    }
}

