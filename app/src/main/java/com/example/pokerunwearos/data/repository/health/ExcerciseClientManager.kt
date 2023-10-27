package com.example.pokerunwearos.data.repository.health

import android.util.Log
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseGoal
import androidx.health.services.client.data.ExerciseGoalType
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseTypeCapabilities
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.LocationAvailability
import androidx.health.services.client.data.WarmUpConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.guava.await
import javax.inject.Inject

class ExerciseClientManager @Inject constructor(
    healthServicesClient: HealthServicesClient, coroutineScope: CoroutineScope
) {
    private val exerciseClient = healthServicesClient.exerciseClient
    private var exerciseCapabilities: MutableMap<ExerciseType, ExerciseTypeCapabilities>? = null
    private var capabilitiesLoaded = false

    suspend fun getExerciseCapabilities(): MutableMap<ExerciseType, ExerciseTypeCapabilities>? {
        val capabilities = exerciseClient.getCapabilitiesAsync().await()

        exerciseCapabilities = mutableMapOf()

        EXERCISE_TYPES.forEach {
            if (!capabilitiesLoaded) {
                if (it in capabilities.supportedExerciseTypes) {
                    exerciseCapabilities!![it] = capabilities.getExerciseTypeCapabilities(it)
                }
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

    fun supportsGoalType(
        capabilities: ExerciseTypeCapabilities?,
        exerciseGoalType: ExerciseGoalType?,
        dataType: DataType<*, *>
    ): Boolean {
        if (capabilities == null || exerciseGoalType == null) return false

        val supported = when (exerciseGoalType) {
            ExerciseGoalType.ONE_TIME_GOAL -> capabilities.supportedGoals[dataType]
            ExerciseGoalType.MILESTONE -> capabilities.supportedMilestones[dataType]
            else -> null
        }

        return supported != null && ComparisonType.GREATER_THAN_OR_EQUAL in supported
    }

    suspend fun startExercise(
        exerciseType: ExerciseType, exerciseGoal: ExerciseGoal<Double>?
    ) {
        Log.d(TAG, "Starting %s".format(exerciseType.name))

        if (exerciseGoal != null) {
            Log.d(
                TAG,
                "Current goal is %s meters".format(exerciseGoal.dataTypeCondition.threshold.toString())
            )
        } else {
            Log.d(TAG, "No goal for workout")
        }

        // Types for which we want to receive metrics. Only ask for ones that are supported.
        val capabilitiesMap = getExerciseCapabilities()
        val capabilities = capabilitiesMap?.get(exerciseType) ?: return
        val dataTypes = setOf(
            DataType.HEART_RATE_BPM,
            DataType.HEART_RATE_BPM_STATS,
            DataType.CALORIES_TOTAL,
            DataType.DISTANCE_TOTAL,
            DataType.STEPS_TOTAL,
            DataType.PACE,
            DataType.PACE_STATS,
            DataType.SPEED,
            DataType.SPEED_STATS
        ).intersect(capabilities.supportedDataTypes)

        // Add goal to list of goals
        val exerciseGoals = mutableListOf<ExerciseGoal<Double>>()

        if ((exerciseGoal != null) && supportsGoalType(
                capabilities, exerciseGoal.exerciseGoalType, exerciseGoal.dataTypeCondition.dataType
            )
        ) {
            exerciseGoals.add(exerciseGoal)
        }

        val isGpsEnabled = exerciseType != ExerciseType.RUNNING_TREADMILL

        val config = ExerciseConfig(
            exerciseType = exerciseType,
            dataTypes = dataTypes,
            isAutoPauseAndResumeEnabled = false,
            isGpsEnabled = isGpsEnabled,
            exerciseGoals = exerciseGoals
        )
        exerciseClient.startExerciseAsync(config).await()
    }

    /***
     * Note: don't call this method from outside of ExerciseService.kt
     * when acquiring calories or distance.
     */
    suspend fun prepareExercise(exerciseType: ExerciseType) {
        Log.d(TAG, "Preparing %s".format(exerciseType.name))
        val dataTypes = mutableSetOf<DeltaDataType<*, *>>(
            DataType.HEART_RATE_BPM
        )

        if (exerciseType != ExerciseType.RUNNING_TREADMILL) dataTypes.add(DataType.LOCATION)

        val warmUpConfig = WarmUpConfig(
            exerciseType, dataTypes
        )

        try {
            exerciseClient.prepareExerciseAsync(warmUpConfig).await()
        } catch (e: Exception) {
            Log.e(TAG, "Prepare exercise failed - ${e.message}")
        }
    }

    suspend fun endExercise() {
        Log.d(TAG, "Ending exercise")
        exerciseClient.endExerciseAsync().await()
    }

    suspend fun pauseExercise() {
        Log.d(TAG, "Pausing exercise")
        exerciseClient.pauseExerciseAsync().await()
    }

    suspend fun resumeExercise() {
        Log.d(TAG, "Resuming exercise")
        exerciseClient.resumeExerciseAsync().await()
    }

    /** Wear OS 3.0 reserves two buttons for the OS. For devices with more than 2 buttons,
     * consider implementing a "press" to mark lap feature**/
    suspend fun markLap() {
        if (isExerciseInProgress()) {
            exerciseClient.markLapAsync().await()
        }
    }

    /**
     * When the flow starts, it will register an [ExerciseUpdateCallback] and start to emit
     * messages. When there are no more subscribers, or when the coroutine scope is
     * cancelled, this flow will unregister the listener.
     * [callbackFlow] is used to bridge between a callback-based API and Kotlin flows.
     */
    val exerciseUpdateFlow = callbackFlow {
        val callback = object : ExerciseUpdateCallback {
            override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                coroutineScope.runCatching {
                    trySendBlocking(ExerciseMessage.ExerciseUpdateMessage(update))
                }
            }

            override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
                coroutineScope.runCatching {
                    trySendBlocking(ExerciseMessage.LapSummaryMessage(lapSummary))
                }
            }

            override fun onRegistered() {
                Log.d(TAG, "Registering for exercise data")
            }

            override fun onRegistrationFailed(throwable: Throwable) {
                Log.d(TAG, "Unregistering for exercise data")
            }

            override fun onAvailabilityChanged(
                dataType: DataType<*, *>, availability: Availability
            ) {
                if (availability is LocationAvailability) {
                    coroutineScope.runCatching {
                        trySendBlocking(ExerciseMessage.LocationAvailabilityMessage(availability))
                    }
                }
            }
        }
        exerciseClient.setUpdateCallback(callback)
        awaitClose {
            exerciseClient.clearUpdateCallbackAsync(callback)
        }
    }


    private companion object {
        val EXERCISE_TYPES = listOf(
            ExerciseType.RUNNING, ExerciseType.RUNNING_TREADMILL, ExerciseType.WALKING
        )

        const val TAG = "Health Exercise Client"
    }
}

sealed class ExerciseMessage {
    class ExerciseUpdateMessage(val exerciseUpdate: ExerciseUpdate) : ExerciseMessage()
    class LapSummaryMessage(val lapSummary: ExerciseLapSummary) : ExerciseMessage()
    class LocationAvailabilityMessage(val locationAvailability: LocationAvailability) :
        ExerciseMessage()
}