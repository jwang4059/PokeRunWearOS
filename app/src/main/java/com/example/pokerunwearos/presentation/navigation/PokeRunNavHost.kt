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
import com.example.pokerunwearos.presentation.composables.SettingsDailyStepsStepperScreen
import com.example.pokerunwearos.presentation.composables.SettingsExerciseGoalStepperScreen
import com.example.pokerunwearos.presentation.composables.SettingsExerciseTypeScreen
import com.example.pokerunwearos.presentation.composables.SettingsGenderScreen
import com.example.pokerunwearos.presentation.composables.SettingsLanguageScreen
import com.example.pokerunwearos.presentation.composables.SettingsMenuScreen
import com.example.pokerunwearos.presentation.composables.StartScreen
import com.example.pokerunwearos.presentation.composables.TrackWorkoutScreen
import com.example.pokerunwearos.presentation.ui.utils.MeasurementUnit
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
            val skipPrompt by viewModel.skipPrompt.collectAsStateWithLifecycle()
            val gender by viewModel.gender.collectAsStateWithLifecycle()
            val stepsDaily by viewModel.stepsDaily.collectAsStateWithLifecycle()
            val hrUiState by viewModel.hrUiState.collectAsStateWithLifecycle()

            StartScreen(permissions = viewModel.permissions,
                skipPrompt = skipPrompt,
                gender = gender,
                stepsDaily = stepsDaily,
                hrBPM = hrUiState.hrBPM,
                hrAvailability = hrUiState.hrAvailability,
                setHrEnabled = viewModel::setHrEnabled,
                navigateToExerciseSelection = { navController.navigate(PokeRunDestinations.ExerciseSelectionScreen.route) },
                navigateToSummary = { navController.navigate(PokeRunDestinations.PreWorkoutScreen.route) },
                navigateToSettings = {
                    navController.navigate(PokeRunDestinations.SettingsMenuScreen.route)
                })
        }

        composable(PokeRunDestinations.ExerciseSelectionScreen.route) {
            val currentExerciseType by viewModel.currentExerciseType.collectAsStateWithLifecycle()
            val exerciseInfo by viewModel.exerciseInfo.collectAsStateWithLifecycle()

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
                            exerciseInfo?.exerciseCapabilities?.get(
                                currentExerciseType?.toExerciseType()
                            ), ExerciseGoalType.ONE_TIME_GOAL, DataType.DISTANCE_TOTAL
                        )
                    ) {
                        navController.navigate(PokeRunDestinations.MissionSelectionScreen.route)
                    } else {
                        navController.navigate(
                            PokeRunDestinations.PreWorkoutScreen.route
                        )
                    }
                })
        }

        composable(PokeRunDestinations.MissionSelectionScreen.route) {
            MissionSelectionScreen(setExerciseGoal = viewModel::setExerciseGoal,
                navigateToSummary = {
                    navController.navigate(PokeRunDestinations.PreWorkoutScreen.route)
                })
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
            val currentExerciseType by viewModel.currentExerciseType.collectAsStateWithLifecycle()
            val currentExerciseGoal by viewModel.currentExerciseGoal.collectAsStateWithLifecycle()
            val exerciseInfo by viewModel.exerciseInfo.collectAsStateWithLifecycle()

            exerciseInfo?.let { it1 ->
                PreWorkoutScreen(
                    serviceState = serviceState,
                    permissions = permissions,
                    trackedStatus = it1.trackedStatus,
                    exerciseType = currentExerciseType,
                    exerciseGoal = currentExerciseGoal,
                    prepareExercise = viewModel::prepareExercise,
                    endExercise = viewModel::endExercise,
                    navigateToCountdown = {
                        navController.navigate(PokeRunDestinations.CountdownScreen.route)
                    },
                )
            }
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
            val metricInfo by viewModel.metricInfo.collectAsStateWithLifecycle()

            TrackWorkoutScreen(serviceState = serviceState,
                measurementUnit = metricInfo?.measurementUnit ?: MeasurementUnit.IMPERIAL,
                onResumeClick = viewModel::resumeExercise,
                onPauseClick = viewModel::pauseExercise,
                onEndClick = viewModel::endExercise,
                saveWorkout = viewModel::saveWorkout,
                navigateToExerciseSelection = { navController.navigate(PokeRunDestinations.ExerciseSelectionScreen.route) },
                navigateToSettings = {
                    navController.navigate(PokeRunDestinations.SettingsMenuScreen.route)
                },
                navigateToPostWorkout = {
                    navController.navigate(PokeRunDestinations.PostWorkoutScreen.route) {
                        popUpTo(PokeRunDestinations.TrackWorkoutScreen.route) {
                            inclusive = true
                        }
                    }
                },
                navigateToMain = {
                    navController.navigate(PokeRunDestinations.StartScreen.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                })
        }

        composable(PokeRunDestinations.PostWorkoutScreen.route) {
            val postWorkoutViewModel = hiltViewModel<PostWorkoutViewModel>()
            val metricInfo by viewModel.metricInfo.collectAsStateWithLifecycle()
            val uiState by postWorkoutViewModel.uiState.collectAsStateWithLifecycle()

            PostWorkoutScreen(workout = uiState,
                measurementUnit = metricInfo?.measurementUnit ?: MeasurementUnit.IMPERIAL,
                fetchPokemon = postWorkoutViewModel::fetchData,
                onRestartClick = {
                    navController.navigate(PokeRunDestinations.StartScreen.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                })
        }

        composable(PokeRunDestinations.SettingsMenuScreen.route) {
            val currentExerciseType by viewModel.currentExerciseType.collectAsStateWithLifecycle()
            val currentExerciseGoal by viewModel.currentExerciseGoal.collectAsStateWithLifecycle()
            val dailyStepsGoal by viewModel.dailyStepsGoal.collectAsStateWithLifecycle()
            val gender by viewModel.gender.collectAsStateWithLifecycle()
            val language by viewModel.language.collectAsStateWithLifecycle()
            val skipPrompt by viewModel.skipPrompt.collectAsStateWithLifecycle()
            val autoPause by viewModel.autoPause.collectAsStateWithLifecycle()
            val metricInfo by viewModel.metricInfo.collectAsStateWithLifecycle()
            val exerciseInfo by viewModel.exerciseInfo.collectAsStateWithLifecycle()

            SettingsMenuScreen(
                capabilities = exerciseInfo?.exerciseCapabilities?.get(currentExerciseType?.toExerciseType()),
                currentExerciseType = currentExerciseType,
                currentExerciseGoal = currentExerciseGoal,
                dailyStepsGoal = dailyStepsGoal,
                gender = gender,
                language = language,
                skipPrompt = skipPrompt?: false,
                autoPause = autoPause ?: false,
                useMetric = metricInfo?.useMetric ?: false,
                measurementUnit = metricInfo?.measurementUnit ?: MeasurementUnit.IMPERIAL,
                setSkipPrompt = viewModel::setSkipPrompt,
                setAutoPause = viewModel::setAutoPause,
                setUseMetric = viewModel::setUseMetric,
                isExerciseInProgress = viewModel::isExerciseInProgress,
                navigateToSettingsExerciseType = {
                    navController.navigate(PokeRunDestinations.SettingsExerciseTypeScreen.route)
                },
                navigateToSettingsExerciseGoal = {
                    navController.navigate(PokeRunDestinations.SettingsExerciseGoalStepperScreen.route)
                },
                navigateToSettingsDailyStepsGoal = {
                    navController.navigate(PokeRunDestinations.SettingsDailyStepsStepperScreen.route)
                },
                navigateToSettingsGender = {
                    navController.navigate(PokeRunDestinations.SettingsGenderScreen.route)
                },
                navigateToSettingsLanguage = {
                    navController.navigate(PokeRunDestinations.SettingsLanguageScreen.route)
                },
            )
        }

        composable(PokeRunDestinations.SettingsExerciseTypeScreen.route) {
            SettingsExerciseTypeScreen(hasCapabilities = viewModel::hasExerciseCapabilities,
                setExerciseType = viewModel::setExercise,
                navigateBack = { navController.popBackStack() })
        }

        composable(PokeRunDestinations.SettingsExerciseGoalStepperScreen.route) {
            val currentExerciseGoal by viewModel.currentExerciseGoal.collectAsStateWithLifecycle()
            val metricInfo by viewModel.metricInfo.collectAsStateWithLifecycle()

            SettingsExerciseGoalStepperScreen(
                measurementUnit = metricInfo?.measurementUnit ?: MeasurementUnit.IMPERIAL,
                exerciseGoal = currentExerciseGoal ?: 0.0,
                setExerciseGoal = viewModel::setExerciseGoal
            )
        }

        composable(PokeRunDestinations.SettingsDailyStepsStepperScreen.route) {
            val dailyStepsGoal by viewModel.dailyStepsGoal.collectAsStateWithLifecycle()

            SettingsDailyStepsStepperScreen(
                dailyStepsGoal = dailyStepsGoal ?: 0,
                setDailyStepsGoal = viewModel::setDailyStepsGoal
            )
        }

        composable(PokeRunDestinations.SettingsGenderScreen.route) {
            SettingsGenderScreen(
                setGender = viewModel::setGender,
                navigateBack = { navController.popBackStack() })
        }

        composable(PokeRunDestinations.SettingsLanguageScreen.route) {
            SettingsLanguageScreen(setLanguage = viewModel::setLanguage,
                navigateBack = { navController.popBackStack() })
        }
    }
}