package com.example.pokerunwearos.presentation.viewmodels

import android.Manifest
import androidx.compose.runtime.MutableState
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
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
import com.example.pokerunwearos.data.repository.health.MeasureMessage
import com.example.pokerunwearos.data.repository.health.ServiceState
import com.example.pokerunwearos.presentation.ui.utils.toExerciseType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
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

    private val exerciseTypes = arrayOf(
        ExerciseType.RUNNING, ExerciseType.RUNNING_TREADMILL, ExerciseType.WALKING
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

    private val _hrUiState = MutableStateFlow(HeartRateUiState())
    val hrUiState: StateFlow<HeartRateUiState> = _hrUiState.asStateFlow()

    private val hrEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)

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

        // Register for heart rate
        viewModelScope.launch {
            val supported = healthServicesRepository.hasHeartRateCapability()
            _hrUiState.update { currentState -> currentState.copy(hrSupported = supported) }
        }

        viewModelScope.launch {
            hrEnabled.collect {
                if (it) {
                    healthServicesRepository.heartRateMeasureFlow().takeWhile { hrEnabled.value }
                        .collect { measureMessage ->
                            when (measureMessage) {
                                is MeasureMessage.MeasureData -> {
                                    _hrUiState.update { currentState -> currentState.copy(hrBPM = measureMessage.data.last().value) }
                                }

                                is MeasureMessage.MeasureAvailability -> {
                                    _hrUiState.update { currentState ->
                                        currentState.copy(
                                            hrAvailability = measureMessage.availability
                                        )
                                    }
                                }
                            }
                        }
                }
            }
        }
    }

    fun setHrEnabled(isEnabled: Boolean) {
        hrEnabled.value = isEnabled

        var availability = hrUiState.value.hrAvailability
        if (!isEnabled) availability = DataTypeAvailability.UNKNOWN

        _hrUiState.update { currentState ->
            currentState.copy(
                hrAvailability = availability
            )
        }
    }

    suspend fun isExerciseInProgress(): Boolean {
        return healthServicesRepository.isExerciseInProgress()
    }

    fun hasExerciseCapabilities(
        capabilities: MutableMap<ExerciseType, ExerciseTypeCapabilities>?,
        exerciseType: ExerciseType? = null
    ): Boolean {
        if (capabilities == null) return true
        return if (exerciseType != null) capabilities[exerciseType] != null else exerciseTypes.all { it in capabilities }
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
    val exerciseCapabilities: MutableMap<ExerciseType, ExerciseTypeCapabilities>? = null,
    val isTrackingAnotherExercise: Boolean = false,
)

data class HeartRateUiState(
    val hrSupported: Boolean = false,
    val hrBPM: Double = 0.0,
    val hrAvailability: DataTypeAvailability = DataTypeAvailability.UNKNOWN
)

data class PokeRunUiState(
    val currentExerciseType: String? = null,
    val currentExerciseGoal: Double? = null,
    val stepsDaily: Long? = null,
    val exerciseCapabilities: MutableMap<ExerciseType, ExerciseTypeCapabilities>? = null,
    val isTrackingAnotherExercise: Boolean = false,
)