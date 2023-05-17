package com.example.pokerunwearos.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class HealthServicesRepository @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {

    @Inject
    lateinit var exerciseClientManager: ExerciseClientManager

//    private var exerciseService: ForegroundService? = null

    suspend fun hasExerciseCapability(): Boolean = getExerciseCapabilities() != null

    private suspend fun getExerciseCapabilities() = exerciseClientManager.getExerciseCapabilities()
}