package com.example.pokerunwearos

sealed class Screens(
    val route: String
) {
    object ExerciseScreen : Screens("exercise")
    object ExerciseNotAvailable : Screens("exerciseNotAvail")
    object StartingUp : Screens("startingUp")
    object PreparingExercise : Screens("preparingExercise")
    object SummaryScreen : Screens("summaryScreen")
}

