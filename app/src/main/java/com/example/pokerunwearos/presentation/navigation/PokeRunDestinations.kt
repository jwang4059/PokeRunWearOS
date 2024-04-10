package com.example.pokerunwearos.presentation.navigation

sealed class PokeRunDestinations(
    val route: String
) {
    object ExerciseNotAvailable : PokeRunDestinations("exerciseNotAvail")
    object StartScreen : PokeRunDestinations("start")
    object ExerciseSelectionScreen : PokeRunDestinations("selectExercise")
    object MissionSelectionScreen : PokeRunDestinations("selectMission")

    object PreWorkoutScreen : PokeRunDestinations("preWorkout")
    object CountdownScreen : PokeRunDestinations("countdown")

    object TrackWorkoutScreen : PokeRunDestinations("trackWorkout")

    object PostWorkoutScreen : PokeRunDestinations("postWorkout")

    object SettingsMenuScreen : PokeRunDestinations("settingsMenu")
    object SettingsExerciseTypeScreen : PokeRunDestinations("settingsExerciseType")

    object SettingsExerciseGoalStepperScreen : PokeRunDestinations("settingsExerciseGoalStepper")

    object SettingsDailyStepsStepperScreen : PokeRunDestinations("settingsDailyStepsStepper")

    object SettingsGenderScreen : PokeRunDestinations("settingsGender")
    object SettingsLanguageScreen : PokeRunDestinations("settingsLanguage")


//    object MissionScreen: PokeRunDestinations("mission")
}

