package com.example.pokerunwearos

sealed class Screens(
    val route: String
) {
    object ExerciseNotAvailable : Screens("exerciseNotAvail")
    object StartWorkoutScreen : Screens("startWorkout")
    object ExerciseSelectionScreen : Screens("selectExercise")
    object MissionSelectionScreen : Screens("selectMission")

    object PreWorkoutScreen : Screens("preWorkout")
    object TrackWorkoutScreen : Screens("trackWorkout")

    object PostWorkoutScreen : Screens("postWorkout")
    object MissionScreen: Screens("mission")


}

