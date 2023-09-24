package com.example.pokerunwearos.presentation.navigation

sealed class PokeRunDestinations(
    val route: String
) {
    object ExerciseNotAvailable : PokeRunDestinations("exerciseNotAvail")
    object StartScreen : PokeRunDestinations("start")
    object ExerciseSelectionScreen : PokeRunDestinations("selectExercise")
    object MissionSelectionScreen : PokeRunDestinations("selectMission")

    object PreWorkoutScreen : PokeRunDestinations("preWorkout")
    object TrackWorkoutScreen : PokeRunDestinations("trackWorkout")

    object PostWorkoutScreen : PokeRunDestinations("postWorkout")
    object MissionScreen: PokeRunDestinations("mission")


}

