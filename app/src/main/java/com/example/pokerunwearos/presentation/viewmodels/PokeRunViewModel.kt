package com.example.pokerunwearos.presentation.viewmodels

import android.Manifest
import androidx.compose.runtime.MutableState
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DataTypeCondition
import androidx.health.services.client.data.ExerciseGoal
import androidx.health.services.client.data.ExerciseGoalType
import androidx.health.services.client.data.ExerciseTrackedStatus
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
import com.example.pokerunwearos.presentation.ui.utils.MeasurementUnit
import com.example.pokerunwearos.presentation.ui.utils.toExerciseType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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

    private val _currentExerciseType = settingsRepository.exerciseType
    val currentExerciseType = _currentExerciseType.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    private val _currentExerciseGoal = settingsRepository.exerciseGoal
    val currentExerciseGoal = _currentExerciseGoal.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    private val _dailyStepsGoal = settingsRepository.dailyStepsGoal
    val dailyStepsGoal = _dailyStepsGoal.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    private val _skipPrompt = settingsRepository.skipPrompt
    val skipPrompt = _skipPrompt.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    private val _autoPause = settingsRepository.autoPause
    val autoPause = _autoPause.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    private val _gender = settingsRepository.gender
    val gender = _gender.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    private val _language = settingsRepository.language
    val language = _language.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    private val _useMetric = settingsRepository.useMetric
    val metricInfo = _useMetric.map { MetricInfo(useMetric = it ?: false, measurementUnit = if (it == true) MeasurementUnit.METRIC else MeasurementUnit.IMPERIAL) }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    private val _stepsDaily = passiveDataRepository.stepsDaily
    val stepsDaily = _stepsDaily.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
    )

    private val _exerciseInfo = flow {
        emit(
            ExerciseInfo(
                exerciseCapabilities = healthServicesRepository.getExerciseCapabilities(),
                trackedStatus = healthServicesRepository.getExerciseTrackedStatus(),
                isTrackingAnotherExercise = healthServicesRepository.isTrackingExerciseInAnotherApp(),
            )
        )
    }
    val exerciseInfo = _exerciseInfo.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), null
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
        exerciseTypes: Array<ExerciseType>, checkAll: Boolean = false
    ): Boolean {
        val capabilities = exerciseInfo.value?.exerciseCapabilities ?: return true

        return if (!checkAll) {
            exerciseTypes.any { capabilities[it] != null }
        } else {
            exerciseTypes.all { capabilities[it] != null }
        }
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

    fun setDailyStepsGoal(steps: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyStepsGoal(steps)
        }
    }

    fun setSkipPrompt(skipPrompt: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSkipPrompt(skipPrompt)
        }
    }

    fun setAutoPause(autoPause: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoPause(autoPause)

            if (isExerciseInProgress()) {
                healthServicesRepository.overrideAutoPause(autoPause)
            }
        }
    }

    fun setGender(gender: String) {
        viewModelScope.launch {
            settingsRepository.setGender(gender)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }

    fun setUseMetric(useMetric: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseMetric(useMetric)
        }
    }

    fun supportsGoalType(
        capabilities: ExerciseTypeCapabilities?,
        exerciseGoalType: ExerciseGoalType?,
        dataType: DataType<*, *>
    ) = healthServicesRepository.supportsGoalType(capabilities, exerciseGoalType, dataType)

    fun prepareExercise() = viewModelScope.launch {
        val exerciseType =
            currentExerciseType.value?.toExerciseType() ?: ExerciseType.RUNNING

        healthServicesRepository.prepareExercise(
            exerciseType
        )
    }

    fun startExercise() = viewModelScope.launch {
        val isAutoPauseAndResumeEnabled = autoPause.value ?: false
        val threshold = currentExerciseGoal.value

        val exerciseType =
            currentExerciseType.value?.toExerciseType() ?: ExerciseType.RUNNING

        val exerciseGoal =
            if (threshold != null && threshold != 0.0) ExerciseGoal.createOneTimeGoal(
                DataTypeCondition(
                    dataType = DataType.DISTANCE_TOTAL,
                    threshold = threshold,
                    comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
                )
            ) else null

        healthServicesRepository.startExercise(
            exerciseType, exerciseGoal, isAutoPauseAndResumeEnabled
        )
    }

    fun pauseExercise() = viewModelScope.launch { healthServicesRepository.pauseExercise() }
    fun endExercise() = viewModelScope.launch { healthServicesRepository.endExercise() }
    fun resumeExercise() = viewModelScope.launch { healthServicesRepository.resumeExercise() }

    fun saveWorkout(workout: Workout) = viewModelScope.launch {
        workoutRepository.insertWorkout(workout)
    }

    fun resetService() = viewModelScope.launch { healthServicesRepository.resetService() }
}

data class ExerciseInfo(
    val exerciseCapabilities: MutableMap<ExerciseType, ExerciseTypeCapabilities>? = null,
    val isTrackingAnotherExercise: Boolean = false,
    val trackedStatus: Int = ExerciseTrackedStatus.UNKNOWN,
)

data class HeartRateUiState(
    val hrSupported: Boolean = false,
    val hrBPM: Double = 0.0,
    val hrAvailability: DataTypeAvailability = DataTypeAvailability.UNKNOWN
)

data class MetricInfo(
    val useMetric: Boolean = false,
    val measurementUnit: MeasurementUnit = MeasurementUnit.IMPERIAL
)