package com.example.pokerunwearos

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.pokerunwearos.presentation.navigation.PokeRunNavHost
import com.example.pokerunwearos.presentation.navigation.PokeRunDestinations
import com.example.pokerunwearos.presentation.viewmodels.PokeRunViewModel


@Composable
fun PokeRunWearApp(
    viewModel: PokeRunViewModel,
    navController: NavHostController = rememberSwipeDismissableNavController(),
    startDestination: String = PokeRunDestinations.StartScreen.route,
) {
    PokeRunNavHost(
        viewModel = viewModel, navController = navController, startDestination = startDestination
    )
}

