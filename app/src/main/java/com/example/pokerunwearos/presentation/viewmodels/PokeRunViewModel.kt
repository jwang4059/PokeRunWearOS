package com.example.pokerunwearos.presentation.viewmodels

import android.Manifest
import androidx.compose.runtime.MutableState
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeCondition
import androidx.health.services.client.data.ExerciseGoal
import androidx.health.services.client.data.ExerciseGoalType
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseTypeCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokerunwearos.data.models.Workout
import com.example.pokerunwearos.data.repository.PassiveDataRepository
import com.example.pokerunwearos.data.repository.SettingsRepository
import com.example.pokerunwearos.data.repository.WorkoutRepository
import com.example.pokerunwearos.data.repository.health.HealthServicesRepository
import com.example.pokerunwearos.data.repository.health.ServiceState
import com.example.pokerunwearos.presentation.ui.utils.toExerciseType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PokeRunViewModel @Inject constructor(
    private val healthServicesRepository: HealthServicesRepository,
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository,
    passiveDataRepository: PassiveDataRepository,
) : ViewModel() {
    val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    private val exerciseFlow = flow {
        emit(
            ExerciseInfo(
                exerciseCapabilities = healthServicesRepository.getExerciseCapabilities(),
                isTrackingAnotherExercise = healthServicesRepository.isTrackingExerciseInAnotherApp(),
            )
        )
    }

    private val uiStateFlow = combine(
        settingsRepository.exerciseType,
        settingsRepository.exerciseGoal,
        passiveDataRepository.stepsDaily,
        exerciseFlow
    ) { exerciseType: String?, exerciseGoal: Double?, stepsDaily: Long?, exerciseInfo: ExerciseInfo ->
        return@combine PokeRunUiState(
            currentExerciseType = exerciseType,
            currentExerciseGoal = exerciseGoal,
            stepsDaily = stepsDaily,
            exerciseCapabilities = exerciseInfo.exerciseCapabilities,
            isTrackingAnotherExercise = exerciseInfo.isTrackingAnotherExercise
        )
    }

    val uiState = uiStateFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), PokeRunUiState()
    )

    private var _exerciseServiceState: MutableState<ServiceState> =
        healthServicesRepository.serviceState
    val exerciseServiceState = _exerciseServiceState

    init {
        // Start service for workout
        viewModelScope.launch {
            healthServicesRepository.createService()
        }

        // Register for daily steps
        viewModelScope.launch {
            val supported = healthServicesRepository.hasStepsDailyCapability()
            if (supported) healthServicesRepository.registerForStepsDailyData()
        }
    }

    suspend fun isExerciseInProgress(): Boolean {
        return healthServicesRepository.isExerciseInProgress()
    }

    fun hasExerciseCapabilities(
        exerciseType: ExerciseType? = null
    ): Boolean {
        val capabilities = uiState.value.exerciseCapabilities
        return if (exerciseType != null) capabilities?.get(exerciseType) != null else capabilities != null
    }

    fun setExercise(exerciseType: String) {
        viewModelScope.launch {
            settingsRepository.setExerciseType(exerciseType)
        }
    }

    fun setExerciseGoal(exerciseGoal: Double) {
        viewModelScope.launch {
            settingsRepository.setExerciseGoal(exerciseGoal)
        }
    }

    fun supportsGoalType(
        capabilities: ExerciseTypeCapabilities?,
        exerciseGoalType: ExerciseGoalType?,
        dataType: DataType<*, *>
    ) = healthServicesRepository.supportsGoalType(capabilities, exerciseGoalType, dataType)

    fun prepareExercise() = viewModelScope.launch {
        val exerciseType =
            uiState.value.currentExerciseType?.toExerciseType() ?: ExerciseType.RUNNING

        healthServicesRepository.prepareExercise(
            exerciseType
        )
    }

    fun startExercise() = viewModelScope.launch {

        val threshold = uiState.value.currentExerciseGoal

        val exerciseType =
            uiState.value.currentExerciseType?.toExerciseType() ?: ExerciseType.RUNNING

        val exerciseGoal =
            if (threshold != null && threshold != 0.0) ExerciseGoal.createOneTimeGoal(
                DataTypeCondition(
                    dataType = DataType.DISTANCE_TOTAL,
                    threshold = threshold,
                    comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
                )
            ) else null

        healthServicesRepository.startExercise(
            exerciseType, exerciseGoal
        )
    }

    fun pauseExercise() = viewModelScope.launch { healthServicesRepository.pauseExercise() }
    fun endExercise() = viewModelScope.launch { healthServicesRepository.endExercise() }
    fun resumeExercise() = viewModelScope.launch { healthServicesRepository.resumeExercise() }

    fun saveWorkout(workout: Workout) = viewModelScope.launch {
        workoutRepository.insertWorkout(workout)
    }
}

data class ExerciseInfo(
    val exerciseCapabilities: MutableMap<ExerciseType, ExerciseTypeCapabilities>? = mutableMapOf(),
    val isTrackingAnotherExercise: Boolean = false,
)

data class PokeRunUiState(
    val currentExerciseType: String? = null,
    val currentExerciseGoal: Double? = null,
    val stepsDaily: Long? = null,
    val exerciseCapabilities: MutableMap<ExerciseType, ExerciseTypeCapabilities>? = mutableMapOf(),
    val isTrackingAnotherExercise: Boolean = false,
)