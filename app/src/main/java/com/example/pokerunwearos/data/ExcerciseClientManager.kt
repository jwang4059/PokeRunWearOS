package com.example.pokerunwearos.data

import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseTypeCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.guava.await
import javax.inject.Inject

class ExerciseClientManager @Inject constructor (
    healthServicesClient: HealthServicesClient,
    coroutineScope: CoroutineScope
) {
    private val exerciseClient = healthServicesClient.exerciseClient
    private var exerciseCapabilities: ExerciseTypeCapabilities? = null
    private var capabilitiesLoaded = false

    suspend fun getExerciseCapabilities(): ExerciseTypeCapabilities? {
        val capabilities = exerciseClient.getCapabilitiesAsync().await()
        if (!capabilitiesLoaded) {
            if (ExerciseType.RUNNING in capabilities.supportedExerciseTypes) {
                exerciseCapabilities =
                    capabilities.getExerciseTypeCapabilities(ExerciseType.RUNNING)
            }
        }
        return exerciseCapabilities
    }

    suspend fun isExerciseInProgress(): Boolean {
        val exerciseInfo = exerciseClient.getCurrentExerciseInfoAsync().await()
        return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS
    }

    suspend fun isTrackingExerciseInAnotherApp(): Boolean {
        val exerciseInfo = exerciseClient.getCurrentExerciseInfoAsync().await()
        return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OTHER_APP_IN_PROGRESS

    }
}