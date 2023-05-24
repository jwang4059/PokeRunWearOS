package com.example.pokerunwearos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.example.pokerunwearos.Screens


@Composable
fun PokeRunWearApp(
    navController: NavHostController,
    startDestination: String
) {
    SwipeDismissableNavHost(
        navController = navController, startDestination = startDestination
    ) {
        composable(Screens.StartingUp.route) {
            val viewModel = hiltViewModel<PokeRunViewModel>()
            val serviceState by viewModel.exerciseServiceState
            val permissions = viewModel.permissions
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            StartingUp(
                serviceState = serviceState,
                permissions = permissions,
                hasCapabilities = uiState.hasExerciseCapabilities,
                isTrackingAnotherExercise = uiState.isTrackingAnotherExercise
            )
        }
    }
}

