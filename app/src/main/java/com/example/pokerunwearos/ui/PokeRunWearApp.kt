package com.example.pokerunwearos.ui

import androidx.compose.runtime.Composable
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
            StartingUp(greetingName = "Trainer")
        }
    }
}

