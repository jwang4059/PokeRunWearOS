/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.pokerunwearos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.pokerunwearos.presentation.navigation.PokeRunDestinations
import com.example.pokerunwearos.presentation.ui.theme.PokeRunWearOSTheme
import com.example.pokerunwearos.presentation.viewmodels.PokeRunViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    private val pokeRunViewModel by viewModels<PokeRunViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        installSplashScreen()

        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val destination = when (pokeRunViewModel.isExerciseInProgress()) {
                false -> PokeRunDestinations.StartScreen.route
                true -> PokeRunDestinations.TrackWorkoutScreen.route
            }

            setContent {
                navController = rememberSwipeDismissableNavController()

                PokeRunWearOSTheme {
                    PokeRunWearApp(
                        viewModel = pokeRunViewModel,
                        navController = navController,
                        startDestination = destination
                    )
                }
            }
        }
    }
}

