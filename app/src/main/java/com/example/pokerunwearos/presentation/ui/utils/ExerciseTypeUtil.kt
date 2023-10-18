package com.example.pokerunwearos.presentation.ui.utils

import androidx.health.services.client.data.ExerciseType

fun String.toExerciseType(): ExerciseType {
    return if (this == WALKING) ExerciseType.WALKING
    else if (this == TREADMILL) ExerciseType.RUNNING_TREADMILL
    else ExerciseType.RUNNING
}

fun ExerciseType.toFormattedString(): String {
    return if (this == ExerciseType.WALKING) WALKING
    else if (this == ExerciseType.RUNNING_TREADMILL) TREADMILL
    else RUNNING
}